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

/**
 * A simple service to schedule tasks in a cron-lile fashion.
 *
 * @since 1.2
 * @author Willi Schoenborn
 */
public interface CronService {

    /**
     * Schedules the given task using the specified cron expression.
     *
     * @since 1.2
     * @param task the task to be executed
     * @param expression the cron expression
     * @throws IllegalArgumentException if expression is no valid cron expression
     */
    void schedule(Runnable task, String expression);
    
}
