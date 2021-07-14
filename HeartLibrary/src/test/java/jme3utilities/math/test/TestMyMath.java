/*
 Copyright (c) 2020-2021, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.math.test;

import java.util.logging.Logger;
import jme3utilities.math.MyMath;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the MyMath class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestMyMath {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestMyMath.class.getName());
    // *************************************************************************
    // new methods exposed

    @Test
    public void testMyMath() {
        float zero = MyMath.hypotenuse();
        Assert.assertEquals(0f, zero, 0f);

        float thirteen = MyMath.hypotenuse(-5f, -12f);
        Assert.assertEquals(13f, thirteen, 0f);

        float four = MyMath.hypotenuse(-2f, 2f, 2f, -2f);
        Assert.assertEquals(4f, four, 0f);

        double five = MyMath.hypotenuseDouble(3.0, 4.0);
        Assert.assertEquals(5.0, five, 1e-15);

        float max1 = MyMath.max();
        Assert.assertEquals(Float.NEGATIVE_INFINITY, max1, 0f);

        float max2 = MyMath.max(-10f);
        Assert.assertEquals(-10f, max2, 0f);

        float max3 = MyMath.max(-4f, -12f, 3f);
        Assert.assertEquals(3f, max3, 0f);

        double max4 = MyMath.maxDouble();
        Assert.assertEquals(Double.NEGATIVE_INFINITY, max4, 0f);

        double max5 = MyMath.maxDouble(-10.0);
        Assert.assertEquals(-10.0, max5, 0.0);

        double max6 = MyMath.maxDouble(-4.0, -12.0, 3.0);
        Assert.assertEquals(3.0, max6, 0.0);

        float min1 = MyMath.min();
        Assert.assertEquals(Float.POSITIVE_INFINITY, min1, 0f);

        float min2 = MyMath.min(10f);
        Assert.assertEquals(10f, min2, 0f);

        float min3 = MyMath.min(-4f, -12f, 3f);
        Assert.assertEquals(-12f, min3, 0f);

        double min4 = MyMath.minDouble();
        Assert.assertEquals(Double.POSITIVE_INFINITY, min4, 0f);

        double min5 = MyMath.minDouble(10.0);
        Assert.assertEquals(10.0, min5, 0.0);

        double min6 = MyMath.minDouble(-4.0, -12.0, 3.0);
        Assert.assertEquals(-12.0, min6, 0.0);

        double zero2 = MyMath.sumOfSquares();
        Assert.assertEquals(0.0, zero2, 0.0);

        double four2 = MyMath.sumOfSquares(1f, -1f, -1f, -1f);
        Assert.assertEquals(4.0, four2, 0.0);

        double six = MyMath.sumOfSquares(1f, -1f, -2f);
        Assert.assertEquals(6.0, six, 0.0);
    }
}
