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

import org.quartz.CronExpression;

/**
 * A trigger binding is a simple value object
 * keeping track of a {@link Runnable} and a cron
 * expression. Using both values to configure
 * a scheduling is left to implementations.
 *
 * @author Willi Schoenborn
 */
public interface TriggerBinding {

    /**
     * Provides the command assocatiated with this binding.
     * 
     * @return the {@link Runnable}
     */
    Runnable getCommand();
    
    /**
     * Provides the cron expression associated with this binding.
     * 
     * @return the expression
     */
    CronExpression getExpression();
    
}
