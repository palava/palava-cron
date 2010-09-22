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

import java.text.ParseException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.CronExpression;

import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.Sets;

import de.cosmocode.Holder;

/**
 * Tests {@link CronService}.
 *
 * @author Willi Schoenborn
 */
public final class CronServiceTest {

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
     * 
     * @throws ParseException should not happen 
     * @throws InterruptedException should not happen
     */
    @Test
    public void singleBinding() throws ParseException, InterruptedException {
        final TriggerBinding binding = EasyMock.createMock("binding", TriggerBinding.class);
        EasyMock.expect(binding.getExpression()).andReturn(new CronExpression("0/1 * * * * ?"));
        final Holder<Boolean> holder = Holder.of(Boolean.FALSE);
        EasyMock.expect(binding.getCommand()).andReturn(new Runnable() {
            
            @Override
            public void run() {
                holder.set(Boolean.TRUE);
            }   
            
        });
        
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        EasyMock.replay(binding);
        
        final Set<TriggerBinding> bindings = ImmutableSet.of(binding);
        
        unit(scheduler, bindings).initialize();
        EasyMock.verify(binding);
        Thread.sleep(1000);
        Assert.assertTrue(holder.get());
    }
    
}
