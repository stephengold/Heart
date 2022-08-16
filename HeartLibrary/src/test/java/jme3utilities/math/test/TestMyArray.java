/*
 Copyright (c) 2019-2022, Stephen Gold
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

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.math.MyArray;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the MyArray class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestMyArray {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestMyArray.class.getName());
    /**
     * test data
     */
    final private static Vector3f[] testData = {
        new Vector3f(3f, 0f, -4f),
        new Vector3f(1f, 0f, 0f),
        new Vector3f(2f, 0f, -4f),
        new Vector3f(-7f, 0f, 0f)
    };
    // *************************************************************************
    // new methods exposed

    /**
     * Test getMax() and getMin().
     */
    @Test
    public void testMyArray() {
        BoundingBox box1 = MyArray.aabb(testData, null);
        assert box1.getMin(null).x == -7f;
        assert box1.getMin(null).y == 0f;
        assert box1.getMin(null).z == -4f;
        assert box1.getMax(null).x == 3f;
        assert box1.getMax(null).y == 0f;
        assert box1.getMax(null).z == 0f;

        BoundingBox box2 = new BoundingBox();
        BoundingBox result = MyArray.aabb(testData, box2);
        assert result == box2;
        assert box2.getMin(null).x == -7f;
        assert box2.getMin(null).y == 0f;
        assert box2.getMin(null).z == -4f;
        assert box2.getMax(null).x == 3f;
        assert box2.getMax(null).y == 0f;
        assert box2.getMax(null).z == 0f;
    }

    /**
     * Test the countNeSorted() method.
     */
    @Test
    public void testCountNeSorted() {
        int count = MyArray.countNeSorted();
        Assert.assertEquals(0, count);

        count = MyArray.countNeSorted(0.6f);
        Assert.assertEquals(1, count);

        count = MyArray.countNeSorted(1f, 4f, 5f);
        Assert.assertEquals(3, count);

        count = MyArray.countNeSorted(1f, 1f, 4f, 5f);
        Assert.assertEquals(3, count);

        count = MyArray.countNeSorted(-0f, 0f);
        Assert.assertEquals(1, count);

        count = MyArray.countNeSorted(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE,
                0f, Float.MIN_VALUE, Float.MAX_VALUE, Float.POSITIVE_INFINITY);
        Assert.assertEquals(6, count);

        count = MyArray.countNeSorted(Float.NaN);
        Assert.assertEquals(1, count);
    }
}
