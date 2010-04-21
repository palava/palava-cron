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
     */
    void using(String expression);

    /**
     * Schedules the configured binding target using the cron expression
     * bound with the specified annotation.
     * 
     * @param annotation the binding annotation for the cron expression
     */
    void using(Annotation annotation);

    /**
     * Schedules the configured binding target using the cron expression
     * bound with the specified annotation type.
     * 
     * @param annotationType the binding annotation type for the cron expression
     */
    void using(Class<? extends Annotation> annotationType);
    
}
