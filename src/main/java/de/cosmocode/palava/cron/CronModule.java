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

import java.lang.annotation.Annotation;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

/**
 * Abstract module which can be used to configure scheduling.
 *
 * @author Willi Schoenborn
 */
public abstract class CronModule extends AbstractModule {

    /**
     * First step of the scheduling binding expression.
     * 
     * @param type the binding target which should be scheduled
     * @return a binding builder used to configure the cron expression
     */
    protected final AnnotatedTriggerBindingBuilder schedule(Class<? extends Runnable> type) {
        return new InternalBuilder(Key.get(type));
    }

    /**
     * First step of the scheduling binding expression.
     * 
     * @param literal the binding target which should be scheduled
     * @return a binding builder used to configure the cron expression
     */
    protected final AnnotatedTriggerBindingBuilder schedule(TypeLiteral<? extends Runnable> literal) {
        return new InternalBuilder(Key.get(literal));
    }

    /**
     * First step of the scheduling binding expression.
     * 
     * @param key the binding target which should be scheduled
     * @return a binding builder used to configure the cron expression
     */
    protected final TriggerBindingBuilder schedule(Key<? extends Runnable> key) {
        return new InternalBuilder(key);
    }
    
    /**
     * Internal implementation of the {@link AnnotatedTriggerBindingBuilder} interface.
     *
     * @author Willi Schoenborn
     */
    private final class InternalBuilder implements AnnotatedTriggerBindingBuilder {
        
        private final Key<? extends Runnable> commandKey;

        public InternalBuilder(Key<? extends Runnable> commandKey) {
            this.commandKey = Preconditions.checkNotNull(commandKey, "CommandKey");
        }
        
        @Override
        public TriggerBindingBuilder annotatedWith(Annotation annotation) {
            return schedule(Key.get(commandKey.getTypeLiteral(), annotation));
        }
        
        @Override
        public TriggerBindingBuilder annotatedWith(Class<? extends Annotation> annotationType) {
            return schedule(Key.get(commandKey.getTypeLiteral(), annotationType));
        }
        
        @Override
        public void using(String expression) {
            final Provider<? extends Runnable> provider = binder().getProvider(commandKey);
            final TriggerBinding binding = TriggerBindings.of(provider, expression);
            bind(binding);
        }
        
        @Override
        public void using(Annotation annotation) {
            using(Key.get(String.class, annotation));
        }
        
        @Override
        public void using(Class<? extends Annotation> annotationType) {
            using(Key.get(String.class, annotationType));
        }
        
        private void using(Key<? extends String> expressionKey) {
            final Provider<? extends Runnable> command = binder().getProvider(commandKey);
            final Provider<? extends String> expression = binder().getProvider(expressionKey);
            final TriggerBinding binding = TriggerBindings.of(command, expression);
            bind(binding);
        }
        
        private void bind(TriggerBinding binding) {
            Multibinder.newSetBinder(binder(), TriggerBinding.class).addBinding().toInstance(binding);
        }
        
    }
    
}
