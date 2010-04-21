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

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;

/**
 * A {@link Initializable} service which schedules all
 * configured triggers on application startup.
 *
 * @author Willi Schoenborn
 */
final class CronService implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(CronService.class);

    private final ScheduledExecutorService scheduler;
    
    private final Set<TriggerBinding> bindings;
    
    @Inject
    public CronService(@Cron ScheduledExecutorService scheduler, Set<TriggerBinding> bindings) {
        this.scheduler = Preconditions.checkNotNull(scheduler, "Scheduler");
        this.bindings = Preconditions.checkNotNull(bindings, "Bindings");
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
            
            scheduler.schedule(command, delay, TimeUnit.MILLISECONDS);
        }
    }
    
    private long computeDelay(CronExpression expression) {
        return computeDelay(expression, new Date());
    }
    
    private long computeDelay(CronExpression expression, Date after) {
        final Date start = expression.getNextValidTimeAfter(after);
        return start.getTime() - System.currentTimeMillis(); 
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
                runnable.run();
            } finally {
                reschedule();
            }
        }
        
        private void reschedule() {
            assert startedAt != null : "Expected Start date to be set";
            final long delay = computeDelay(expression, startedAt);
            scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
        }
        
    }
    
}
