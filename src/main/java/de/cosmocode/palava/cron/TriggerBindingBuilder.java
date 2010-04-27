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

import java.lang.annotation.Annotation;

/**
 * A builder which is part of the EDSL provided
 * by the {@link CronModule}.
 *
 * @author Willi Schoenborn
 */
public interface TriggerBindingBuilder {

    /**
     * Schedules the configured binding target using the specified
     * cron expression.
     * 
     * @param expression the cron expression
     * @throws NullPointerException if expression is null
     */
    void using(String expression);

    /**
     * Schedules the configured binding target using the cron expression
     * bound with the specified annotation.
     * 
     * @param annotation the binding annotation for the cron expression
     * @throws NullPointerException if annotation is null
     */
    void using(Annotation annotation);

    /**
     * Schedules the configured binding target using the cron expression
     * bound with the specified annotation type.
     * 
     * @param annotationType the binding annotation type for the cron expression
     * @throws NullPointerException if annotationType is null
     */
    void using(Class<? extends Annotation> annotationType);
    
}
