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
package jme3utilities.mesh;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.logging.Logger;
import jme3utilities.MyMesh;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * A static, Lines-mode mesh (with indices) that renders a subdivided line
 * segment.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DividedLine extends Mesh {
    // *************************************************************************
    // constants and loggers

    /**
     * number of axes in a vector
     */
    final private static int numAxes = 3;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DividedLine.class.getName());
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil.
     */
    protected DividedLine() {
    }

    /**
     * Instantiate a uniformly subdivided line segment between the specified
     * endpoints.
     *
     * @param endPoint1 the desired location of the first endpoint (in mesh
     * coordinates, not null, unaffected)
     * @param endPoint2 the desired location of the 2nd endpoint (in mesh
     * coordinates, not null, unaffected)
     * @param numSegments the desired number of sub-segments (&ge;1)
     */
    public DividedLine(Vector3f endPoint1, Vector3f endPoint2,
            int numSegments) {
        Validate.positive(numSegments, "number of segments");

        setMode(Mode.Lines);

        int numVertices = numSegments + 1;
        int numFloats = numAxes * numVertices;
        FloatBuffer posBuffer = BufferUtils.createFloatBuffer(numFloats);
        setBuffer(VertexBuffer.Type.Position, numAxes, posBuffer);
        /*
         * Write the locations of all vertices:
         */
        Vector3f temp = new Vector3f();
        for (int vIndex = 0; vIndex < numVertices; ++vIndex) {
            float t = vIndex / (float) numSegments;
            MyVector3f.lerp(t, endPoint1, endPoint2, temp);
            posBuffer.put(temp.x).put(temp.y).put(temp.z);
        }
        assert posBuffer.position() == numFloats;
        posBuffer.flip();

        int numIndices = MyMesh.vpe * numSegments;
        IndexBuffer indexBuffer
                = IndexBuffer.createIndexBuffer(numVertices, numIndices);
        VertexBuffer.Format ibFormat = indexBuffer.getFormat();
        Buffer ibData = indexBuffer.getBuffer();
        setBuffer(VertexBuffer.Type.Index, 1, ibFormat, ibData);
        /*
         * Write the vertex indices of all edges:
         */
        for (int edgeIndex = 0; edgeIndex < numSegments; ++edgeIndex) {
            indexBuffer.put(edgeIndex);
            indexBuffer.put(edgeIndex + 1);
        }
        ibData.flip();
        assert indexBuffer.size() == numIndices;

        updateBound();
        setStatic();
    }
}
