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
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Tests {@link CronExpression}.
 *
 * @author Willi Schoenborn
 */
public final class CronExpressionTest implements Function<String, CronExpression> {

    private static final Logger LOG = LoggerFactory.getLogger(CronExpressionTest.class);
    
    @Override
    public CronExpression apply(String from) {
        try {
            return new CronExpression(from);
        } catch (ParseException e) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Tests a valid cron expression in the future.
     */
    @Test
    public void validFuture() {
        final CronExpression unit = apply("0 15 10 15 * ?");
        final Date now = new Date();
        final Date next = unit.getNextValidTimeAfter(now);
        LOG.debug("Next date is {}", next);
        Assert.assertTrue(next.after(now));
    }
    
    /**
     * Tests a valid cron expression in the past.
     */
    @Test
    public void validPast() {
        final CronExpression unit = apply("0 0 10 * * ? 1900");
        final Date now = new Date();
        Assert.assertNull(unit.getNextValidTimeAfter(now));
    }
    
    /**
     * Tests an invalid cron expression.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid() {
        apply("");
    }
    
}
