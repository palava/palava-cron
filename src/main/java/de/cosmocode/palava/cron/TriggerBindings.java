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

import com.google.common.base.Preconditions;
import com.google.inject.Provider;

/**
 * Static utility class for {@link TriggerBinding}s.
 *
 * @author Willi Schoenborn
 */
final class TriggerBindings {

    private static final CronExpressionConverter CONVERTER = new CronExpressionConverter(); 
    
    private TriggerBindings() {
        
    }

    /**
     * Creates a new {@link TriggerBinding} using the specified providers.
     * 
     * @param command the provider for the command
     * @param expression the provider for the cron expression
     * @return a {@link TriggerBinding} which delegates to the specified provides
     *         when requested
     * @throws NullPointerException if command or expression is null
     */
    public static TriggerBinding of(final Provider<? extends Runnable> command, 
        final Provider<? extends CronExpression> expression) {
        Preconditions.checkNotNull(command, "Command");
        Preconditions.checkNotNull(expression, "Expression");        
        return new TriggerBinding() {

            @Override
            public Runnable getCommand() {
                return command.get();
            }
            
            @Override
            public CronExpression getExpression() {
                return expression.get();
            }
            
        };
    }
    
    /**
     * Creates a new {@link TriggerBinding} using the specified provider and
     * cron expression.
     * 
     * @param command the provider for the command
     * @param expression the cron expression
     * @return a {@link TriggerBinding} which delegates to the specified provider
     *         when requested
     * @throws NullPointerException if command or expression is null
     */
    public static TriggerBinding of(final Provider<? extends Runnable> command, final String expression) {
        Preconditions.checkNotNull(command, "Command");
        Preconditions.checkNotNull(expression, "Expression");
        return new TriggerBinding() {
            
            @Override
            public Runnable getCommand() {
                return command.get();
            }
            
            @Override
            public CronExpression getExpression() {
                return CONVERTER.convert(expression, CronExpressionConverter.LITERAL);
            }
            
        };
    }
    
    /**
     * Creates a new {@link TriggerBinding} using the specified provider and cron expression.
     * 
     * @param command the provider for the command
     * @param expression the cron expression
     * @return a {@link TriggerBinding} which delegates to the specified provider
     *         when requested
     * @throws NullPointerException if command or expression is null
     */
    public static TriggerBinding of(final Provider<? extends Runnable> command, final CronExpression expression) {
        Preconditions.checkNotNull(command, "Command");
        Preconditions.checkNotNull(expression, "Expression");
        return new TriggerBinding() {
            
            @Override
            public Runnable getCommand() {
                return command.get();
            }
            
            @Override
            public CronExpression getExpression() {
                return expression;
            }
            
        };
    }
    
}
