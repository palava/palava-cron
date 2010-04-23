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
public interface AnnotatedTriggerBindingBuilder extends TriggerBindingBuilder {

    /**
     * Adds the specified annotation type to the configured binding target.
     * 
     * @param annotation the binding annotation
     * @return a builder which is used to configure the cron expression
     */
    TriggerBindingBuilder annotatedWith(Annotation annotation);

    /**
     * Adds the specified annotation type to the configured binding target.
     * 
     * @param annotationType the binding annotation type
     * @return a builder which is used to configure the cron expression
     */
    TriggerBindingBuilder annotatedWith(Class<? extends Annotation> annotationType);
    
}
