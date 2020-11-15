/*
 Copyright (c) 2020, Stephen Gold
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

import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A 2-D, static, TriangleFan-mode mesh that renders a circular disc in the X-Y
 * plane.
 * <p>
 * In local space, X and Y extend from -radius to +radius, with normals set to
 * (0,0,1). In texture space, U and V extend from 0 to 1.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DiscMesh extends Mesh {
    // *************************************************************************
    // constants and loggers

    /**
     * number of axes in a vector
     */
    final private static int numAxes = 3;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DiscMesh.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disc with the specified radius and number of vertices.
     *
     * @param radius the radius of the disc (&ge;0)
     * @param numVertices the number of vertices (&ge;3)
     */
    public DiscMesh(float radius, int numVertices) {
        Validate.nonNegative(radius, "radius");
        Validate.inRange(numVertices, "number of vertices", 3,
                Integer.MAX_VALUE);

        setMode(Mode.TriangleFan);

        float[] normals = new float[numAxes * numVertices];
        float[] positions = new float[numAxes * numVertices];
        float[] texCoords = new float[2 * numVertices];

        for (int vi = 0; vi < numVertices; ++vi) {
            normals[numAxes * vi] = 0f;
            normals[numAxes * vi + 1] = 0f;
            normals[numAxes * vi + 2] = 1f;

            float theta = FastMath.TWO_PI * vi / numVertices;
            float sin = FastMath.sin(theta);
            float cos = FastMath.cos(theta);
            positions[numAxes * vi] = radius * cos;
            positions[numAxes * vi + 1] = radius * sin;
            positions[numAxes * vi + 2] = 0f;

            texCoords[2 * vi] = (1f + cos) / 2f;
            texCoords[2 * vi + 1] = (1f + sin) / 2f;
        }

        setBuffer(Type.Normal, numAxes, normals);
        setBuffer(Type.Position, numAxes, positions);
        setBuffer(Type.TexCoord, 2, texCoords);

        updateBound();
        setStatic();
    }
}
