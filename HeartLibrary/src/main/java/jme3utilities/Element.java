/*
 Copyright (c) 2020-2022, Stephen Gold
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
package jme3utilities;

import com.jme3.scene.VertexBuffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Logger;
import jme3utilities.math.MyMath;

/**
 * Utility methods that operate on individual elements of vertex buffers.
 * Outside of index buffers, each element represents a single mesh vertex.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Element {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Element.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Element() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Copy all data from one vertex-buffer element to another. The vertex
     * buffers must have the same type, format, and number of components per
     * element.
     *
     * @param source the VertexBuffer containing the source element (not null,
     * unaffected)
     * @param sourceIndex the index of the source element (&ge;0)
     * @param target the VertexBuffer containing the target element (not null,
     * modified)
     * @param targetIndex the index of the target element (&ge;0)
     */
    public static void copy(VertexBuffer source, int sourceIndex,
            VertexBuffer target, int targetIndex) {
        int sourceCount = source.getNumElements();
        Validate.inRange(sourceIndex, "source index", 0, sourceCount - 1);
        int targetCount = target.getNumElements();
        Validate.inRange(targetIndex, "target index", 0, targetCount - 1);
        Validate.require(source.getBufferType() == target.getBufferType(),
                "same buffer type");
        Validate.require(source.getFormat() == target.getFormat(),
                "same format");
        Validate.require(source.getNumComponents() == target.getNumComponents(),
                "same number of components");

        int numComponents = target.getNumComponents();
        VertexBuffer.Format format = target.getFormat();
        if (format == VertexBuffer.Format.Half) {
            numComponents *= 2;
        }
        int sourceStart = numComponents * sourceIndex;
        int targetStart = numComponents * targetIndex;

        switch (format) {
            case Byte:
            case Half:
            case UnsignedByte:
                ByteBuffer sbBuf = (ByteBuffer) source.getData();
                ByteBuffer tbBuf = (ByteBuffer) target.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    byte b = sbBuf.get(sourceStart + cIndex);
                    tbBuf.put(targetStart + cIndex, b);
                }
                break;

            case Double:
                DoubleBuffer sdBuf = (DoubleBuffer) source.getData();
                DoubleBuffer tdBuf = (DoubleBuffer) target.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    double d = sdBuf.get(sourceStart + cIndex);
                    tdBuf.put(targetStart + cIndex, d);
                }
                break;

            case Float:
                FloatBuffer sfBuf = (FloatBuffer) source.getData();
                FloatBuffer tfBuf = (FloatBuffer) target.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    float f = sfBuf.get(sourceStart + cIndex);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            case Int:
            case UnsignedInt:
                IntBuffer siBuf = (IntBuffer) source.getData();
                IntBuffer tiBuf = (IntBuffer) target.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    int i = siBuf.get(sourceStart + cIndex);
                    tiBuf.put(targetStart + cIndex, i);
                }
                break;

            case Short:
            case UnsignedShort:
                ShortBuffer ssBuf = (ShortBuffer) source.getData();
                ShortBuffer tsBuf = (ShortBuffer) target.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    short s = ssBuf.get(sourceStart + cIndex);
                    tsBuf.put(targetStart + cIndex, s);
                }
                break;

            default:
                String message = "Unrecognized buffer format: " + format;
                throw new UnsupportedOperationException(message);
        }
    }

    /**
     * Compare all data between 2 elements of a VertexBuffer.
     *
     * @param vertexBuffer (not null, unaffected)
     * @param index1 the index of the first element (&ge;0)
     * @param index2 the index of the 2nd element (&ge;0)
     * @return true if all data are equal, otherwise false
     */
    public static boolean equals(VertexBuffer vertexBuffer, int index1,
            int index2) {
        int numElements = vertexBuffer.getNumElements();
        Validate.inRange(index1, "index1", 0, numElements - 1);
        Validate.inRange(index2, "index2", 0, numElements - 1);
        if (index1 == index2) {
            return true;
        }

        int numCperE = vertexBuffer.getNumComponents();
        VertexBuffer.Format format = vertexBuffer.getFormat();
        if (format == VertexBuffer.Format.Half) {
            numCperE *= 2;
        }
        int startPos1 = numCperE * index1;
        int startPos2 = numCperE * index2;

        switch (format) {
            case Byte:
            case Half:
            case UnsignedByte:
                ByteBuffer byteBuf = (ByteBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numCperE; ++cIndex) {
                    byte b1 = byteBuf.get(startPos1 + cIndex);
                    byte b2 = byteBuf.get(startPos2 + cIndex);
                    if (b1 != b2) {
                        return false;
                    }
                }
                break;

            case Double:
                DoubleBuffer doubleBuffer
                        = (DoubleBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numCperE; ++cIndex) {
                    double d1 = doubleBuffer.get(startPos1 + cIndex);
                    double d2 = doubleBuffer.get(startPos2 + cIndex);
                    if (d1 != d2) {
                        return false;
                    }
                }
                break;

            case Float:
                FloatBuffer floatBuf = (FloatBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numCperE; ++cIndex) {
                    float f1 = floatBuf.get(startPos1 + cIndex);
                    float f2 = floatBuf.get(startPos2 + cIndex);
                    if (f1 != f2) {
                        return false;
                    }
                }
                break;

            case Int:
            case UnsignedInt:
                IntBuffer intBuf = (IntBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numCperE; ++cIndex) {
                    int i1 = intBuf.get(startPos1 + cIndex);
                    int i2 = intBuf.get(startPos2 + cIndex);
                    if (i1 != i2) {
                        return false;
                    }
                }
                break;

            case Short:
            case UnsignedShort:
                ShortBuffer shortBuf = (ShortBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numCperE; ++cIndex) {
                    short s1 = shortBuf.get(startPos1 + cIndex);
                    short s2 = shortBuf.get(startPos2 + cIndex);
                    if (s1 != s2) {
                        return false;
                    }
                }
                break;

            default:
                String message = "Unrecognized buffer format: " + format;
                throw new UnsupportedOperationException(message);
        }

        return true;
    }

    /**
     * Linearly interpolate all data between 2 vertex-buffer elements and store
     * the result in a 3rd vertex-buffer element. The vertex buffers must have
     * the same type and number of components per element.
     *
     * @param t the descaled parameter value from y0 to y1
     * @param source the VertexBuffer containing the source elements (not null,
     * unaffected)
     * @param sourceIndex0 the index of the source element for t=0 (&ge;0)
     * @param sourceIndex1 the index of the source element for t=1 (&ge;0)
     * @param target the VertexBuffer containing the target element (not null,
     * Float format, modified)
     * @param targetIndex the index of the target element (&ge;0)
     */
    public static void lerp(float t, VertexBuffer source, int sourceIndex0,
            int sourceIndex1, VertexBuffer target, int targetIndex) {
        int sourceCount = source.getNumElements();
        Validate.inRange(sourceIndex0, "source index(t=0)", 0, sourceCount - 1);
        Validate.inRange(sourceIndex1, "source index(t=1)", 0, sourceCount - 1);
        int targetCount = target.getNumElements();
        Validate.inRange(targetIndex, "target index", 0, targetCount - 1);
        Validate.require(source.getBufferType() == target.getBufferType(),
                "same buffer type");
        Validate.require(source.getNumComponents() == target.getNumComponents(),
                "same number of components");
        Validate.require(target.getFormat() == VertexBuffer.Format.Float,
                "Float format");

        int numComponents = target.getNumComponents();
        VertexBuffer.Format format = target.getFormat();
        if (format == VertexBuffer.Format.Half) {
            numComponents *= 2;
        }
        int sourceStart0 = numComponents * sourceIndex0;
        int sourceStart1 = numComponents * sourceIndex1;
        int targetStart = numComponents * targetIndex;
        FloatBuffer tfBuf = (FloatBuffer) target.getData();

        switch (format) {
            case Byte:
                ByteBuffer sbBuf = (ByteBuffer) source.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    byte b0 = sbBuf.get(sourceStart0 + cIndex);
                    byte b1 = sbBuf.get(sourceStart1 + cIndex);
                    float f = MyMath.lerp(t, b0, b1);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            case UnsignedByte:
                sbBuf = (ByteBuffer) source.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    byte b0 = sbBuf.get(sourceStart0 + cIndex);
                    byte b1 = sbBuf.get(sourceStart1 + cIndex);
                    int i0 = 0xFF & (int) b0;
                    int i1 = 0xFF & (int) b1;
                    float f = MyMath.lerp(t, i0, i1);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            case Double:
                DoubleBuffer sdBuf = (DoubleBuffer) source.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    double d0 = sdBuf.get(sourceStart0 + cIndex);
                    double d1 = sdBuf.get(sourceStart1 + cIndex);
                    float f = MyMath.lerp(t, (float) d0, (float) d1);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            case Float:
                FloatBuffer sfBuf = (FloatBuffer) source.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    float f0 = sfBuf.get(sourceStart0 + cIndex);
                    float f1 = sfBuf.get(sourceStart1 + cIndex);
                    float f = MyMath.lerp(t, (float) f0, (float) f1);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            case Int:
                IntBuffer siBuf = (IntBuffer) source.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    int i0 = siBuf.get(sourceStart0 + cIndex);
                    int i1 = siBuf.get(sourceStart1 + cIndex);
                    float f = MyMath.lerp(t, (float) i0, (float) i1);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            case UnsignedInt:
                siBuf = (IntBuffer) source.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    int i0 = siBuf.get(sourceStart0 + cIndex);
                    int i1 = siBuf.get(sourceStart1 + cIndex);
                    long l0 = 0xFFFFFFFF & (long) i0;
                    long l1 = 0xFFFFFFFF & (long) i1;
                    float f = MyMath.lerp(t, (float) l0, (float) l1);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            case Short:
                ShortBuffer ssBuf = (ShortBuffer) source.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    short s0 = ssBuf.get(sourceStart0 + cIndex);
                    short s1 = ssBuf.get(sourceStart1 + cIndex);
                    float f = MyMath.lerp(t, (float) s0, (float) s1);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            case UnsignedShort:
                ssBuf = (ShortBuffer) source.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    short s0 = ssBuf.get(sourceStart0 + cIndex);
                    short s1 = ssBuf.get(sourceStart1 + cIndex);
                    int i0 = 0xFFFF & (int) s0;
                    int i1 = 0xFFFF & (int) s1;
                    float f = MyMath.lerp(t, (float) i0, (float) i1);
                    tfBuf.put(targetStart + cIndex, f);
                }
                break;

            default:
                String message = "Unrecognized buffer format: " + format;
                throw new UnsupportedOperationException(message);
        }
    }

    /**
     * Swap all data between 2 elements of a VertexBuffer.
     *
     * @param vertexBuffer the VertexBuffer to modify (not null)
     * @param index1 the index of the first element (&ge;0)
     * @param index2 the index of the 2nd element (&ge;0)
     */
    public static void swap(VertexBuffer vertexBuffer, int index1, int index2) {
        int numElements = vertexBuffer.getNumElements();
        Validate.inRange(index1, "index1", 0, numElements - 1);
        Validate.inRange(index2, "index2", 0, numElements - 1);
        if (index1 == index2) {
            return;
        }

        int numComponents = vertexBuffer.getNumComponents();
        VertexBuffer.Format format = vertexBuffer.getFormat();
        if (format == VertexBuffer.Format.Half) {
            numComponents *= 2;
        }
        int startPos1 = numComponents * index1;
        int startPos2 = numComponents * index2;

        switch (format) {
            case Byte:
            case UnsignedByte:
            case Half:
                ByteBuffer byteBuf = (ByteBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    byte b1 = byteBuf.get(startPos1 + cIndex);
                    byte b2 = byteBuf.get(startPos2 + cIndex);
                    byteBuf.put(startPos1 + cIndex, b2);
                    byteBuf.put(startPos2 + cIndex, b1);
                }
                break;

            case Short:
            case UnsignedShort:
                ShortBuffer shortBuf = (ShortBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    short s1 = shortBuf.get(startPos1 + cIndex);
                    short s2 = shortBuf.get(startPos2 + cIndex);
                    shortBuf.put(startPos1 + cIndex, s2);
                    shortBuf.put(startPos2 + cIndex, s1);
                }
                break;

            case Int:
            case UnsignedInt:
                IntBuffer intBuf = (IntBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    int i1 = intBuf.get(startPos1 + cIndex);
                    int i2 = intBuf.get(startPos2 + cIndex);
                    intBuf.put(startPos1 + cIndex, i2);
                    intBuf.put(startPos2 + cIndex, i1);
                }
                break;

            case Double:
                DoubleBuffer doubleBuffer
                        = (DoubleBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    double d1 = doubleBuffer.get(startPos1 + cIndex);
                    double d2 = doubleBuffer.get(startPos2 + cIndex);
                    doubleBuffer.put(startPos1 + cIndex, d2);
                    doubleBuffer.put(startPos2 + cIndex, d1);
                }
                break;

            case Float:
                FloatBuffer floatBuf = (FloatBuffer) vertexBuffer.getData();
                for (int cIndex = 0; cIndex < numComponents; ++cIndex) {
                    float f1 = floatBuf.get(startPos1 + cIndex);
                    float f2 = floatBuf.get(startPos2 + cIndex);
                    floatBuf.put(startPos1 + cIndex, f2);
                    floatBuf.put(startPos2 + cIndex, f1);
                }
                break;

            default:
                String message = "Unrecognized buffer format: " + format;
                throw new UnsupportedOperationException(message);
        }
    }
}
