/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package de.cosmocode.palava.cron;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import de.cosmocode.commons.Stateful;
import de.cosmocode.commons.concurrent.TimeUnits;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;

/**
 * A {@link Initializable} service which schedules all
 * configured triggers on application startup.
 *
 * @author Willi Schoenborn
 */
final class CronService implements Initializable, UncaughtExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CronService.class);

    private final ScheduledExecutorService scheduler;
    
    private final Set<TriggerBinding> bindings;
    
    private UncaughtExceptionHandler handler = this;
    
    @Inject
    public CronService(@Cron ScheduledExecutorService scheduler, Set<TriggerBinding> bindings) {
        this.scheduler = Preconditions.checkNotNull(scheduler, "Scheduler");
        this.bindings = Preconditions.checkNotNull(bindings, "Bindings");
    }
    
    @Inject(optional = true)
    void setHandler(@Cron UncaughtExceptionHandler handler) {
        this.handler = Preconditions.checkNotNull(handler, "Handler");
    }

    @Override
    public void initialize() throws LifecycleException {
        LOG.info("Scheduling {} tasks", bindings.size());
        
        for (TriggerBinding binding : bindings) {
            final Runnable runnable = binding.getCommand();
            final String expression = binding.getExpression();
            final CronExpression cronExpression;
            
            try {
                cronExpression = new CronExpression(expression);
            } catch (ParseException e) {
                throw new LifecycleException(e);
            }
            
            final Runnable command = new ReschedulingRunnable(runnable, cronExpression);
            final long delay = computeDelay(cronExpression);
            
            if (delay == -1) {
                LOG.info("Cron expression '{}' for {} is not satisfied", expression, runnable);
            } else {
                final TimeUnit human = TimeUnits.forMortals(delay, TimeUnit.MILLISECONDS);
                LOG.info("Scheduling {} to run in {} {}", new Object[] {
                    runnable, human.convert(delay, TimeUnit.MILLISECONDS), human.name().toLowerCase()
                });
                schedule(command, delay);
            }
        }
    }
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error("Uncaught exception in " + t, e);
    }
    
    private void schedule(Runnable command, long delay) {
        final Future<?> future = scheduler.schedule(command, delay, TimeUnit.MILLISECONDS);
        
        scheduler.execute(new Runnable() {
            
            @Override
            public void run() {
                LOG.debug("Starting watcher thread");
                try {
                    future.get();
                    // dead code here?
                } catch (InterruptedException e) {
                    LOG.error("Scheduler was interrupted", e);
                } catch (CancellationException e) {
                    LOG.info("Watcher thread has been cancelled");
                } catch (ExecutionException e) {
                    handler.uncaughtException(Thread.currentThread(), e.getCause());
                }
                LOG.debug("Watcher thread terminated");
            }
            
        });
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
            startedAt = new Date();
            try {
                if (isInRunningState()) {
                    LOG.trace("Performing scheduled execution of {}", runnable);
                    runnable.run();
                } else {
                    LOG.info("{} is currently not in running mode, skipping execution.", runnable);
                }
            } finally {
                reschedule();
            }
        }
        
        // TODO should this really be here? @Magic?
        private boolean isInRunningState() {
            if (runnable instanceof Stateful) {
                return Stateful.class.cast(runnable).isRunning();
            } else {
                return true;
            }
        }
        
        private void reschedule() {
            assert startedAt != null : "Expected Start date to be set";
            LOG.debug("Rescheduling {}", runnable);
            final long delay = computeDelay(expression, startedAt);

            if (delay == -1) {
                LOG.info("Cron expression '{}' for {} is not longer satisfied", expression, runnable);
            } else {
                final TimeUnit human = TimeUnits.forMortals(delay, TimeUnit.MILLISECONDS);
                LOG.info("Scheduling {} to run again in {} {}", new Object[] {
                    runnable, human.convert(delay, TimeUnit.MILLISECONDS), human.name().toLowerCase()
                });
                schedule(this, delay);
            }
        }
        
    }
    
}
