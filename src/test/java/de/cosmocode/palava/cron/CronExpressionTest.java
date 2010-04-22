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
