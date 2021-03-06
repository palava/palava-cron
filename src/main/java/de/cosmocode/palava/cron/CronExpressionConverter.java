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

import java.text.ParseException;

import org.quartz.CronExpression;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

/**
 * {@link TypeConverter} for {@link CronExpression}s.
 *
 * @author Willi Schoenborn
 */
public final class CronExpressionConverter implements TypeConverter {

    public static final TypeLiteral<CronExpression> LITERAL = TypeLiteral.get(CronExpression.class);
    
    @Override
    public CronExpression convert(String value, TypeLiteral<?> toType) {
        try {
            return new CronExpression(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
}
