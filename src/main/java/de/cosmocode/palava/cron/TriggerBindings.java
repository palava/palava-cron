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

import com.google.inject.Provider;

/**
 * Static utility class for {@link TriggerBinding}s.
 *
 * @author Willi Schoenborn
 */
final class TriggerBindings {

    private TriggerBindings() {
        
    }

    /**
     * Constructs a new {@link TriggerBinding} using the specified providers.
     * 
     * @param command the provider for the command
     * @param expression the provider for the cron expression
     * @return a {@link TriggerBinding} which delegates to the specified provides
     *         when requested
     */
    public static TriggerBinding of(final Provider<? extends Runnable> command, 
        final Provider<? extends String> expression) {
        return new TriggerBinding() {
            
            public Runnable getCommand() {
                return command.get();
            };
            
            @Override
            public String getExpression() {
                return expression.get();
            }
            
        };
    }
    
    /**
     * Constructs a new {@link TriggerBinding} using the specified provider and
     * cron expression.
     * 
     * @param command the provider for the command
     * @param expression the cron expression
     * @return a {@link TriggerBinding} which delegates to the specified provider
     *         when requested
     */
    public static TriggerBinding of(final Provider<? extends Runnable> command, final String expression) {
        return new TriggerBinding() {
            
            @Override
            public Runnable getCommand() {
                return command.get();
            }
            
            @Override
            public String getExpression() {
                return expression;
            }
            
        };
    }
    
}
