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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.cosmocode.palava.concurrent.ForwardingScheduledExecutorService;

/**
 * Mock implementation of the {@link ScheduledExecutorService} which throws
 * an {@link UnsupportedOperationException} on every method call.
 *
 * @author Willi Schoenborn
 */
final class MockScheduledExecutorService extends ForwardingScheduledExecutorService {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    private int runs;

    @Override
    protected ScheduledExecutorService delegate() {
        return scheduler;
    }
    
    @Override
    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
        if (runs++ == 1) return null;
        final ScheduledFuture<?> future = scheduler.schedule(runnable, delay, unit);
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } catch (ExecutionException e) {
            throw new AssertionError(e);
        }
        return future;
    }

    @Override
    public void execute(Runnable command) {
        scheduler.execute(command);
    }

    @Override
    public boolean isShutdown() {
        return scheduler.isShutdown();
    }

}
