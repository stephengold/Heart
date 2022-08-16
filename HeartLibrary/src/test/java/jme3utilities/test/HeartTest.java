/*
 Copyright (c) 2021-2022, Stephen Gold
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
package jme3utilities.test;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import jme3utilities.math.ReadXZ;
import org.junit.Assert;

/**
 * Utility methods for automated testing of the Heart Library.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class HeartTest {
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private HeartTest() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Verify that 2 quaternions are equal to within some tolerance.
     *
     * @param x the expected X component
     * @param y the expected Y component
     * @param z the expected Z component
     * @param w the expected W component
     * @param actual the Quaternion to test (not null, unaffected)
     * @param tolerance the allowable difference for each component
     */
    public static void assertEquals(float x, float y, float z, float w,
            Quaternion actual, float tolerance) {
        Assert.assertEquals(x, actual.getX(), tolerance);
        Assert.assertEquals(y, actual.getY(), tolerance);
        Assert.assertEquals(z, actual.getZ(), tolerance);
        Assert.assertEquals(w, actual.getW(), tolerance);
    }

    /**
     * Verify that 2 vectors are equal to within some tolerance.
     *
     * @param x the expected X component
     * @param y the expected Y component
     * @param z the expected Z component
     * @param actual the vector to test (not null, unaffected)
     * @param tolerance the allowable difference for each component
     */
    public static void assertEquals(float x, float y, float z, Vector3f actual,
            float tolerance) {
        Assert.assertEquals(x, actual.x, tolerance);
        Assert.assertEquals(y, actual.y, tolerance);
        Assert.assertEquals(z, actual.z, tolerance);
    }

    /**
     * Verify that 2 vectors are equal to within some tolerance.
     *
     * @param expected the expected value (not null, unaffected)
     * @param actual the vector to test (not null, unaffected)
     * @param tolerance the allowable difference for each component
     */
    public static void assertEquals(Vector3f expected, Vector3f actual,
            float tolerance) {
        assertEquals(expected.x, expected.y, expected.z, actual, tolerance);
    }

    /**
     * Verify that 2 quaternions are equal to within some tolerance.
     *
     * @param expected the expected value (not null, unaffected)
     * @param actual the vector to test (not null, unaffected)
     * @param tolerance the allowable difference for each component
     */
    public static void assertEquals(Quaternion expected, Quaternion actual,
            float tolerance) {
        assertEquals(expected.getX(), expected.getY(), expected.getZ(),
                expected.getW(), actual, tolerance);
    }

    /**
     * Verify that 2 X-Z vectors are equal to within some tolerance.
     *
     * @param expected the expected value (not null)
     * @param actual the vector to test (not null)
     * @param tolerance the allowable difference for each component
     */
    public static void assertEquals(ReadXZ expected, ReadXZ actual,
            float tolerance) {
        Assert.assertEquals(expected.getX(), actual.getX(), tolerance);
        Assert.assertEquals(expected.getZ(), actual.getZ(), tolerance);
    }
}
