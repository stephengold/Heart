/*
 Copyright (c) 2020-2023, Stephen Gold
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

import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyQuaternion;
import jme3utilities.test.HeartTest;
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

    /**
     * Test combine().
     */
    @Test
    public void testCombine() {
        Vector3f[] testVectors = {
            new Vector3f(1f, 2f, 3f),
            new Vector3f(0f, -1f, -2f),
            new Vector3f(7f, 0.7f, 0f)
        };
        Quaternion[] testQuaternions = {
            new Quaternion(1f, 7f, 1f, -7f),
            new Quaternion(3f, -4f, 0f, 0f),
            new Quaternion(0f, 0f, 0f, 1f),
            new Quaternion(0f, 0f, -1f, 1f),
            new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f)
        };

        float tol = 4e-6f;

        for (Vector3f pt : testVectors) {
            for (Quaternion pr : testQuaternions) {
                for (Vector3f ps : testVectors) {
                    for (Vector3f ct : testVectors) {
                        for (Quaternion cr : testQuaternions) {
                            for (Vector3f cs : testVectors) {
                                Quaternion prn = pr.clone();
                                MyQuaternion.normalizeLocal(prn);
                                Quaternion crn = cr.clone();
                                MyQuaternion.normalizeLocal(crn);

                                Transform nChild = new Transform(ct, crn, cs);
                                Transform nParent = new Transform(pt, prn, ps);
                                Transform expect
                                        = nChild.combineWithParent(nParent);

                                Transform child = new Transform(ct, cr, cs);
                                Transform parent = new Transform(pt, pr, ps);
                                Transform saveChild = child.clone();
                                Transform saveParent = parent.clone();

                                Transform actual1
                                        = MyMath.combine(child, parent, null);
                                HeartTest.assertEquals(expect, actual1, tol);
                                HeartTest.assertEquals(saveChild, child, 0f);
                                HeartTest.assertEquals(saveParent, parent, 0f);

                                Transform actual2
                                        = MyMath.combine(child, parent, child);
                                HeartTest.assertEquals(expect, actual2, tol);
                                HeartTest.assertEquals(expect, child, tol);
                                HeartTest.assertEquals(saveParent, parent, 0f);

                                child = saveChild.clone();
                                Transform actual3
                                        = MyMath.combine(child, parent, parent);
                                HeartTest.assertEquals(expect, actual3, tol);
                                HeartTest.assertEquals(saveChild, child, 0f);
                                HeartTest.assertEquals(expect, parent, tol);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Test cube(), cubeRoot(), and modulo().
     */
    @Test
    public void testMy1() {
        Assert.assertEquals(0f, MyMath.cube(0f), 0f);
        Assert.assertEquals(-27f, MyMath.cube(-3f), 0f);
        Assert.assertEquals(512f, MyMath.cube(8f), 0f);
        Assert.assertEquals(
                Float.POSITIVE_INFINITY, MyMath.cube(Float.MAX_VALUE / 2f), 0f);

        Assert.assertEquals(0f, MyMath.cubeRoot(0f), 0f);
        Assert.assertEquals(-1.44225f, MyMath.cubeRoot(-3f), 1e-6f);
        Assert.assertEquals(2f, MyMath.cubeRoot(8f), 0f);
        Assert.assertEquals(
                5.5411915e12f, MyMath.cubeRoot(Float.MAX_VALUE / 2f), 1e6f);

        Assert.assertEquals(0, MyMath.modulo(0, 2));
        Assert.assertEquals(1, MyMath.modulo(1, 3));
        Assert.assertEquals(3, MyMath.modulo(-1, 4));
        Assert.assertEquals(2, MyMath.modulo(12, 5), 0f);

        Assert.assertEquals(0f, MyMath.modulo(0f, 2f), 0f);
        Assert.assertEquals(1f, MyMath.modulo(1f, 3f), 0f);
        Assert.assertEquals(3f, MyMath.modulo(-1f, 4f), 0f);
        Assert.assertEquals(1.5f, MyMath.modulo(11.5f, 5f), 0f);

        Assert.assertEquals(0.0, MyMath.modulo(0.0, 2.0), 0.0);
        Assert.assertEquals(1.0, MyMath.modulo(1.0, 3.0), 0.0);
        Assert.assertEquals(3.0, MyMath.modulo(-1.0, 4.0), 0.0);
        Assert.assertEquals(1.5, MyMath.modulo(11.5, 5.0), 0.0);
    }

    /**
     * Test lerp() and lerp3().
     */
    @Test
    public void testMy2() {
        Assert.assertEquals(1f, MyMath.lerp(0f, 1f, 2f), 0f);
        Assert.assertEquals(2f, MyMath.lerp(1f, 1f, 2f), 0f);
        Assert.assertEquals(1.5f, MyMath.lerp(0.5f, 0.2f, 2.8f), 0f);
        Assert.assertEquals(-3f, MyMath.lerp(2f, 1f, -1f), 0f);

        Assert.assertEquals(3f, MyMath.lerp3(0f, 0f, 3f, 4f, 2f), 0f);
        Assert.assertEquals(3.5f, MyMath.lerp3(1f, 0.5f, 3f, 4f, 2f), 0f);
    }

    /**
     * Test hypotenuse(), hypotenuseDouble(), max(), maxDouble(), min(),
     * minDouble(), and sumOfSquares().
     */
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

    /**
     * Test fromAngles().
     */
    @Test
    public void testFromAngles() {
        Vector3f in = new Vector3f(4f, 6f, 9f); // test vector, never modified
        Vector3f saveIn = in.clone();

        // Three arbitrary rotation angles between -PI/2 and +PI/2
        final float xAngle = 1.23f;
        final float yAngle = 0.765f;
        final float zAngle = -0.456f;
        /*
         * Part 1: verify that the extrinsic rotation order is x-z-y
         *
         * Apply extrinsic rotations to the "in" vector in x-z-y order.
         */
        Matrix3f rx = new Matrix3f();
        rx.fromAngleAxis(xAngle, Vector3f.UNIT_X);
        Matrix3f ry = new Matrix3f();
        ry.fromAngleAxis(yAngle, Vector3f.UNIT_Y);
        Matrix3f rz = new Matrix3f();
        rz.fromAngleAxis(zAngle, Vector3f.UNIT_Z);
        Vector3f outXzy = rx.mult(in);
        rz.mult(outXzy, outXzy);
        ry.mult(outXzy, outXzy);
        /*
         * Construct a Matrix3f using fromAngles(float, float, float),
         * use it to rotate the "in" vector, and compare.
         */
        Matrix3f r1 = MyMath.fromAngles(xAngle, yAngle, zAngle, null);
        Vector3f out1 = r1.mult(in);
        HeartTest.assertEquals(outXzy, out1, 1e-5f);
        /*
         * Part 2: verify intrinsic rotation order
         *
         * Apply intrinsic rotations to the "in" vector in y-z'-x" order.
         */
        Matrix3f r4 = ry.mult(rz).mult(rx);
        Vector3f out7 = r4.mult(in);
        HeartTest.assertEquals(outXzy, out7, 1e-5f);

        // Verify that the value of "in" hasn't changed.
        HeartTest.assertEquals(saveIn, in, 0f);
    }

    /**
     * Test transform().
     */
    @Test
    public void testTransform() {
        Vector3f[] testVectors = {
            new Vector3f(1f, 2f, 3f),
            new Vector3f(0f, -1f, -2f),
            new Vector3f(1.7f, 0.7f, 0f)
        };
        Quaternion[] testQuaternions = {
            new Quaternion(1f, 7f, 1f, -7f),
            new Quaternion(3f, -4f, 0f, 0f),
            new Quaternion(0f, 0f, 0f, 1f),
            new Quaternion(0f, 0f, -1f, 1f),
            new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f)
        };

        float tol = 1e-6f;

        for (Vector3f t : testVectors) {
            for (Quaternion r : testQuaternions) {
                for (Vector3f s : testVectors) {
                    for (Vector3f v : testVectors) {
                        Quaternion rn = r.clone();
                        MyQuaternion.normalizeLocal(rn);
                        Transform nTransform = new Transform(t, rn, s);
                        Vector3f expect = nTransform.transformVector(v, null);

                        Transform transform = new Transform(t, r, s);
                        Transform saveTransform = transform.clone();
                        Vector3f saveV = v.clone();

                        Vector3f actual1 = MyMath.transform(transform, v, null);
                        HeartTest.assertEquals(expect, actual1, tol);
                        HeartTest.assertEquals(saveV, v, 0f); // unaffected
                        HeartTest.assertEquals(saveTransform, transform, 0f);

                        Vector3f w = v.clone();
                        Vector3f actual2 = MyMath.transform(transform, v, w);
                        HeartTest.assertEquals(expect, actual2, tol);
                        HeartTest.assertEquals(saveTransform, transform, 0f);
                        HeartTest.assertEquals(saveV, v, 0f); // unaffected
                        HeartTest.assertEquals(expect, w, tol);
                    }
                }
            }
        }
    }

    /**
     * Test transformInverse(Vector3f).
     */
    @Test
    public void testTransformInverse() {
        Vector3f[] testVectors = {
            new Vector3f(1f, 2f, 3f),
            new Vector3f(0f, -1f, -2f),
            new Vector3f(1.7f, 0.7f, 0f)
        };
        Quaternion[] testQuaternions = {
            new Quaternion(1f, 7f, 1f, -7f),
            new Quaternion(3f, -4f, 0f, 0f),
            new Quaternion(0f, 0f, 0f, 1f),
            new Quaternion(0f, 0f, -1f, 1f),
            new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f)
        };

        float tol = 1e-6f;

        for (Vector3f t : testVectors) {
            for (Quaternion r : testQuaternions) {
                for (Vector3f s : testVectors) {
                    for (Vector3f v : testVectors) {
                        Quaternion rn = r.clone();
                        MyQuaternion.normalizeLocal(rn);
                        Transform nTransform = new Transform(t, rn, s);
                        Vector3f expect
                                = nTransform.transformInverseVector(v, null);

                        Transform transform = new Transform(t, r, s);
                        Transform saveTransform = transform.clone();
                        Vector3f saveV = v.clone();

                        Vector3f actual1
                                = MyMath.transformInverse(transform, v, null);
                        HeartTest.assertEquals(expect, actual1, tol);
                        HeartTest.assertEquals(saveV, v, 0f); // unaffected
                        HeartTest.assertEquals(saveTransform, transform, 0f);

                        Vector3f w = v.clone();
                        Vector3f actual2
                                = MyMath.transformInverse(transform, v, w);
                        HeartTest.assertEquals(expect, actual2, tol);
                        HeartTest.assertEquals(saveTransform, transform, 0f);
                        HeartTest.assertEquals(saveV, v, 0f); // unaffected
                        HeartTest.assertEquals(expect, w, tol);
                    }
                }
            }
        }
    }
}
