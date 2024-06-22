/*
 Copyright (c) 2020-2024 Stephen Gold
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * A static, Triangles-mode mesh (with indices and normals) that approximates a
 * Y-axis capsule, its domes generated by subdividing the faces of a regular
 * octahedron.
 * <p>
 * The center is at (0,0,0). All triangles face outward with right-handed
 * winding.
 * <p>
 * Derived from Icosphere by jayfella.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CapsuleMesh extends Mesh {
    // *************************************************************************
    // constants and loggers

    /**
     * number of axes in a vector
     */
    final private static int numAxes = 3;
    /**
     * vertex indices of the 8 faces in a regular octahedron (outward-facing
     * triangles with right-handed winding)
     */
    final private static int[] octaIndices = {
        0, 2, 5, //  -X -Y +Z face
        1, 4, 3, //  +X +Y -Z face
        0, 3, 4, //  -X +Y -Z face
        1, 5, 2, //  +X -Y +Z face
        0, 4, 2, //  -X -Y -Z face
        1, 3, 5, //  +X +Y +Z face
        0, 5, 3, //  -X +Y +Z face
        1, 2, 4 //   +X -Y -Z face
    };
    /**
     * number of vertices per triangle
     */
    final private static int vpt = 3;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(CapsuleMesh.class.getName());
    /**
     * vertex locations in a regular octahedron with radius=1
     */
    final private static Vector3f[] octaLocations = {
        new Vector3f(-1f, 0f, 0f), // [0]
        new Vector3f(+1f, 0f, 0f), // [1]
        new Vector3f(0f, -1f, 0f), // [2]
        new Vector3f(0f, +1f, 0f), // [3]
        new Vector3f(0f, 0f, -1f), // [4]
        new Vector3f(0f, 0f, +1f) //  [5]
    };
    // *************************************************************************
    // fields

    /**
     * next vertex index to be assigned
     */
    private int nextVertexIndex = 0;
    /**
     * map vertex indices to the normals of a sphere
     */
    final private List<Vector3f> sphereNormals = new ArrayList<>(258);
    /**
     * cache to avoid duplicate vertices: map index pairs to midpoint indices
     */
    final private Map<Long, Integer> midpointCache = new HashMap<>(252);
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil.
     */
    protected CapsuleMesh() {
    }

    /**
     * Instantiate an Capsule with the specified radius, height, and number of
     * refinement steps:
     *
     * @param numRefineSteps number of refinement steps (&ge;0, &le;13)
     * @param radius radius (in mesh units, &gt;0)
     * @param height height of the cylindrical portion (in mesh units, &gt;0)
     */
    public CapsuleMesh(int numRefineSteps, float radius, float height) {
        Validate.inRange(numRefineSteps, "number of refinement steps", 0, 13);
        Validate.positive(radius, "radius");
        Validate.positive(height, "height");

        // Add the 6 vertices of a regular octahedron with radius=1.
        addVertex(octaLocations[0]);
        addVertex(octaLocations[1]);
        addVertex(octaLocations[2]);
        addVertex(octaLocations[3]);
        addVertex(octaLocations[4]);
        addVertex(octaLocations[5]);

        // Add the 8 triangular faces of a regular octahedron.
        List<Integer> faces = new ArrayList<>(24);
        for (int octaIndex : octaIndices) {
            faces.add(octaIndex);
        }

        for (int stepIndex = 0; stepIndex < numRefineSteps; ++stepIndex) {
            List<Integer> newFaces = new ArrayList<>(4 * faces.size());
            /*
             * a refinement step: divide each edge into 2 halves;
             * for each triangle in {@code faces},
             * add 4 triangles to {@code newFaces}
             */
            for (int j = 0; j < faces.size(); j += vpt) {
                int v1 = faces.get(j);
                int v2 = faces.get(j + 1);
                int v3 = faces.get(j + 2);

                int a = midpointIndex(v1, v2);
                int b = midpointIndex(v2, v3);
                int c = midpointIndex(v3, v1);

                newFaces.add(v1);
                newFaces.add(a);
                newFaces.add(c);

                newFaces.add(v2);
                newFaces.add(b);
                newFaces.add(a);

                newFaces.add(v3);
                newFaces.add(c);
                newFaces.add(b);

                newFaces.add(a);
                newFaces.add(b);
                newFaces.add(c);
            }

            faces = newFaces;
        }

        // System.out.println("numRefineSteps  = " + numRefineSteps);
        // System.out.println("numVertices     = " + locations.size());
        // System.out.println("numFaces        = " + faces.size() / vpt);
        // System.out.println("numCacheEntries = " + midpointCache.size());
        // System.out.println();
        //
        midpointCache.clear();
        assert faces.size() == vpt << (3 + 2 * numRefineSteps);

        // Count the equatoral vertices, which will be doubled in the mesh:
        int numEquatoralVertices = 0;
        for (Vector3f normal : sphereNormals) {
            if (normal.y == 0f) {
                ++numEquatoralVertices;
            }
        }
        Map<Integer, Integer> mapN2S = new HashMap<>(numEquatoralVertices);

        // Create and fill a position buffer and a normal buffer for the mesh:
        int numVertices = sphereNormals.size() + numEquatoralVertices;
        int numFloats = numAxes * numVertices;
        FloatBuffer posBuffer = BufferUtils.createFloatBuffer(numFloats);
        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(numFloats);
        float halfHeight = height / 2f;
        for (Vector3f normal : sphereNormals) {
            posBuffer.put(radius * normal.x);
            if (normal.y >= 0f) {
                posBuffer.put(halfHeight + radius * normal.y);
            } else {
                posBuffer.put(-halfHeight + radius * normal.y);
            }
            posBuffer.put(radius * normal.z);

            normalBuffer.put(normal.x).put(normal.y).put(normal.z);
        }
        for (int northI = 0; northI < sphereNormals.size(); ++northI) {
            Vector3f normal = sphereNormals.get(northI); // alias
            if (normal.y == 0f) { // This is an equatorial vertex, so double it.
                int southIndex = posBuffer.position() / numAxes;
                mapN2S.put(northI, southIndex);

                posBuffer.put(radius * normal.x);
                posBuffer.put(-halfHeight);
                posBuffer.put(radius * normal.z);

                normalBuffer.put(normal.x);
                normalBuffer.put(0f);
                normalBuffer.put(normal.z);
            }
        }
        posBuffer.flip();
        setBuffer(VertexBuffer.Type.Position, numAxes, posBuffer);
        normalBuffer.flip();
        setBuffer(VertexBuffer.Type.Normal, numAxes, normalBuffer);

        // Create an index buffer for the mesh:
        int numIndices = faces.size() + 6 * numEquatoralVertices;
        IndexBuffer ib = IndexBuffer.createIndexBuffer(numVertices, numIndices);

        // Put indices for the dome triangles:
        int numDomeTriangles = faces.size() / vpt;
        assert vpt * numDomeTriangles == faces.size();
        for (int triangleI = 0; triangleI < numDomeTriangles; ++triangleI) {
            int vi0 = faces.get(vpt * triangleI);
            int vi1 = faces.get(vpt * triangleI + 1);
            int vi2 = faces.get(vpt * triangleI + 2);

            float v0y = sphereNormals.get(vi0).y;
            float v1y = sphereNormals.get(vi1).y;
            float v2y = sphereNormals.get(vi2).y;
            if (v0y == 0f || v1y == 0f || v2y == 0f) { // triangle along equator
                if (v0y < 0f || v1y < 0 || v2y < 0f) { // lies on the -Y side
                    if (v0y == 0f) {
                        vi0 = mapN2S.get(vi0);
                    }
                    if (v1y == 0f) {
                        vi1 = mapN2S.get(vi1);
                    }
                    if (v2y == 0f) {
                        vi2 = mapN2S.get(vi2);
                    }
                }
            }

            ib.put(vi0);
            ib.put(vi1);
            ib.put(vi2);
        }

        // Sort the indices of vertices on the cylinder's +Y rim:
        Integer[] northIndices = mapN2S.keySet().toArray(new Integer[0]);
        Arrays.sort(northIndices, new Comparator<Integer>() {
            @Override
            public int compare(Integer vi1, Integer vi2) {
                Vector3f sn1 = sphereNormals.get(vi1); // alias
                double atan1 = Math.atan2(sn1.x, sn1.z);
                Vector3f sn2 = sphereNormals.get(vi2); // alias
                double atan2 = Math.atan2(sn2.x, sn2.z);

                int result = Double.compare(atan1, atan2);
                return result;
            }
        });
        sphereNormals.clear();

        // Put indices for cylinder triangles:
        for (int evI = 0; evI < numEquatoralVertices - 1; ++evI) {
            int ni0 = northIndices[evI];
            int ni1 = northIndices[evI + 1];
            int si0 = mapN2S.get(ni0);
            int si1 = mapN2S.get(ni1);

            ib.put(si0).put(si1).put(ni1);
            ib.put(si0).put(ni1).put(ni0);
        }
        int ni0 = northIndices[numEquatoralVertices - 1];
        int ni1 = northIndices[0];
        int si0 = mapN2S.get(ni0);
        int si1 = mapN2S.get(ni1);
        mapN2S.clear();

        ib.put(si0).put(si1).put(ni1);
        ib.put(si0).put(ni1).put(ni0);

        VertexBuffer.Format ibFormat = ib.getFormat();
        Buffer ibData = ib.getBuffer();
        ibData.flip();
        setBuffer(VertexBuffer.Type.Index, vpt, ibFormat, ibData);

        updateBound();
        setStatic();
    }
    // *************************************************************************
    // private methods

    /**
     * Add a vertex to the list of sphere normals.
     *
     * @param normal the approximate vertex normal (in mesh coordinates, not
     * null, unaffected)
     * @return the index assigned to the new vertex (&ge;0)
     */
    private int addVertex(Vector3f normal) {
        float length = normal.length();
        sphereNormals.add(normal.mult(1f / length));

        int result = nextVertexIndex;
        ++nextVertexIndex;

        return result;
    }

    /**
     * Determine the index of the vertex halfway between the indexed vertices.
     *
     * @param p1 the index of the first input vertex (&ge;0)
     * @param p2 the index of the 2nd input vertex (&ge;0)
     * @return the midpoint index (&ge;0)
     */
    private int midpointIndex(int p1, int p2) {
        // Check whether the midpoint has already been assigned an index.
        boolean firstIsSmaller = p1 < p2;
        long smallerIndex = firstIsSmaller ? p1 : p2;
        long greaterIndex = firstIsSmaller ? p2 : p1;
        long key = (smallerIndex << 32) + greaterIndex;
        Integer cachedIndex = midpointCache.get(key);
        if (cachedIndex != null) {
            return cachedIndex;
        }

        // The midpoint vertex is not in the cache: calculate its location.
        Vector3f sn1 = sphereNormals.get(p1);
        Vector3f sn2 = sphereNormals.get(p2);
        Vector3f middleNormal = MyVector3f.midpoint(sn1, sn2, null);

        // addVertex() scales the midpoint location to unit sphere.
        int newIndex = addVertex(middleNormal);

        // Add the new vertex to the midpoint cache:
        midpointCache.put(key, newIndex);

        return newIndex;
    }
}
