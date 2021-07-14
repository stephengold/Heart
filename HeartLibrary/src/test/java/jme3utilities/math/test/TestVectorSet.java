/*
 Copyright (c) 2019-2021, Stephen Gold
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

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.nio.FloatBuffer;
import java.util.logging.Logger;
import jme3utilities.math.MyVector3f;
import jme3utilities.math.VectorSet;
import jme3utilities.math.VectorSetUsingBuffer;
import jme3utilities.math.VectorSetUsingCollection;
import org.junit.Test;

/**
 * Test the VectorSet classes.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestVectorSet {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestVectorSet.class.getName());
    /**
     * test data
     */
    final private static Vector3f[] testData = new Vector3f[]{
        new Vector3f(3f, 0f, -4f),
        new Vector3f(1f, 0f, 0f),
        new Vector3f(3f, 0f, -4f),
        new Vector3f(Float.POSITIVE_INFINITY, 0f, 0f)
    };
    // *************************************************************************
    // new methods exposed

    @Test
    public void testVectorSet() {
        VectorSetUsingCollection vectorSet1 = new VectorSetUsingCollection(1);
        test(vectorSet1, true);
        vectorSet1.clear();
        test(vectorSet1, true);

        VectorSetUsingBuffer vectorSet2 = new VectorSetUsingBuffer(1, true);
        test(vectorSet2, true);
        vectorSet2.clear();
        test(vectorSet2, true);

        VectorSetUsingBuffer vectorSet3 = new VectorSetUsingBuffer(1, false);
        test(vectorSet3, true);
        vectorSet3.clear();
        test(vectorSet3, false);
        vectorSet3.clear();
        test(vectorSet3, true);
    }
    // *************************************************************************
    // private methods

    /**
     * Test the specified VectorSet instance.
     */
    private static void test(VectorSet vectorSet, boolean toBuffer) {
        float maxLength;
        String string;
        Vector3f max = new Vector3f();
        Vector3f mean = new Vector3f();
        Vector3f min = new Vector3f();

        for (Vector3f v : testData) {
            assert !vectorSet.contains(v);
        }
        maxLength = vectorSet.maxLength();
        assert maxLength == 0f;
        vectorSet.maxMin(max, min);
        assert max.x == Float.NEGATIVE_INFINITY;
        assert max.y == Float.NEGATIVE_INFINITY;
        assert max.z == Float.NEGATIVE_INFINITY;
        assert min.x == Float.POSITIVE_INFINITY;
        assert min.y == Float.POSITIVE_INFINITY;
        assert min.z == Float.POSITIVE_INFINITY;
        assert vectorSet.numVectors() == 0;
        string = vectorSet.toString();
        assert string != null;
        assert !string.isEmpty();
        /*
         * Add the first vector.
         */
        vectorSet.add(testData[0]);
        for (Vector3f v : testData) {
            if (v.equals(testData[0])) {
                assert vectorSet.contains(v);
            } else {
                assert !vectorSet.contains(v);
            }
        }
        maxLength = vectorSet.maxLength();
        assert FastMath.approximateEquals(maxLength, 5f);
        vectorSet.maxMin(max, min);
        assert max.equals(testData[0]);
        assert min.equals(testData[0]);
        vectorSet.mean(mean);
        assert mean.equals(testData[0]);
        assert vectorSet.numVectors() == 1;
        string = vectorSet.toString();
        assert string != null;
        assert !string.isEmpty();
        /*
         * Add the 2nd vector.
         */
        vectorSet.add(testData[1]);
        for (Vector3f v : testData) {
            if (v.equals(testData[0]) || v.equals(testData[1])) {
                assert vectorSet.contains(v);
            } else {
                assert !vectorSet.contains(v);
            }
        }
        maxLength = vectorSet.maxLength();
        assert FastMath.approximateEquals(maxLength, 5f);
        vectorSet.maxMin(max, min);
        assert max.equals(new Vector3f(3f, 0f, 0f));
        assert min.equals(new Vector3f(1f, 0f, -4f));
        vectorSet.mean(mean);
        assert mean.equals(new Vector3f(2f, 0f, -2f));
        assert vectorSet.numVectors() == 2;
        string = vectorSet.toString();
        assert string != null;
        assert !string.isEmpty();
        /*
         * Add the 3rd vector.
         */
        vectorSet.add(testData[2]);
        for (Vector3f v : testData) {
            if (v.equals(testData[0]) || v.equals(testData[1])) {
                assert vectorSet.contains(v);
            } else {
                assert !vectorSet.contains(v);
            }
        }
        vectorSet.covariance(null);
        maxLength = vectorSet.maxLength();
        assert FastMath.approximateEquals(maxLength, 5f);
        vectorSet.maxMin(max, min);
        assert max.equals(new Vector3f(3f, 0f, 0f));
        assert min.equals(new Vector3f(1f, 0f, -4f));
        vectorSet.mean(mean);
        assert mean.equals(new Vector3f(2f, 0f, -2f));
        assert vectorSet.numVectors() == 2;
        string = vectorSet.toString();
        assert string != null;
        assert !string.isEmpty();
        /*
         * Convert to arrays.
         */
        float[] fa = vectorSet.toFloatArray();
        assert fa != null;
        assert fa.length == 2 * MyVector3f.numAxes;
        Vector3f[] va = vectorSet.toVectorArray();
        assert va != null;
        assert va.length == 2;
        /*
         * Add the 4th vector.
         */
        vectorSet.add(testData[3]);
        for (Vector3f v : testData) {
            if (v.equals(testData[0]) || v.equals(testData[1])) {
                assert vectorSet.contains(v);
            }
        }
        vectorSet.covariance(null);
        maxLength = vectorSet.maxLength();
        assert maxLength == Float.POSITIVE_INFINITY;
        vectorSet.maxMin(max, min);
        assert max.equals(new Vector3f(Float.POSITIVE_INFINITY, 0f, 0f));
        assert min.equals(new Vector3f(1f, 0f, -4f));
        vectorSet.mean(mean);
        assert mean.x == Float.POSITIVE_INFINITY;
        assert mean.y == 0f;
        assert vectorSet.numVectors() == 3;
        string = vectorSet.toString();
        assert string != null;
        assert !string.isEmpty();

        vectorSet.toFloatArray();
        vectorSet.toVectorArray();

        if (toBuffer) {
            /*
             * Convert to a FloatBuffer.
             */
            FloatBuffer buffer = vectorSet.toBuffer();
            assert buffer != null;
            assert buffer.limit() == 3 * MyVector3f.numAxes;
        }
    }
}
