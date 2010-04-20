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

    protected final AnnotatedTriggerBindingBuilder schedule(Class<? extends Runnable> type) {
        return schedule(Key.get(type));
    }
    
    protected final AnnotatedTriggerBindingBuilder schedule(TypeLiteral<? extends Runnable> literal) {
        return schedule(Key.get(literal));
    }
    
    protected final AnnotatedTriggerBindingBuilder schedule(Key<? extends Runnable> key) {
        return new InternalBuilder(key);
    }
    
    private final class InternalBuilder implements AnnotatedTriggerBindingBuilder {
        
        private final Key<? extends Runnable> key;

        public InternalBuilder(Key<? extends Runnable> key) {
            this.key = Preconditions.checkNotNull(key, "Key");
        }
        
        @Override
        public TriggerBindingBuilder annotatedWith(Annotation annotation) {
            return schedule(Key.get(key.getTypeLiteral(), annotation));
        }
        
        @Override
        public TriggerBindingBuilder annotatedWith(Class<? extends Annotation> annotationType) {
            return schedule(Key.get(key.getTypeLiteral(), annotationType));
        }
        
        @Override
        public void using(String expression) {
            final Provider<? extends Runnable> provider = binder().getProvider(key);
            final TriggerBinding binding = new TriggerBinding.Builder(provider).withExpression(expression);
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
            final Provider<? extends Runnable> provider = binder().getProvider(key);
            final Provider<? extends String> expressionProvider = binder().getProvider(expressionKey);
            final TriggerBinding binding = new TriggerBinding.Builder(provider).withExpression(expressionProvider);
            bind(binding);
        }
        
        private void bind(TriggerBinding binding) {
            Multibinder.newSetBinder(binder(), TriggerBinding.class).addBinding().toInstance(binding);
        }
        
    }
    
}
