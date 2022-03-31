/*
 Copyright (c) 2019-2022, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

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

import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

/**
 * Limits on the size of a non-degenerate rectangle. Immutable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class RectSizeLimits {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(RectSizeLimits.class.getName());
    // *************************************************************************
    // fields

    /**
     * maximum height (&ge;minHeight)
     */
    final public int maxHeight;
    /**
     * maximum width (&ge;minWidth)
     */
    final public int maxWidth;
    /**
     * minimum height (&le;maxHeight)
     */
    final public int minHeight;
    /**
     * minimum width (&le;maxWidth)
     */
    final public int minWidth;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a set of limits.
     *
     * @param minWidth the desired minimum width (&gt;0, &le;maxWidth)
     * @param minHeight the desired minimum height (&gt;0, &le;maxHeight)
     * @param maxWidth the desired maximum width (&ge;minWidth)
     * @param maxHeight the desired maximum height (&ge;minHeight)
     */
    public RectSizeLimits(int minWidth, int minHeight, int maxWidth,
            int maxHeight) {
        Validate.inRange(minWidth, "minimum width", 1, maxWidth);
        Validate.inRange(minHeight, "minimum height", 1, maxHeight);
        Validate.inRange(maxWidth, "maximum width", minWidth,
                Integer.MAX_VALUE);
        Validate.inRange(maxHeight, "maximum height", minHeight,
                Integer.MAX_VALUE);

        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Clamp the specified height to the limits.
     *
     * @param height the height to be clamped
     * @return the value between minHeight and maxHeight inclusive that is
     * closest to {@code height}
     */
    public int clampHeight(int height) {
        int result = MyMath.clamp(height, minHeight, maxHeight);
        return result;
    }

    /**
     * Clamp the specified width to the limits.
     *
     * @param width the width to be clamped
     * @return the value between minWidth and maxWidth inclusive that is closest
     * to {@code width}
     */
    public int clampWidth(int width) {
        int result = MyMath.clamp(width, minWidth, maxWidth);
        return result;
    }

    /**
     * Explain why the specified size is invalid.
     *
     * @param width the width to test
     * @param height the height to test
     * @return message text in English or "" if in range (not null)
     */
    public String feedbackInRange(int width, int height) {
        if (width < minWidth) {
            return String.format("width must not be < %d", minWidth);
        } else if (width > maxWidth) {
            return String.format("width must not be > %d", maxWidth);
        } else if (height < minHeight) {
            return String.format("height must not be < %d", minHeight);
        } else if (height > maxHeight) {
            return String.format("height must not be > %d", maxHeight);
        } else {
            return "";
        }
    }

    /**
     * Test the validity of the specified dimensions.
     * <p>
     * This test is inclusive, so both (minWidth, minHeight) and (maxWidth,
     * maxHeight) are considered valid.
     *
     * @param width the width to test
     * @param height the height to test
     * @return true if in range, otherwise false
     */
    public boolean isInRange(int width, int height) {
        if (!MyMath.isBetween(minHeight, height, maxHeight)) {
            return false;

        } else if (!MyMath.isBetween(minWidth, width, maxWidth)) {
            return false;

        } else {
            return true;
        }
    }
    // *************************************************************************
    // Object methods

    /**
     * Test for exact equivalence with another Object.
     *
     * @param otherObject the object to compare to (may be null, unaffected)
     * @return true if the objects are equivalent, otherwise false
     */
    @Override
    public boolean equals(Object otherObject) {
        boolean result;
        if (otherObject == this) {
            result = true;
        } else if (otherObject != null
                && otherObject.getClass() == getClass()) {
            RectSizeLimits other = (RectSizeLimits) otherObject;
            result = (other.maxHeight == maxHeight)
                    && (other.maxWidth == maxWidth)
                    && (other.minHeight == minHeight)
                    && (other.minWidth == minWidth);
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Generate the hash code for these limits.
     *
     * @return the value to use for hashing
     */
    @Override
    public int hashCode() {
        int result = 707;
        result = 29 * result + maxHeight;
        result = 29 * result + maxWidth;
        result = 29 * result + minHeight;
        result = 29 * result + minWidth;

        return result;
    }

    /**
     * Represent these limits as a text string.
     * <p>
     * The format is: (minWidth, minHeight)-(maxWidth, maxHeight)
     *
     * @return a descriptive string of text (not null)
     */
    @Override
    public String toString() {
        String result = String.format("(%d,%d)-(%d,%d)",
                minWidth, minHeight, maxWidth, maxHeight);
        return result;
    }
}
