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

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Simple dummy implementation of the {@link CronModule} to verify code beautiness ;).
 *
 * @author Willi Schoenborn
 */
public final class SimpleCronModule extends CronModule {

    @Override
    protected void configure() {
        schedule(Runnable.class).using("* 10 15 * * * 2010");
        schedule(Key.get(Runnable.class)).using(Names.named("my.cronExpression"));
        schedule(TypeLiteral.get(Runnable.class)).annotatedWith(Deprecated.class).using(Override.class);
    }

}
