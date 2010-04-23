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
