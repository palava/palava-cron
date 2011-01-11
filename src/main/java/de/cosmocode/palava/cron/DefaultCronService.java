/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.cron;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.internal.Sets;
import com.google.inject.name.Named;

import de.cosmocode.commons.concurrent.TimeUnits;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;

/**
 * A {@link Initializable} service which schedules all
 * configured triggers on application startup.
 *
 * @author Willi Schoenborn
 */
final class DefaultCronService implements CronService, Initializable, UncaughtExceptionHandler, Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCronService.class);

    private final ScheduledExecutorService scheduler;
    
    private final Set<TriggerBinding> bindings;
    
    private boolean disposed;
    
    private final ConcurrentMap<Runnable, Future<?>> futures = Maps.newConcurrentMap();
    
    private UncaughtExceptionHandler handler = this;
    
    private long taskShutdownTimeout = 1;
    
    private TimeUnit taskShutdownTimeoutUnit = TimeUnit.MINUTES;
    
    @Inject
    public DefaultCronService(@Cron ScheduledExecutorService scheduler, Set<TriggerBinding> bindings) {
        this.scheduler = Preconditions.checkNotNull(scheduler, "Scheduler");
        this.bindings = Preconditions.checkNotNull(bindings, "Bindings");
    }
    
    @Inject(optional = true)
    void setHandler(@Cron UncaughtExceptionHandler handler) {
        this.handler = Preconditions.checkNotNull(handler, "Handler");
    }
    
    @Inject(optional = true)
    void setTaskShutdownTimeout(@Named(CronConfig.TASK_SHUTDOWN_TIMEOUT) long taskShutdownTimeout) {
        this.taskShutdownTimeout = taskShutdownTimeout;
    }
    
    @Inject(optional = true)
    void setTaskShutdownTimeoutUnit(@Named(CronConfig.TASK_SHUTDOWN_TIMEOUT_UNIT) TimeUnit taskShutdownTimeoutUnit) {
        this.taskShutdownTimeoutUnit = Preconditions.checkNotNull(taskShutdownTimeoutUnit, "TaskShutdownTimeoutUnit");
    }

    @Override
    public void initialize() throws LifecycleException {
        LOG.info("Scheduling {} tasks", bindings.size());
        
        for (TriggerBinding binding : bindings) {
            final Runnable runnable = binding.getCommand();
            final CronExpression expression = binding.getExpression();
            schedule(runnable, expression);
        }
    }
    
    @Override
    public void schedule(Runnable runnable, String expression) {
        try {
            schedule(runnable, new CronExpression(expression));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    private void schedule(Runnable runnable, CronExpression expression) {
        final Runnable command = new ReschedulingRunnable(runnable, expression);
        final long delay = computeDelay(expression);
        
        if (delay == -1) {
            LOG.info("Cron expression '{}' for {} is not satisfied", expression, runnable);
        } else {
            // may save some time here
            if (LOG.isInfoEnabled()) {
                final TimeUnit human = TimeUnits.forMortals(delay, TimeUnit.MILLISECONDS);
                LOG.info("Scheduling {} to run in {} {}", new Object[] {
                    runnable, human.convert(delay, TimeUnit.MILLISECONDS), human.name().toLowerCase()
                });
            }
            schedule(command, delay);
        }
    }
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error("Uncaught exception in " + t, e);
    }
    
    private void schedule(Runnable command, long delay) {
        final Future<?> future = scheduler.schedule(command, delay, TimeUnit.MILLISECONDS);
        futures.put(command, future);
    }

    private long computeDelay(CronExpression expression) {
        return computeDelay(expression, new Date());
    }
    
    /**
     * Computes the delay for the next execution in milliseconds.
     * 
     * @param expression the cron expression
     * @param after the date after which the next run should happen
     * @return the computed delay or -1 if now date after the specified one
     *         satisifies the given cron expression
     */
    private long computeDelay(CronExpression expression, Date after) {
        final Date start = expression.getNextValidTimeAfter(after);
        return start == null ? -1 : start.getTime() - System.currentTimeMillis(); 
    }
    
    /**
     * Implementation of the {@link Runnable} interface which reschedules itself
     * after every execution.
     *
     * @author Willi Schoenborn
     */
    private final class ReschedulingRunnable implements Runnable {
        
        private final Runnable runnable;
        
        private final CronExpression expression;
        
        private Date startedAt;

        public ReschedulingRunnable(Runnable runnable, CronExpression expression) {
            this.runnable = Preconditions.checkNotNull(runnable, "Runnable");
            this.expression = Preconditions.checkNotNull(expression, "Expression");
        }
        
        @Override
        public void run() {
            if (isDisposed()) {
                LOG.debug("Suppressing scheduled execution of {} due to shutdown", runnable);
            } else {
                startedAt = new Date();
                try {
                    LOG.trace("Performing scheduled execution of {}", runnable);
                    try {
                        runnable.run();
                        /* CHECKSTYLE:OFF */
                    } catch (RuntimeException e) {
                        /* CHECKSTYLE:ON */
                        handler.uncaughtException(Thread.currentThread(), e);
                    }
                } finally {
                    futures.remove(this);
                    reschedule();
                }
            }
        }
        
        private void reschedule() {
            if (scheduler.isShutdown()) {
                LOG.debug("Suppressing {} from beind re-scheduled due to {} shutting down", runnable, scheduler);
            } else if (isDisposed()) {
                LOG.debug("Suppressing re-scheduling of {} due to shutdown", runnable);
            } else {
                assert startedAt != null : "Expected Start date to be set";
                LOG.debug("Rescheduling {}", runnable);
                final long delay = computeDelay(expression, startedAt);
                
                if (delay == -1) {
                    LOG.info("Cron expression '{}' for {} is not longer satisfied", expression, runnable);
                } else {
                    // may save some time here
                    if (LOG.isInfoEnabled()) {
                        final TimeUnit human = TimeUnits.forMortals(delay, TimeUnit.MILLISECONDS);
                        LOG.info("Scheduling {} to run again in {} {}", new Object[] {
                            runnable, human.convert(delay, TimeUnit.MILLISECONDS), human.name().toLowerCase()
                        });
                    }
                    schedule(this, delay);
                }
            }
        }
        
    }
    
    private boolean isDisposed() {
        return disposed;
    }
    
    @Override
    public void dispose() throws LifecycleException {
        final Set<Future<?>> running = Sets.newHashSet();
        for (Future<?> future : futures.values()) {
            if (future.isDone()) {
                LOG.trace("No need to dispose {} (is already done)", future);
            } else if (future.cancel(false)) {
                LOG.debug("Successfully cancelled {}", future);
            } else {
                running.add(future);
            }
        }
        
        final Iterator<Future<?>> iterator = running.iterator();
        
        while (iterator.hasNext()) {
            final Future<?> future = iterator.next();
            try {
                future.get(taskShutdownTimeout, taskShutdownTimeoutUnit);
            } catch (InterruptedException e) {
                LOG.info("Interrupted while waiting for {}", future);
            } catch (CancellationException e) {
                LOG.info("{} has been cancelled during wait", future);
            } catch (ExecutionException e) {
                LOG.error("Execution of " + future + " failed with an exception", e);
            } catch (TimeoutException e) {
                final TimeUnit humanUnit = TimeUnits.forMortals(taskShutdownTimeout, taskShutdownTimeoutUnit);
                final long human = humanUnit.convert(taskShutdownTimeout, taskShutdownTimeoutUnit);
                LOG.warn("{} has exceeded the maximum wait limit of {} {}", new Object[] {
                    future, human, humanUnit.name().toLowerCase()
                });
            } finally {
                iterator.remove();
            }
        }
    }
    
}
