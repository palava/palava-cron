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

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * 
 *
 * @author Willi Schoenborn
 */
interface TriggerBinding {

    Runnable getRunnable();
    
    String getExpression();
    
    public static final class Builder {
        
        private final Provider<? extends Runnable> runnable;

        public Builder(Provider<? extends Runnable> runnable) {
            this.runnable = Preconditions.checkNotNull(runnable, "Runnable");
        }

        public TriggerBinding withExpression(final String expression) {
            return new TriggerBinding() {
                
                @Override
                public Runnable getRunnable() {
                    return runnable.get();
                }
                
                @Override
                public String getExpression() {
                    return expression;
                }
                
            };
        }
        
        public TriggerBinding withExpression(final Provider<? extends String> expression) {
            return new TriggerBinding() {
                
                @Override
                public Runnable getRunnable() {
                    return runnable.get();
                }
                
                @Override
                public String getExpression() {
                    return expression.get();
                }
                
            };
        }
        
    }
    
}
