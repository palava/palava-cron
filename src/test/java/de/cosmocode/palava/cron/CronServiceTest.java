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

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.Sets;

import de.cosmocode.commons.State;
import de.cosmocode.commons.Stateful;

/**
 * Tests {@link CronService}.
 *
 * @author Willi Schoenborn
 */
public final class CronServiceTest {

    /**
     * A runtime execption indicating a success ;).
     *
     * @author Willi Schoenborn
     */
    private static final class Success extends RuntimeException {

        private static final long serialVersionUID = 8246542271989524124L;
        
    }
    
    private CronService unit(Set<TriggerBinding> bindings) {
        return unit(Executors.newSingleThreadScheduledExecutor(), bindings);
    }
    
    private CronService unit(ScheduledExecutorService scheduler, Set<TriggerBinding> bindings) {
        return new CronService(scheduler, bindings);
    }
    
    /**
     * Tests {@link CronService#initialize()} without configured bindings.
     */
    @Test
    public void noBindings() {
        final Set<TriggerBinding> bindings = Sets.newHashSet();
        unit(bindings).initialize();
    }
    
    /**
     * Tests {@link CronService#initialize()} with one binding.
     */
    @Test(expected = Success.class)
    public void singleBinding() {
        final TriggerBinding binding = EasyMock.createMock("binding", TriggerBinding.class);
        EasyMock.expect(binding.getExpression()).andReturn("0/5 * * * * ?");
        EasyMock.expect(binding.getCommand()).andReturn(new Runnable() {
            
            @Override
            public void run() {
                throw new Success();
            }   
            
        });
        
        final ScheduledExecutorService scheduler = new MockScheduledExecutorService() {
            
            private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            
            private int runs;
            
            @Override
            public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
                if (runs++ == 1) return null;
                final ScheduledFuture<?> future = scheduler.schedule(runnable, delay, unit);
                try {
                    future.get();
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                } catch (ExecutionException e) {
                    Assert.assertTrue(e.getCause() instanceof Success);
                    throw Success.class.cast(e.getCause());
                }
                return future;
            }
            
            @Override
            public void execute(Runnable command) {
                scheduler.execute(command);
            }
            
        };
        
        EasyMock.replay(binding);
        
        final Set<TriggerBinding> bindings = ImmutableSet.of(binding);
        
        try {
            unit(scheduler, bindings).initialize();
        } finally {
            EasyMock.verify(binding);
        }
    }
    
    /**
     * A {@link Stateful} {@link Runnable} which never leaves {@link State#TERMINATED}.
     *
     * @author Willi Schoenborn
     */
    private static final class SuspendedRunnable implements Runnable, Stateful {

        @Override
        public State currentState() {
            return State.TERMINATED;
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void run() {
            Assert.fail();
        }
        
    }
    
    /**
     * Tests {@link CronService#initialize()} with a suspended {@link Runnable}.
     */
    @Test
    public void suspended() {
        final TriggerBinding binding = EasyMock.createMock("binding", TriggerBinding.class);
        EasyMock.expect(binding.getExpression()).andReturn("0/5 * * * * ?");
        EasyMock.expect(binding.getCommand()).andReturn(new SuspendedRunnable());
        
        final ScheduledExecutorService scheduler = new MockScheduledExecutorService() {
            
            private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            
            private int runs;
            
            @Override
            public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
                if (runs++ == 1) return null;
                final ScheduledFuture<?> future = scheduler.schedule(runnable, delay, unit);
                try {
                    future.get();
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                } catch (ExecutionException e) {
                    Assert.assertTrue(e.getCause() instanceof Success);
                    throw Success.class.cast(e.getCause());
                }
                return future;
            }
            
            @Override
            public void execute(Runnable command) {
                scheduler.execute(command);
            }
            
        };
        
        EasyMock.replay(binding);
        
        final Set<TriggerBinding> bindings = ImmutableSet.of(binding);
        
        try {
            unit(scheduler, bindings).initialize();
        } finally {
            EasyMock.verify(binding);
        }
    }

}
