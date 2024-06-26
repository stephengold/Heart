/*
 Copyright (c) 2013-2024 Stephen Gold
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
package jme3utilities.math;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import java.util.logging.Logger;
import jme3utilities.MyMesh;
import jme3utilities.Validate;

/**
 * Mathematical utility methods.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class MyMath {
    // *************************************************************************
    // constants and loggers

    /**
     * Pi/2
     *
     * @see com.jme3.math.FastMath#HALF_PI
     */
    final public static double halfPi = Math.PI / 2.0;
    /**
     * golden ratio = 1.618...
     */
    final public static float phi = (1f + FastMath.sqrt(5f)) / 2f;
    /**
     * square root of 2
     */
    final public static float root2 = FastMath.sqrt(2f);
    /**
     * square root of 1/2
     */
    final public static float rootHalf = FastMath.sqrt(0.5f);
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(MyMath.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private MyMath() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the (one-sided) area of the specified triangle.
     *
     * @param triangle (not null, unaffected)
     * @return the area (&ge;0)
     */
    public static double area(Triangle triangle) {
        Vector3f a = triangle.get1();
        Vector3f b = triangle.get2();
        Vector3f c = triangle.get3();

        Vector3f ab = b.subtract(a); // TODO garbage
        Vector3f ac = c.subtract(a);

        Vector3f cross = ab.cross(ac); // TODO garbage
        double areaSquared = MyVector3f.lengthSquared(cross) / 4.0;
        double area = Math.sqrt(areaSquared);

        return area;
    }

    /**
     * Test whether 2 float values are within the specified relative tolerance
     * of one another.
     *
     * @param a the first input value
     * @param b the 2nd input value
     * @param relativeTolerance the relative tolerance (&ge;0, 0.02 &rarr; 2
     * percent)
     * @return true if {@code a} and {@code b} differ by less than
     * {@code relativeTolerance * max(abs(a), abs(b))}, otherwise false
     */
    public static boolean areWithinTolerance(
            float a, float b, float relativeTolerance) {
        Validate.nonNegative(relativeTolerance, "relative tolerance");

        if (a == b) {
            return true;
        }

        float maxAbsoluteValue = Math.max(FastMath.abs(a), FastMath.abs(b));
        float tolerance = relativeTolerance * maxAbsoluteValue;
        float absDiff = FastMath.abs(a - b);
        if (absDiff < tolerance) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the circle function {@code sqrt(1 - x^2)} for a double-precision
     * value.
     *
     * @param abscissa input (&le;1, &ge;-1)
     * @return positive ordinate of the unit circle at the abscissa (&le;1,
     * &ge;0)
     */
    public static double circle(double abscissa) {
        assert Validate.inRange(abscissa, "abscissa", -1.0, 1.0);

        double y = Math.sqrt(1.0 - abscissa * abscissa);

        assert y >= 0.0 : y;
        assert y <= 1.0 : y;
        return y;
    }

    /**
     * Return the circle function {@code sqrt(1 - x^2)} for a single-precision
     * value. Double-precision arithmetic is used to reduce the risk of
     * overflow.
     *
     * @param abscissa input (&le;1, &ge;-1)
     * @return positive ordinate of the unit circle at the abscissa (&le;1,
     * &ge;0)
     */
    public static float circle(float abscissa) {
        assert Validate.inRange(abscissa, "abscissa", -1f, 1f);

        double x = abscissa;
        float y = (float) Math.sqrt(1.0 - x * x);

        assert y >= 0f : y;
        assert y <= 1f : y;
        return y;
    }

    /**
     * Clamp the magnitude of a single-precision value.
     *
     * @param fValue input value to be clamped
     * @param maxMagnitude limit of the clamp (&ge;0)
     * @return value between -maxMagnitude and +maxMagnitude inclusive which is
     * closest to fValue
     * @see com.jme3.math.FastMath#clamp(float,float,float)
     */
    public static float clamp(float fValue, float maxMagnitude) {
        assert Validate.nonNegative(maxMagnitude, "limit");
        float result = FastMath.clamp(fValue, -maxMagnitude, maxMagnitude);

        assert result >= -maxMagnitude : result;
        assert result <= maxMagnitude : result;
        return result;
    }

    /**
     * Clamp the magnitude of a double-precision value.
     *
     * @param dValue input value to be clamped
     * @param maxMagnitude limit of the clamp (&ge;0)
     * @return value between -maxMagnitude and +maxMagnitude inclusive which is
     * closest to fValue
     * @see com.jme3.math.FastMath#clamp(float,float,float)
     */
    public static double clamp(double dValue, double maxMagnitude) {
        assert Validate.nonNegative(maxMagnitude, "limit");

        if (dValue < -maxMagnitude) {
            return -maxMagnitude;
        } else if (dValue > maxMagnitude) {
            return maxMagnitude;
        } else {
            return dValue;
        }
    }

    /**
     * Clamp a double-precision value between 2 limits.
     *
     * @param dValue input value to be clamped
     * @param min lower limit of the clamp
     * @param max upper limit of the clamp
     * @return the value between min and max inclusive that is closest to fValue
     * @see com.jme3.math.FastMath#clamp(float,float,float)
     */
    public static double clamp(double dValue, double min, double max) {
        double result;
        if (dValue < min) {
            result = min;
        } else if (dValue > max) {
            result = max;
        } else {
            result = dValue;
        }

        return result;
    }

    /**
     * Clamp an integer value between 2 limits.
     *
     * @param iValue input value to be clamped
     * @param min the lower limit
     * @param max the upper limit
     * @return the value between min and max inclusive that is closest to iValue
     */
    public static int clamp(int iValue, int min, int max) {
        int result;
        if (iValue < min) {
            result = min;
        } else if (iValue > max) {
            result = max;
        } else {
            result = iValue;
        }

        return result;
    }

    /**
     * Combine the specified transforms.
     * <p>
     * It is safe for any or all of {@code child}, {@code parent}, and
     * {@code storeResult} to be the same object.
     * <p>
     * Unlike {@link
     * com.jme3.math.Transform#combineWithParent(com.jme3.math.Transform)}, this
     * method works on transforms containing non-normalized quaternions.
     *
     * @param child the transform applied first (not null, unaffected unless
     * it's {@code storeResult})
     * @param parent the transform applied last (not null, unaffected unless
     * it's {@code storeResult})
     * @param storeResult (modified if not null)
     * @return a Transform equivalent to {@code child} followed by
     * {@code parent} (either {@code storeResult} or a new instance)
     */
    public static Transform combine(
            Transform child, Transform parent, Transform storeResult) {
        TempVars tempVars = TempVars.get();
        Vector3f combTranslation = tempVars.vect1; // alias
        Quaternion combRotation = tempVars.quat1; // alias
        Vector3f combScale = tempVars.vect2; // alias

        Vector3f parentTranslation = parent.getTranslation(); // alias
        Quaternion parentRotation = parent.getRotation(); // alias
        Vector3f parentScale = parent.getScale(); // alias

        // Combine the scales.
        child.getScale().mult(parentScale, combScale);

        // Combine the (intrinsic) rotations and re-normalize.
        Quaternion childRotation = child.getRotation(); // alias
        parentRotation.mult(childRotation, combRotation);
        MyQuaternion.normalizeLocal(combRotation);
        /*
         * The combined translation is the parent's scale, rotation,
         * and translation applied (in that order) to the child's translation.
         */
        child.getTranslation().mult(parentScale, combTranslation);
        MyQuaternion.rotate(parentRotation, combTranslation, combTranslation);
        combTranslation.addLocal(parentTranslation);

        Transform result
                = (storeResult == null) ? new Transform() : storeResult;
        result.setTranslation(combTranslation);
        result.setRotation(combRotation);
        result.setScale(combScale);
        tempVars.release();

        return result;
    }

    /**
     * Cube the specified single-precision value. Logs a warning in case of
     * overflow.
     *
     * @param fValue input value to be cubed
     * @return fValue raised to the third power
     * @see #cubeRoot(float)
     * @see com.jme3.math.FastMath#sqr(float)
     */
    public static float cube(float fValue) {
        float result = fValue * fValue * fValue;

        if (Float.isInfinite(result)) {
            String message = String.format("Overflow from cubing %g.", fValue);
            logger.warning(message);
        }
        return result;
    }

    /**
     * Extract the cube root of a single-precision value. Unlike
     * {@link com.jme3.math.FastMath#pow(float,float)}, this method works on
     * negative values.
     *
     * @param fValue input cube to be extracted (may be negative)
     * @return cube root of fValue
     * @see #cube(float)
     * @see com.jme3.math.FastMath#pow(float,float)
     * @see java.lang.Math#cbrt(double)
     */
    public static float cubeRoot(float fValue) {
        double dValue = fValue;
        float result = (float) Math.cbrt(dValue);

        return result;
    }

    /**
     * Return the discriminant {@code (b^2 - 4*a*c)} of a quadratic equation in
     * standard form {@code (a*x^2 + b*x + c) == 0}.
     *
     * @param a the coefficient of the square term
     * @param b the coefficient of the linear term
     * @param c the constant term
     * @return the discriminant
     */
    public static double discriminant(double a, double b, double c) {
        double result = b * b - 4.0 * a * c;
        return result;
    }

    /**
     * Quartic easing function for animation: slow at the start, fast at the
     * end.
     *
     * @param time the elapsed time since the start of the animation (&ge;0,
     * &le;duration)
     * @param start the starting value
     * @param end the ending value
     * @param duration the duration of the animation (&gt;0)
     * @return the current value
     */
    public static float easeInQuartic(
            float time, float start, float end, float duration) {
        assert Validate.positive(duration, "duration");

        float t = time / duration; // goes from 0 -> 1
        float t2 = t * t;
        float fraction = t2 * t2; // goes from 0 -> 1
        float result = lerp(fraction, start, end);

        return result;
    }

    /**
     * Quartic easing function for animation: fast at the start, slow at the
     * end.
     *
     * @param time the elapsed time since the start of the animation (&ge;0,
     * &le;duration)
     * @param start the starting value
     * @param end the ending value
     * @param duration the duration of the animation (&gt;0)
     * @return the current value
     */
    public static float easeOutQuartic(
            float time, float start, float end, float duration) {
        assert Validate.positive(duration, "duration");

        float t = time / duration - 1f; // goes from -1 -> 0
        float t2 = t * t;
        float fraction = 1f - t2 * t2; // goes from 0 -> 1
        float result = lerp(fraction, start, end);

        return result;
    }

    /**
     * Fade polynomial for Perlin noise. Double-precision arithmetic is used to
     * reduce rounding error.
     *
     * @param t input value (&le;1, &ge;0)
     * @return 6*t^5 - 15*t^4 + 10*t^3 (&le;1, &ge;0)
     */
    public static float fade(float t) {
        assert Validate.fraction(t, "t");

        double tt = t;
        double ff = tt * tt * tt * (10.0 + tt * (-15.0 + 6.0 * tt));
        float result = (float) ff;

        assert result >= 0f : result;
        assert result <= 1f : result;
        return result;
    }

    /**
     * Extract the 4th root of a double-precision value. This method is faster
     * than Math.pow(d, 0.25).
     *
     * @param dValue input 4th power to be extracted (&ge;0)
     * @return the positive 4th root of dValue (&ge;0)
     * @see java.lang.Math#cbrt(double)
     */
    public static double fourthRoot(double dValue) {
        assert Validate.nonNegative(dValue, "dValue");

        double sqrt = Math.sqrt(dValue);
        double result = Math.sqrt(sqrt);

        assert result >= 0.0 : result;
        return result;
    }

    /**
     * Sets a rotation matrix from the specified Tait-Bryan angles, applying the
     * rotations in x-z-y extrinsic order or y-z'-x" intrinsic order.
     *
     * @param xAngle the X angle (in radians)
     * @param yAngle the Y angle (in radians)
     * @param zAngle the Z angle (in radians)
     * @param storeResult storage for the result (modified if not null)
     * @return a rotation matrix (either {@code storeResult} or a new instance)
     */
    public static Matrix3f fromAngles(
            float xAngle, float yAngle, float zAngle, Matrix3f storeResult) {
        Matrix3f result = (storeResult == null) ? new Matrix3f() : storeResult;

        float c1 = FastMath.cos(yAngle);
        float c2 = FastMath.cos(zAngle);
        float c3 = FastMath.cos(xAngle);
        float s1 = FastMath.sin(yAngle);
        float s2 = FastMath.sin(zAngle);
        float s3 = FastMath.sin(xAngle);

        result.set(0, 0, c1 * c2);
        result.set(0, 1, s1 * s3 - c1 * c3 * s2);
        result.set(0, 2, c3 * s1 + c1 * s2 * s3);

        result.set(1, 0, s2);
        result.set(1, 1, c2 * c3);
        result.set(1, 2, -c2 * s3);

        result.set(2, 0, -c2 * s1);
        result.set(2, 1, c1 * s3 + c3 * s1 * s2);
        result.set(2, 2, c1 * c3 - s1 * s2 * s3);

        return result;
    }

    /**
     * Return the root sum of squares of some single-precision values.
     * Double-precision arithmetic is used to reduce the risk of overflow.
     *
     * @param fValues the input values
     * @return the positive square root of the sum of squares (&ge;0)
     */
    public static float hypotenuse(float... fValues) {
        double sum = 0.0;
        for (float fValue : fValues) {
            double value = fValue;
            sum += value * value;
        }

        float result = (float) Math.sqrt(sum);
        assert result >= 0f : result;
        return result;
    }

    /**
     * Return the root sum of squares of some double-precision values.
     *
     * @param dValues the input values
     * @return the positive square root of the sum of squares (&ge;0)
     */
    public static double hypotenuseDouble(double... dValues) {
        double sum = 0.0;
        for (double value : dValues) {
            sum += value * value;
        }

        double result = Math.sqrt(sum);
        assert result >= 0.0 : result;
        return result;
    }

    /**
     * Test whether b is between a and c.
     *
     * @param a the first input value
     * @param b the 2nd input value
     * @param c the 3rd input value
     * @return true if b is between a and c (inclusive), otherwise false
     */
    public static boolean isBetween(float a, float b, float c) {
        if (a > c) {
            return a >= b && b >= c;
        } else if (a < c) {
            return a <= b && b <= c;
        } else if (a == c) {
            return a == b;
        } else {
            String message = "a = " + a + " c = " + c;
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Test whether b is between a and c.
     *
     * @param a the first input value
     * @param b the 2nd input value
     * @param c the 3rd input value
     * @return true if b is between a and c (inclusive), otherwise false
     */
    public static boolean isBetween(int a, int b, int c) {
        if (a > c) {
            return a >= b && b >= c;
        } else if (a < c) {
            return a <= b && b <= c;
        } else {
            assert a == c;
            return a == b;
        }
    }

    /**
     * Test whether b is between a and c.
     *
     * @param a the first input value
     * @param b the 2nd input value
     * @param c the 3rd input value
     * @return true if b is between a and c (inclusive), otherwise false
     */
    public static boolean isBetween(double a, double b, double c) {
        if (a > c) {
            return a >= b && b >= c;
        } else if (a < c) {
            return a <= b && b <= c;
        } else if (a == c) {
            return a == b;
        } else {
            String message = "a = " + a + " c = " + c;
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Test the specified transform for exact identity.
     *
     * @param transform which transform to test (not null, unaffected)
     * @return true if exact identity, otherwise false
     */
    public static boolean isIdentity(Transform transform) {
        boolean result = false;
        Vector3f translation = transform.getTranslation(); // alias
        if (MyVector3f.isZero(translation)) {
            Quaternion rotation = transform.getRotation(); // alias
            if (MyQuaternion.isRotationIdentity(rotation)) {
                Vector3f scale = transform.getScale(); // alias
                result = MyVector3f.isScaleIdentity(scale);
            }
        }

        return result;
    }

    /**
     * Test whether an integer value is odd.
     *
     * @param iValue the value to be tested
     * @return true if x is odd, false if it's even
     */
    public static boolean isOdd(int iValue) {
        boolean result = (iValue % 2) != 0;
        return result;
    }

    /**
     * Interpolate linearly between (or extrapolate linearly from) 2
     * single-precision values.
     * <p>
     * Unlike {@link com.jme3.math.FastMath#interpolateLinear(float, float,
     * float)}, no rounding error is introduced when y0==y1.
     *
     * @param t the weight given to {@code y1}
     * @param y0 the function value at t=0
     * @param y1 the function value at t=1
     * @return the interpolated function value
     */
    public static float lerp(float t, float y0, float y1) {
        float result;
        if (y0 == y1) {
            result = y0;
        } else {
            float u = 1f - t;
            result = u * y0 + t * y1;
        }

        return result;
    }

    /**
     * Interpolate linearly between (or extrapolate linearly from) 3
     * single-precision values.
     *
     * @param t1 the weight given to {@code y1}
     * @param t2 the weight given to {@code y2}
     * @param y0 the function value at t1=0, t2=0
     * @param y1 the function value at t1=1, t2=0
     * @param y2 the function value at t1=0, t2=1
     * @return the interpolated function value
     */
    public static float lerp3(
            float t1, float t2, float y0, float y1, float y2) {
        float u = 1f - t1 - t2;
        float result = u * y0 + t1 * y1 + t2 * y2;

        return result;
    }

    /**
     * Calculate the floor of the base-2 logarithm of the input value.
     *
     * @param iValue the input value (&ge;1)
     * @return the largest integer N&le;30 for which {@code (1 << N) <= iValue}
     * (&ge;0, &le;30)
     */
    public static int log2(int iValue) {
        Validate.positive(iValue, "input value");
        int result = 31 - Integer.numberOfLeadingZeros(iValue);
        return result;
    }

    /**
     * Find the maximum of some single-precision values.
     *
     * @param fValues the input values
     * @return the most positive value
     * @see java.lang.Math#max(float, float)
     */
    public static float max(float... fValues) {
        float result = Float.NEGATIVE_INFINITY;
        for (float value : fValues) {
            if (value > result) {
                result = value;
            }
        }

        return result;
    }

    /**
     * Find the maximum of some double-precision values.
     *
     * @param dValues the input values
     * @return the most positive value
     * @see java.lang.Math#max(double, double)
     */
    public static double maxDouble(double... dValues) {
        double result = Double.NEGATIVE_INFINITY;
        for (double value : dValues) {
            if (value > result) {
                result = value;
            }
        }

        return result;
    }

    /**
     * Find the maximum of some int values.
     *
     * @param iValues the input values
     * @return the most positive value
     * @see java.util.Collections#max(java.util.Collection)
     * @see java.lang.Math#max(int, int)
     */
    public static int maxInt(int... iValues) {
        int result = Integer.MIN_VALUE;
        for (int value : iValues) {
            if (value > result) {
                result = value;
            }
        }

        return result;
    }

    /**
     * Find the median of 3 single-precision values.
     *
     * @param a the first input value
     * @param b the 2nd input value
     * @param c the 3rd input value
     * @return the median of the 3 values
     */
    public static float mid(float a, float b, float c) {
        if (a >= b) {
            if (b >= c) {
                return b; // a >= b >= c
            } else if (a >= c) {
                return c; // a >= c > b
            } else {
                return a; // c > a >= b
            }
        } else if (a >= c) {
            return a; // b > a >= c
        } else if (b >= c) {
            return c; // b >= c > a
        } else {
            return b; // c > b > a
        }
    }

    /**
     * Find the median of 3 double-precision values.
     *
     * @param a the first input value
     * @param b the 2nd input value
     * @param c the 3rd input value
     * @return the median of the 3 values
     */
    public static double mid(double a, double b, double c) {
        if (a >= b) {
            if (b >= c) {
                return b; // a >= b >= c
            } else if (a >= c) {
                return c; // a >= c > b
            } else {
                return a; // c > a >= b
            }
        } else if (a >= c) {
            return a; // b > a >= c
        } else if (b >= c) {
            return c; // b >= c > a
        } else {
            return b; // c > b > a
        }
    }

    /**
     * Find the minimum of some single-precision values.
     *
     * @param fValues the input values
     * @return the most negative value
     * @see java.lang.Math#min(float, float)
     */
    public static float min(float... fValues) {
        float result = Float.POSITIVE_INFINITY;
        for (float value : fValues) {
            if (value < result) {
                result = value;
            }
        }

        return result;
    }

    /**
     * Find the minimum of some double-precision values.
     *
     * @param dValues the input values
     * @return the most negative value
     * @see java.lang.Math#min(double, double)
     */
    public static double minDouble(double... dValues) {
        double result = Double.POSITIVE_INFINITY;
        for (double value : dValues) {
            if (value < result) {
                result = value;
            }
        }

        return result;
    }

    /**
     * Return the least non-negative value congruent with the input value with
     * respect to the specified modulus.
     * <p>
     * This differs from remainder for negative input values. For instance,
     * modulo(-1, 4) == 3, while -1 % 4 == -1.
     *
     * @param iValue the input value
     * @param modulus (&gt;0)
     * @return iValue MOD modulus (&lt;modulus, &ge;0)
     */
    public static int modulo(int iValue, int modulus) {
        assert Validate.positive(modulus, "modulus");

        int remainder = iValue % modulus;
        int result;
        if (iValue >= 0) {
            result = remainder;
        } else {
            result = (remainder + modulus) % modulus;
        }

        assert result >= 0f : result;
        assert result < modulus : result;
        return result;
    }

    /**
     * Return the least non-negative value congruent with the input value with
     * respect to the specified modulus.
     * <p>
     * This differs from remainder for negative input values. For instance,
     * modulo(-1f, 4f) == 3f, while -1f % 4f == -1f.
     *
     * @param fValue the input value
     * @param modulus (&gt;0)
     * @return fValue MOD modulus (&lt;modulus, &ge;0)
     */
    public static float modulo(float fValue, float modulus) {
        assert Validate.positive(modulus, "modulus");

        float remainder = fValue % modulus;
        float result;
        if (fValue >= 0) {
            result = remainder;
        } else {
            result = (remainder + modulus) % modulus;
        }

        assert result >= 0f : result;
        assert result < modulus : result;
        return result;
    }

    /**
     * Return the least non-negative value congruent with the input value with
     * respect to the specified modulus.
     * <p>
     * This differs from remainder for negative input values. For instance,
     * modulo(-1, 4) == 3, while -1 % 4 == -1.
     *
     * @param dValue the input value
     * @param modulus (&gt;0)
     * @return dValue MOD modulus (&lt;modulus, &ge;0)
     */
    public static double modulo(double dValue, double modulus) {
        assert Validate.positive(modulus, "modulus");

        double remainder = dValue % modulus;
        double result;
        if (dValue >= 0) {
            result = remainder;
        } else {
            result = (remainder + modulus) % modulus;
        }

        assert result >= 0.0 : result;
        assert result < modulus : result;
        return result;
    }

    /**
     * Interpolate between 2 transforms using spherical linear (Slerp)
     * interpolation.
     * <p>
     * This method is slower (but more accurate) than {@link
     * com.jme3.math.Transform#interpolateTransforms(
     * com.jme3.math.Transform, com.jme3.math.Transform, float)} and doesn't
     * trash {@code t1}. The caller is responsible for flipping quaternion signs
     * when it's appropriate to do so.
     *
     * @param t the weight given to {@code t1} (&ge;0, &le;1)
     * @param t0 the function value at t=0 (not null, unaffected unless it's
     * {@code storeResult})
     * @param t1 the function value at t=1 (not null, unaffected unless it's
     * {@code storeResult})
     * @param storeResult storage for the result (modified if not null, may be
     * {@code t0} or {@code t1})
     * @return the interpolated transform (either {@code storeResult} or a new
     * instance)
     */
    public static Transform slerp(
            float t, Transform t0, Transform t1, Transform storeResult) {
        assert Validate.fraction(t, "weight");
        assert Validate.nonNull(t0, "t0");
        assert Validate.nonNull(t1, "t1");
        Transform result
                = (storeResult == null) ? new Transform() : storeResult;

        MyVector3f.lerp(t, t0.getTranslation(), t1.getTranslation(),
                result.getTranslation());

        MyQuaternion.slerp(
                t, t0.getRotation(), t1.getRotation(), result.getRotation());

        MyVector3f.lerp(
                t, t0.getScale(), t1.getScale(), result.getScale());

        return result;
    }

    /**
     * Square the specified double-precision value. Logs a warning in case of
     * overflow.
     *
     * @param dValue input value to be squared
     * @return dValue squared (&ge;0)
     * @see com.jme3.math.FastMath#sqr(float)
     * @see Math#sqrt(double)
     */
    public static double sqr(double dValue) {
        double result = dValue * dValue;

        if (Double.isInfinite(result)) {
            String message = String.format(
                    "Overflow from squaring %g.", dValue);
            logger.warning(message);
        }
        assert result >= 0.0 : result;
        return result;
    }

    /**
     * Standardize a single-precision value in preparation for hashing.
     *
     * @param fValue input value
     * @return an equivalent value that's not -0
     */
    public static float standardize(float fValue) {
        float result = fValue;
        if (Float.compare(fValue, -0f) == 0) {
            result = 0f;
        }

        return result;
    }

    /**
     * Standardize a rotation angle to the range [-Pi, Pi).
     *
     * @param angle the input angle (in radians)
     * @return the standardized angle (in radians, &lt;Pi, &ge;-Pi)
     */
    public static float standardizeAngle(float angle) {
        Validate.finite(angle, "angle");

        float result = modulo(angle, FastMath.TWO_PI);
        if (result >= FastMath.PI) {
            result -= FastMath.TWO_PI;
        }

        assert result >= -FastMath.PI : result;
        assert result < FastMath.PI : result;
        return result;
    }

    /**
     * Return the sum of squares of some single-precision values.
     * Double-precision arithmetic is used to reduce the risk of overflow.
     *
     * @param fValues the input values
     * @return the sum of squares (&ge;0)
     */
    public static double sumOfSquares(float... fValues) {
        double result = 0.0;
        for (float fValue : fValues) {
            double value = fValue;
            result += value * value;
        }

        assert result >= 0.0 : result;
        return result;
    }

    /**
     * Convert an angle from radians to degrees.
     *
     * @param radians input angle
     * @return equivalent in degrees
     * @see java.lang.Math#toDegrees(double)
     */
    public static float toDegrees(float radians) {
        float result = radians * FastMath.RAD_TO_DEG;
        return result;
    }

    /**
     * Convert an angle from degrees to radians.
     *
     * @param degrees input angle
     * @return equivalent in radians
     * @see java.lang.Math#toRadians(double)
     */
    public static float toRadians(float degrees) {
        float result = degrees * FastMath.DEG_TO_RAD;
        return result;
    }

    /**
     * Apply the specified transform to a Vector3f.
     * <p>
     * It is safe for {@code input} and {@code storeResult} to be the same
     * object.
     * <p>
     * Unlike {@link
     * com.jme3.math.Transform#transformVector(com.jme3.math.Vector3f,
     * com.jme3.math.Vector3f)}, this method works on transforms containing
     * non-normalized quaternions.
     *
     * @param transform the transform to apply (not null, unaffected unless
     * {@code storeResult} is its translation or scaling component)
     * @param input the input vector (not null, unaffected unless it's
     * {@code storeResult})
     * @param storeResult storage for the result (modified if not null)
     * @return the transformed vector (either {@code storeResult} or a new
     * instance)
     */
    public static Vector3f transform(
            Transform transform, Vector3f input, Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;
        Vector3f translation = transform.getTranslation(); // alias
        if (translation == result) {
            translation = translation.clone();
        }

        // scale
        Vector3f scale = transform.getScale(); // alias
        input.mult(scale, result);

        // rotate
        Quaternion rotation = transform.getRotation(); // alias
        MyQuaternion.rotate(rotation, result, result);

        // translate
        result.addLocal(translation);

        return result;
    }

    /**
     * Apply the inverse of the specified transform to each vertex of a
     * Triangle.
     * <p>
     * It is safe for {@code input} and {@code storeResult} to be the same
     * object.
     *
     * @param transform the transform to apply (not null, unaffected)
     * @param input the input triangle (not null, unaffected unless it's
     * {@code storeResult})
     * @param storeResult storage for the result (modified if not null)
     * @return the transformed triangle (either {@code storeResult} or a new
     * instance)
     */
    public static Triangle transformInverse(
            Transform transform, Triangle input, Triangle storeResult) {
        Triangle result = (storeResult == null) ? new Triangle() : storeResult;

        Vector3f tmpVector = new Vector3f();
        for (int vertexIndex = 0; vertexIndex < MyMesh.vpt; ++vertexIndex) {
            Vector3f inputVector = input.get(vertexIndex); // alias
            MyMath.transformInverse(transform, inputVector, tmpVector);
            result.set(vertexIndex, tmpVector);
        }

        return result;
    }

    /**
     * Apply the inverse of the specified transform to a Vector3f.
     * <p>
     * It is safe for {@code input} and {@code storeResult} to be the same
     * object.
     * <p>
     * Unlike {@link
     * com.jme3.math.Transform#transformInverseVector(com.jme3.math.Vector3f,
     * com.jme3.math.Vector3f)}, this method works on transforms containing
     * non-normalized quaternions.
     *
     * @param transform the transform to un-apply (not null, unaffected unless
     * {@code storeResult} is its translation or scaling component)
     * @param input the input vector (not null, unaffected unless it's
     * {@code storeResult})
     * @param storeResult storage for the result (modified if not null)
     * @return the transformed vector (either {@code storeResult} or a new
     * instance)
     */
    public static Vector3f transformInverse(
            Transform transform, Vector3f input, Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;
        Vector3f scale = transform.getScale(); // alias
        if (scale == result) {
            scale = scale.clone();
        }

        // un-translate
        Vector3f translation = transform.getTranslation(); // alias
        input.subtract(translation, result);

        // un-rotate
        Quaternion rotation = transform.getRotation(); // alias
        MyQuaternion.rotateInverse(rotation, result, result);

        // de-scale
        result.divideLocal(scale);

        return result;
    }
}
