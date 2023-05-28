/*
 Copyright (c) 2017-2023, Stephen Gold
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

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Triangle;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.CollisionData;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.util.BufferUtils;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import jme3utilities.math.IntPair;
import jme3utilities.math.MyBuffer;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.MyVector3f;
import jme3utilities.math.VectorSet;
import jme3utilities.math.VectorSetUsingBuffer;

/**
 * Utility methods for meshes and mesh vertices.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class MyMesh { // TODO finalize the class
    // *************************************************************************
    // constants and loggers

    /**
     * maximum number of bones/joints that can influence any one vertex
     */
    final private static int maxWeights = 4;
    /**
     * number of axes in a vector
     */
    final private static int numAxes = 3;
    /**
     * number of vertices per edge
     */
    final public static int vpe = 2;
    /**
     * number of vertices per triangle
     */
    final public static int vpt = 3;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MyMesh.class.getName());
    /**
     * local copy of {@link com.jme3.math.Matrix4f#IDENTITY}
     */
    final private static Matrix4f matrixIdentity = new Matrix4f();
    /**
     * scale factors to reverse the direction of a vector
     */
    final private static Vector3f scaleReverse = new Vector3f(-1f, -1f, -1f);
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private MyMesh() {
    }
    // *************************************************************************
    // new methods exposed
    // TODO add isConnected(), makeDoubleSided(), and transform()

    /**
     * Compress a Mesh by introducing an index buffer.
     *
     * @param input the input Mesh (not null, without an index buffer)
     * @return a new Mesh (with an index buffer)
     */
    public static Mesh addIndices(Mesh input) {
        Validate.nonNull(input, "input mesh");
        Validate.require(!hasIndices(input), "not have an index buffer");
        /*
         * Assign new indices and create mappings between
         * the old and new indices.
         */
        int oldN = input.getVertexCount();
        int[] old2new = new int[oldN];
        int[] new2old = new int[oldN];
        int newN = 0;
        for (int oldI = 0; oldI < oldN; ++oldI) {
            old2new[oldI] = -1;
            new2old[oldI] = -1;

            for (int newI = 0; newI < newN; ++newI) {
                if (areIdentical(input, oldI, new2old[newI])) {
                    old2new[oldI] = newI;
                    break;
                }
            }
            if (old2new[oldI] == -1) { // allocate a vertex index
                old2new[oldI] = newN;
                new2old[newN] = oldI;
                ++newN;
            }
        }

        // Create a clone of the input mesh with smaller vertex buffers.
        Mesh result = input.clone();
        for (VertexBuffer oldVertexBuffer : input.getBufferList()) {
            VertexBuffer.Type type = oldVertexBuffer.getBufferType();
            result.clearBuffer(type);

            VertexBuffer.Format format = oldVertexBuffer.getFormat();
            if (format == null) {
                format = VertexBuffer.Format.Float; // to avoid an NPE
            }
            int numCperE = oldVertexBuffer.getNumComponents();
            numCperE = MyMath.clamp(numCperE, 1, 4); // to avoid an IAE
            Buffer data = VertexBuffer.createBuffer(format, numCperE, newN);
            result.setBuffer(type, numCperE, format, data);
        }

        // Copy vertex data from the input mesh to the new mesh.
        for (int newI = 0; newI < newN; ++newI) {
            int oldI = new2old[newI];
            for (VertexBuffer newVB : result.getBufferList()) {
                VertexBuffer.Type type = newVB.getBufferType();
                VertexBuffer oldVB = input.getBuffer(type);
                assert oldVB != newVB;
                if (oldVB.getNumElements() > 0) {
                    Element.copy(oldVB, oldI, newVB, newI);
                }
            }
        }

        // Create the index buffer and fill it with indices.
        IndexBuffer ib = IndexBuffer.createIndexBuffer(newN, oldN);
        for (int oldI = 0; oldI < oldN; ++oldI) {
            int newI = old2new[oldI];
            ib.put(oldI, newI);
        }
        VertexBuffer.Format ibFormat = ib.getFormat();
        Buffer ibData = ib.getBuffer();
        result.setBuffer(VertexBuffer.Type.Index, 1, ibFormat, ibData);

        // Flip each buffer.
        for (VertexBuffer outVB : result.getBufferList()) {
            Buffer data = outVB.getData();
            int endPosition = data.capacity();
            data.position(endPosition);
            data.flip();
        }

        result.updateCounts();

        assert hasIndices(result);
        return result;
    }

    /**
     * Add normals to a Mesh for an outward-facing sphere.
     *
     * @param mesh the Mesh to modify (not null, without normals)
     */
    public static void addSphereNormals(Mesh mesh) {
        Validate.nonNull(mesh, "mesh");
        Validate.require(!hasAnyNormals(mesh), "not have normals");

        generateSphereNormals(
                mesh, VertexBuffer.Type.Normal, VertexBuffer.Type.Position);

        VertexBuffer bpPosition
                = mesh.getBuffer(VertexBuffer.Type.BindPosePosition);
        if (bpPosition != null) {
            generateSphereNormals(mesh, VertexBuffer.Type.BindPoseNormal,
                    VertexBuffer.Type.BindPosePosition);
        }
    }

    /**
     * Test whether specified vertices in the specified Mesh are identical.
     *
     * @param mesh (not null, unaffected)
     * @param vi1 the index of the first vertex (&ge;0)
     * @param vi2 the index of the 2nd vertex (&ge;0)
     * @return true if identical, otherwise false
     */
    public static boolean areIdentical(Mesh mesh, int vi1, int vi2) {
        Validate.nonNull(mesh, "mesh");
        int numVertices = mesh.getVertexCount();
        Validate.inRange(vi1, "first vertex index", 0, numVertices - 1);
        Validate.inRange(vi2, "2nd vertex index", 0, numVertices - 1);

        if (vi1 == vi2) {
            return true;
        }

        for (VertexBuffer vertexBuffer : mesh.getBufferList()) {
            VertexBuffer.Type type = vertexBuffer.getBufferType();
            if (type != VertexBuffer.Type.Index
                    && vertexBuffer.getNumElements() > 0) {
                if (!Element.equals(vertexBuffer, vi1, vi2)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Generate a material to visualize the bone weights in the specified Mesh.
     *
     * @param mesh the subject mesh (not null, animated, modified)
     * @param boneIndexToColor map bone indices to colors (not null, unaffected)
     * @param assetManager (not null)
     * @return a new wireframe material instance
     */
    public static Material boneWeightMaterial(Mesh mesh,
            ColorRGBA[] boneIndexToColor, AssetManager assetManager) {
        if (!isAnimated(mesh)) {
            throw new IllegalArgumentException("Must be an animated mesh.");
        }

        int numVertices = mesh.getVertexCount();
        FloatBuffer colorBuf = BufferUtils.createFloatBuffer(4 * numVertices);

        int[] biArray = new int[maxWeights];
        float[] bwArray = new float[maxWeights];
        ColorRGBA sum = new ColorRGBA();
        ColorRGBA term = new ColorRGBA();
        for (int vertexIndex = 0; vertexIndex < numVertices; ++vertexIndex) {
            vertexBoneIndices(mesh, vertexIndex, biArray);
            vertexBoneWeights(mesh, vertexIndex, bwArray);
            sum.set(0f, 0f, 0f, 1f);
            for (int j = 0; j < maxWeights; ++j) {
                int boneI = biArray[j];
                if (boneI >= 0 && boneI < boneIndexToColor.length) {
                    term.set(boneIndexToColor[boneI]);
                    float weight = bwArray[j];
                    term.multLocal(weight);
                    sum.addLocal(term);
                }
            }
            colorBuf.put(sum.r).put(sum.g).put(sum.b).put(1f);
        }

        mesh.setBuffer(VertexBuffer.Type.Color, 4, VertexBuffer.Format.Float,
                colorBuf);

        Material material = MyAsset.createUnshadedMaterial(assetManager);
        material.setBoolean("VertexColor", true);

        RenderState rs = material.getAdditionalRenderState();
        rs.setWireframe(true);

        return material;
    }

    /**
     * Translate the specified VertexBuffer so that the center of its
     * axis-aligned bounding box is at (0, 0, 0).
     *
     * @param mesh the subject mesh (not null)
     * @param bufferType which buffer to modify (not null)
     */
    public static void centerBuffer(Mesh mesh, VertexBuffer.Type bufferType) {
        Validate.nonNull(bufferType, "buffer type");

        VertexBuffer vertexBuffer = mesh.getBuffer(bufferType);
        if (vertexBuffer != null) {
            FloatBuffer floatBuffer = (FloatBuffer) vertexBuffer.getData();
            Vector3f max = new Vector3f();
            Vector3f min = new Vector3f();
            int numVertices = mesh.getVertexCount();
            int numFloats = numAxes * numVertices;
            MyBuffer.maxMin(floatBuffer, 0, numFloats, max, min);
            Vector3f offset = MyVector3f.midpoint(max, min, null).negateLocal();
            MyBuffer.translate(floatBuffer, 0, numFloats, offset);

            vertexBuffer.setUpdateNeeded();
        }
    }

    /**
     * Estimate the number of bones in the specified Mesh by reading its
     * bone-index buffers.
     *
     * @param mesh the Mesh to examine (not null)
     * @return an estimated number of bones (&ge;0)
     */
    public static int countBones(Mesh mesh) {
        int maxWeightsPerVert = mesh.getMaxNumWeights();
        Validate.inRange(
                maxWeightsPerVert, "mesh max num weights", 1, maxWeights);

        VertexBuffer biBuf = mesh.getBuffer(VertexBuffer.Type.BoneIndex);
        Buffer boneIndexBuffer = biBuf.getDataReadOnly();
        boneIndexBuffer.rewind();
        int numBoneIndices = boneIndexBuffer.remaining();
        assert numBoneIndices % maxWeights == 0 : numBoneIndices;
        int numVertices = boneIndexBuffer.remaining() / maxWeights;

        FloatBuffer weightBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.BoneWeight);
        weightBuffer.rewind();
        int numWeights = weightBuffer.remaining();
        assert numWeights == numVertices * maxWeights : numWeights;

        int result = 0;
        for (int vIndex = 0; vIndex < numVertices; ++vIndex) {
            for (int wIndex = 0; wIndex < maxWeights; ++wIndex) {
                float weight = weightBuffer.get();
                int boneIndex = MyBuffer.readIndex(boneIndexBuffer);
                if (wIndex < maxWeightsPerVert && weight != 0f
                        && boneIndex >= result) {
                    result = boneIndex + 1;
                }
            }
        }

        assert result >= 0 : result;
        return result;
    }

    /**
     * Expand a Mesh to ensure that no vertex data are re-used. Any index buffer
     * is eliminated and any loop/strip/fan mode is changed to Triangles or
     * Lines.
     *
     * @param in the input mesh (not null, mode not Hybrid or Patch, unaffected)
     * @return a new Mesh (without an index buffer, in one of the list modes)
     */
    public static Mesh expand(Mesh in) {
        IndexBuffer indexList = in.getIndicesAsList();
        int outVertexCount = indexList.size();
        Mesh.Mode outMode = expandedMode(in);

        // Create a shallow clone of the input mesh.
        Mesh out = in.clone();
        out.setMode(outMode);

        for (VertexBuffer inVertexBuffer : in.getBufferList()) {
            VertexBuffer.Type type = inVertexBuffer.getBufferType();
            out.clearBuffer(type);

            if (type != VertexBuffer.Type.Index) {
                VertexBuffer.Format format = inVertexBuffer.getFormat();
                if (format == null) {
                    format = VertexBuffer.Format.Float; // to avoid an NPE
                }
                int numCperE = inVertexBuffer.getNumComponents();
                numCperE = MyMath.clamp(numCperE, 1, 4); // to avoid an IAE
                Buffer data = VertexBuffer
                        .createBuffer(format, numCperE, outVertexCount);
                out.setBuffer(type, numCperE, format, data);
            }
        }

        // Copy all vertex data to the new Mesh.
        for (int outVI = 0; outVI < outVertexCount; ++outVI) {
            int inVI = indexList.get(outVI);
            for (VertexBuffer outVB : out.getBufferList()) {
                VertexBuffer.Type type = outVB.getBufferType();
                VertexBuffer inVB = in.getBuffer(type);
                assert inVB != outVB;
                if (inVB.getNumElements() > 0) {
                    Element.copy(inVB, inVI, outVB, outVI);
                }
            }
        }

        // Flip each buffer.
        for (VertexBuffer outVB : out.getBufferList()) {
            Buffer data = outVB.getData();
            int endPosition = data.capacity();
            data.position(endPosition);
            data.flip();
        }

        out.updateCounts();

        assert out.getMode().isListMode();
        assert !hasIndices(out);
        return out;
    }

    /**
     * Determine the type of primitives contained in the specified Mesh.
     *
     * @param inputMesh the Mesh to analyze (not null, mode not Hybrid or Patch,
     * unaffected)
     * @return an enum value (one of the list modes)
     * @see #hasTriangles(com.jme3.scene.Mesh)
     */
    public static Mesh.Mode expandedMode(Mesh inputMesh) {
        Mesh.Mode mode = inputMesh.getMode();
        Mesh.Mode result;
        switch (mode) {
            case Points:
            case Lines:
            case Triangles:
                result = mode;
                break;

            case LineLoop:
            case LineStrip:
                result = Mesh.Mode.Lines;
                break;

            case TriangleFan:
            case TriangleStrip:
                result = Mesh.Mode.Triangles;
                break;

            default:
                throw new IllegalArgumentException("mode = " + mode);
        }

        assert result.isListMode();
        return result;
    }

    /**
     * Generate normals on a triangle-by-triangle basis for a Triangles-mode
     * Mesh without an index buffer. Pre-existing normal buffers are discarded.
     *
     * @param mesh the Mesh to modify (not null, mode=Triangles, not indexed)
     */
    public static void generateFacetNormals(Mesh mesh) {
        Validate.require(
                mesh.getMode() == Mesh.Mode.Triangles, "be in Triangles mode");
        Validate.require(!hasIndices(mesh), "not have an index buffer");

        generateFacetNormals(
                mesh, VertexBuffer.Type.Normal, VertexBuffer.Type.Position);

        VertexBuffer bpPosition
                = mesh.getBuffer(VertexBuffer.Type.BindPosePosition);
        if (bpPosition != null) {
            generateFacetNormals(mesh, VertexBuffer.Type.BindPoseNormal,
                    VertexBuffer.Type.BindPosePosition);
        }
    }

    /**
     * Generate facet normals for a Triangles-mode Mesh without an index buffer.
     * Any pre-existing target buffer is discarded.
     *
     * @param mesh the Mesh to modify (not null, mode=Triangles, not indexed)
     * @param normalBufferType the target buffer type (Normal or BindPoseNormal)
     * @param positionBufferType the source buffer type (Position or
     * BindPosePosition)
     */
    public static void generateFacetNormals(Mesh mesh,
            VertexBuffer.Type normalBufferType,
            VertexBuffer.Type positionBufferType) {
        Validate.nonNull(mesh, "mesh");
        Validate.require(mesh.getMode() == Mesh.Mode.Triangles,
                "be in Triangles mode");
        Validate.require(!hasIndices(mesh), "not have an index buffer");
        Validate.require(normalBufferType == VertexBuffer.Type.BindPoseNormal
                || normalBufferType == VertexBuffer.Type.Normal,
                "normal target-buffer type");
        Validate.require(
                positionBufferType == VertexBuffer.Type.BindPosePosition
                || positionBufferType == VertexBuffer.Type.Position,
                "position source-buffer type");

        FloatBuffer positionBuffer = mesh.getFloatBuffer(positionBufferType);
        int numFloats = positionBuffer.limit();

        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(numFloats);
        mesh.setBuffer(normalBufferType, numAxes, normalBuffer);

        Triangle triangle = new Triangle();
        Vector3f pos1 = new Vector3f();
        Vector3f pos2 = new Vector3f();
        Vector3f pos3 = new Vector3f();

        int numTriangles = numFloats / vpt / numAxes;
        for (int triIndex = 0; triIndex < numTriangles; ++triIndex) {
            int trianglePosition = triIndex * vpt * numAxes;
            MyBuffer.get(positionBuffer, trianglePosition, pos1);
            MyBuffer.get(positionBuffer, trianglePosition + numAxes, pos2);
            MyBuffer.get(positionBuffer, trianglePosition + 2 * numAxes, pos3);
            triangle.set(pos1, pos2, pos3);

            Vector3f normal = triangle.getNormal();
            for (int j = 0; j < vpt; ++j) {
                normalBuffer.put(normal.x);
                normalBuffer.put(normal.y);
                normalBuffer.put(normal.z);
            }
        }
        normalBuffer.flip();
    }

    /**
     * Generate normals on a triangle-by-triangle basis for a Triangles-mode
     * Mesh without an index buffer. Pre-existing normal buffers are discarded.
     *
     * @param mesh the Mesh to modify (not null, mode=Triangles, not indexed)
     * @deprecated use {@link #generateFacetNormals(com.jme3.scene.Mesh)}
     */
    @Deprecated
    public static void generateNormals(Mesh mesh) {
        Validate.require(
                mesh.getMode() == Mesh.Mode.Triangles, "be in Triangles mode");
        Validate.require(!hasIndices(mesh), "not have an index buffer");
        generateFacetNormals(mesh);
    }

    /**
     * Generate sphere normals for a Mesh. Any pre-existing target buffer is
     * discarded.
     *
     * @param mesh the Mesh to modify (not null)
     * @param normalBufferType the target buffer type (Normal or BindPoseNormal)
     * @param positionBufferType the source buffer type (Position or
     * BindPosePosition)
     */
    public static void generateSphereNormals(
            Mesh mesh, VertexBuffer.Type normalBufferType,
            VertexBuffer.Type positionBufferType) {
        Validate.nonNull(mesh, "mesh");

        FloatBuffer positionBuffer = mesh.getFloatBuffer(positionBufferType);
        int numFloats = positionBuffer.limit();

        FloatBuffer normalBuffer = BufferUtils.clone(positionBuffer);
        mesh.setBuffer(normalBufferType, numAxes, normalBuffer);

        MyBuffer.normalize(normalBuffer, 0, numFloats);
        normalBuffer.limit(numFloats);
    }

    /**
     * Access the collision tree of the specified Mesh
     *
     * @param mesh the Mesh to access (not null, unaffected)
     * @return the pre-existing instance, or null if none
     */
    public static CollisionData getCollisionTree(Mesh mesh) {
        Field field;
        try {
            field = Mesh.class.getDeclaredField("collisionTree");
        } catch (NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
        field.setAccessible(true);

        CollisionData result;
        try {
            result = (CollisionData) field.get(mesh);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }

        return result;
    }

    /**
     * Test whether the specified Mesh has vertex normals, including bind-pose
     * normals.
     *
     * @param mesh the Mesh to test (not null, unaffected)
     * @return true if the Mesh has vertex normals, otherwise false
     */
    public static boolean hasAnyNormals(Mesh mesh) {
        VertexBuffer buffer = mesh.getBuffer(VertexBuffer.Type.BindPoseNormal);
        if (buffer == null && !hasNormals(mesh)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the specified Mesh has vertex tangents, including bind-pose
     * tangents.
     *
     * @param mesh the Mesh to test (not null, unaffected)
     * @return true if the Mesh has vertex tangents, otherwise false
     */
    public static boolean hasAnyTangents(Mesh mesh) {
        VertexBuffer buffer = mesh.getBuffer(VertexBuffer.Type.BindPoseTangent);
        if (buffer == null && !hasTangents(mesh)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the specified Mesh has an index buffer.
     *
     * @param mesh the Mesh to test (not null, unaffected)
     * @return true if the Mesh has indices, otherwise false
     */
    public static boolean hasIndices(Mesh mesh) {
        VertexBuffer buffer = mesh.getBuffer(VertexBuffer.Type.Index);
        if (buffer == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the specified Mesh has vertex normals, not including
     * bind-pose normals.
     *
     * @param mesh the Mesh to test (not null, unaffected)
     * @return true if the Mesh has vertex normals, otherwise false
     */
    public static boolean hasNormals(Mesh mesh) {
        VertexBuffer buffer = mesh.getBuffer(VertexBuffer.Type.Normal);
        if (buffer == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the specified Mesh has vertex tangents, not including
     * bind-pose tangents.
     *
     * @param mesh the Mesh to test (not null, unaffected)
     * @return true if the Mesh has vertex tangents, otherwise false
     */
    public static boolean hasTangents(Mesh mesh) {
        VertexBuffer buffer = mesh.getBuffer(VertexBuffer.Type.Tangent);
        if (buffer == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the specified Mesh is composed of triangles.
     *
     * @param mesh the Mesh to test (not null, unaffected)
     * @return true if the Mesh is composed of triangles, otherwise false
     */
    public static boolean hasTriangles(Mesh mesh) {
        Mesh.Mode mode = mesh.getMode();
        boolean result;
        switch (mode) {
            case Points:
            case Lines:
            case LineStrip:
            case LineLoop:
                result = false;
                break;

            case Triangles:
            case TriangleFan:
            case TriangleStrip:
                result = true;
                break;

            default:
                String message = "mode = " + mode;
                throw new IllegalArgumentException(message);
        }

        return result;
    }

    /**
     * Test whether the specified Mesh has texture (U-V) coordinates.
     *
     * @param mesh the Mesh to test (not null, unaffected)
     * @return true if the Mesh has texture coordinates, otherwise false
     */
    public static boolean hasUV(Mesh mesh) {
        VertexBuffer buffer = mesh.getBuffer(VertexBuffer.Type.TexCoord);
        if (buffer == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the specified Mesh is bone animated. Unlike
     * {@code Mesh.isAnimated()}, this method checks for bone weights and
     * ignores hardware buffers.
     *
     * @param mesh which Mesh to test (not null, unaffected)
     * @return true if animated, otherwise false
     */
    public static boolean isAnimated(Mesh mesh) {
        VertexBuffer indices = mesh.getBuffer(VertexBuffer.Type.BoneIndex);
        boolean hasIndices = indices != null;

        VertexBuffer weights = mesh.getBuffer(VertexBuffer.Type.BoneWeight);
        boolean hasWeights = weights != null;

        boolean result = hasIndices && hasWeights;
        return result;
    }

    /**
     * Enumerate all meshes in the specified subtree of a scene graph. Note:
     * recursive!
     *
     * @param subtree (may be null, aliases created)
     * @param storeResult storage for results (added to if not null)
     * @return an expanded list (either storeResult or a new instance)
     */
    public static List<Mesh> listMeshes(
            Spatial subtree, List<Mesh> storeResult) {
        List<Mesh> result = (storeResult == null)
                ? new ArrayList<Mesh>(10) : storeResult;

        if (subtree instanceof Geometry) {
            Geometry geometry = (Geometry) subtree;
            Mesh mesh = geometry.getMesh();
            if (!result.contains(mesh)) {
                result.add(mesh);
            }

        } else if (subtree instanceof Node) {
            Node node = (Node) subtree;
            List<Spatial> children = node.getChildren();
            for (Spatial child : children) {
                listMeshes(child, result);
            }
        }

        return result;
    }

    /**
     * Enumerate the world locations of all vertices in the specified subtree of
     * a scene graph. Note: recursive!
     *
     * @param subtree (may be null)
     * @param storeResult storage for results (added to if not null)
     * @return the resulting set (either storeResult or a new instance)
     */
    public static VectorSet listVertexLocations(
            Spatial subtree, VectorSet storeResult) {
        VectorSet result;
        if (storeResult == null) {
            int numVectors = 64;
            boolean direct = false;
            result = new VectorSetUsingBuffer(numVectors, direct);
        } else {
            result = storeResult;
        }

        if (subtree instanceof Geometry) {
            Geometry geometry = (Geometry) subtree;
            Mesh mesh = geometry.getMesh();
            int numVertices = mesh.getVertexCount();
            Vector3f tempLocation = new Vector3f();
            for (int vertexI = 0; vertexI < numVertices; ++vertexI) {
                vertexVector3f(mesh, VertexBuffer.Type.Position, vertexI,
                        tempLocation);
                if (!geometry.isIgnoreTransform()) {
                    geometry.localToWorld(tempLocation, tempLocation);
                }
                result.add(tempLocation);
            }

        } else if (subtree instanceof Node) {
            Node node = (Node) subtree;
            List<Spatial> children = node.getChildren();
            for (Spatial child : children) {
                listVertexLocations(child, result);
            }
        }

        return result;
    }

    /**
     * Merge 2 meshes.
     *
     * Doesn't handle levels of detail, hybrid meshes, or tessellation meshes.
     *
     * Both meshes must be composed of the same sort of primitives. In other
     * words, if mesh1 is composed of triangles, then so must mesh2, and vice
     * versa.
     *
     * Apart from indices, both meshes must have the same set of vertex buffers.
     * In other words, if mesh1 has normals, UVs, and bone animations, then so
     * must mesh2, and vice versa. Furthermore, corresponding vertex buffers
     * must have the same number of components per element.
     *
     * @param mesh1 the first input mesh (not null, unaffected)
     * @param mesh2 the 2nd input mesh (not null, unaffected)
     * @return a new Mesh (without an index buffer, in one of the list modes)
     */
    public static Mesh merge(Mesh mesh1, Mesh mesh2) {
        int levels1 = mesh1.getNumLodLevels();
        Validate.require(levels1 == 0, "no LODs in mesh1");
        int levels2 = mesh1.getNumLodLevels();
        Validate.require(levels2 == 0, "no LODs in mesh2");

        Mesh.Mode outMode = expandedMode(mesh1);
        Mesh.Mode outMode2 = expandedMode(mesh2);
        Validate.require(outMode == outMode2, "same primitives");

        Mesh result = new Mesh();
        result.setMode(outMode);

        IndexBuffer indexList1 = mesh1.getIndicesAsList();
        IndexBuffer indexList2 = mesh2.getIndicesAsList();
        int numVertices1 = indexList1.size();
        int numVertices2 = indexList2.size();
        int outNumVertices = numVertices1 + numVertices2;

        for (VertexBuffer.Type type : VertexBuffer.Type.values()) {
            if (type == VertexBuffer.Type.Index) {
                continue;
            }
            VertexBuffer vb1 = mesh1.getBuffer(type);
            VertexBuffer vb2 = mesh2.getBuffer(type);
            if (vb1 == null && vb2 == null) {
                continue;
            }
            /*
             * If one input mesh includes the buffer, then so must the other,
             * and the components per element must be equal.
             */
            Validate.nonNull(vb1, "mesh1's " + type);
            Validate.nonNull(vb2, "mesh2's " + type);
            int numCperE = vb1.getNumComponents();
            int numCperE2 = vb2.getNumComponents();
            assert numCperE2 == numCperE : "numComponents differ in " + type;

            // If the buffer formats differ, use the larger one for output.
            VertexBuffer.Format format1 = vb1.getFormat();
            if (format1 == null) {
                format1 = VertexBuffer.Format.Float; // to avoid an NPE
            }
            VertexBuffer.Format format2 = vb2.getFormat();
            if (format2 == null) {
                format2 = VertexBuffer.Format.Float; // to avoid an NPE
            }
            VertexBuffer.Format outFormat;
            if (format1.getComponentSize() > format2.getComponentSize()) {
                outFormat = format1;
            } else {
                outFormat = format2;
            }

            // Create the vertex buffer for output.
            numCperE = MyMath.clamp(numCperE, 1, 4); // to avoid an IAE
            Buffer outBuffer = VertexBuffer
                    .createBuffer(outFormat, numCperE, outNumVertices);
            result.setBuffer(type, numCperE, outFormat, outBuffer);
            VertexBuffer outVb = result.getBuffer(type);
            /*
             * Perform a component-by-component copy from the input buffers
             * to the output buffer.
             */
            for (int vertexI = 0; vertexI < numVertices1; ++vertexI) {
                int elementIndex = indexList1.get(vertexI);
                for (int componentI = 0; componentI < numCperE; ++componentI) {
                    Object value
                            = vb1.getElementComponent(elementIndex, componentI);
                    outVb.setElementComponent(vertexI, componentI, value);
                }
            }
            for (int vertexI = 0; vertexI < numVertices2; ++vertexI) {
                int outIndex = numVertices1 + vertexI;
                int vb2Index = indexList2.get(vertexI);
                for (int componentI = 0; componentI < numCperE; ++componentI) {
                    Object value
                            = vb2.getElementComponent(vb2Index, componentI);
                    outVb.setElementComponent(outIndex, componentI, value);
                }
            }
        }

        result.updateBound();
        int maxNumWeights1 = mesh1.getMaxNumWeights();
        int maxNumWeights2 = mesh2.getMaxNumWeights();
        int maxNumWeights = Math.max(maxNumWeights1, maxNumWeights2);
        result.setMaxNumWeights(maxNumWeights);

        assert result.getMode().isListMode();
        assert !hasIndices(result);
        return result;
    }

    /**
     * Count how many vertices in the specified Mesh are directly influenced by
     * the indexed bone.
     *
     * @param mesh the Mesh to analyze (not null, possibly modified)
     * @param boneIndex which bone (&ge;0)
     * @return count (&ge;0)
     */
    public static int numInfluenced(Mesh mesh, int boneIndex) {
        Validate.nonNegative(boneIndex, "bone index");

        int maxWeightsPerVert = mesh.getMaxNumWeights();
        assert maxWeightsPerVert > 0 : maxWeightsPerVert;
        assert maxWeightsPerVert <= maxWeights : maxWeightsPerVert;

        VertexBuffer biBuf = mesh.getBuffer(VertexBuffer.Type.BoneIndex);
        Buffer boneIndexBuffer = biBuf.getDataReadOnly();
        boneIndexBuffer.rewind();
        int numBoneIndices = boneIndexBuffer.remaining();
        assert numBoneIndices % maxWeights == 0 : numBoneIndices;
        int numVertices = boneIndexBuffer.remaining() / maxWeights;

        FloatBuffer weightBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.BoneWeight);
        weightBuffer.rewind();
        int numWeights = weightBuffer.remaining();
        assert numWeights == numVertices * maxWeights : numWeights;

        int result = 0;
        for (int vIndex = 0; vIndex < numVertices; ++vIndex) {
            for (int wIndex = 0; wIndex < maxWeights; ++wIndex) {
                float weight = weightBuffer.get();
                int bIndex = MyBuffer.readIndex(boneIndexBuffer);
                if (wIndex < maxWeightsPerVert
                        && bIndex == boneIndex
                        && weight != 0f) {
                    ++result;
                }
            }
        }

        return result;
    }

    /**
     * Reverse the normals of a Mesh. Apply this method (for instance) after
     * reversing the winding order of a triangle mesh.
     *
     * @param mesh the Mesh to modify (not null)
     */
    public static void reverseNormals(Mesh mesh) {
        FloatBuffer buffer = mesh.getFloatBuffer(VertexBuffer.Type.Normal);
        if (buffer != null) {
            MyBuffer.scale(buffer, 0, buffer.limit(), scaleReverse);
        }

        buffer = mesh.getFloatBuffer(VertexBuffer.Type.BindPoseNormal);
        if (buffer != null) {
            MyBuffer.scale(buffer, 0, buffer.limit(), scaleReverse);
        }
    }

    /**
     * Reverse the winding order of a Triangles-mode Mesh, but don't reverse its
     * normals.
     *
     * @param mesh the Mesh to modify (not null, mode=Triangles)
     */
    public static void reverseWinding(Mesh mesh) {
        Validate.require(
                mesh.getMode() == Mesh.Mode.Triangles, "be in Triangles mode");

        mesh.updateCounts();
        int numTriangles = mesh.getTriangleCount();

        IndexBuffer indexBuffer = mesh.getIndexBuffer();
        if (indexBuffer != null) { // a Mesh with indices
            int numIndices = vpt * numTriangles;
            assert indexBuffer.size() == numIndices : indexBuffer.size();
            for (int triIndex = 0; triIndex < numTriangles; ++triIndex) {
                int v1Index = vpt * triIndex;
                int v3Index = (vpt * triIndex + vpt - 1);
                int i1 = indexBuffer.get(v1Index);
                int i3 = indexBuffer.get(v3Index);
                indexBuffer.put(v1Index, i3);
                indexBuffer.put(v3Index, i1);
            }

        } else { // a Mesh without indices
            int numVertices = vpt * numTriangles;
            for (VertexBuffer vb : mesh.getBufferList()) {
                assert vb.getNumElements() == numVertices : vb.getNumElements();
                for (int triIndex = 0; triIndex < numTriangles; ++triIndex) {
                    int v1Index = vpt * triIndex;
                    int v3Index = vpt * triIndex + vpt - 1;
                    Element.swap(vb, v1Index, v3Index);
                }
            }
        }
    }

    /**
     * Apply the specified rotation to the specified Mesh. This is not
     * recommended for animated meshes.
     *
     * @param mesh the Mesh to rotate (not null, modified)
     * @param rotation the rotation to apply (not null, unaffected)
     */
    public static void rotate(Mesh mesh, Quaternion rotation) {
        rotateBuffer(mesh, VertexBuffer.Type.Position, rotation);
        rotateBuffer(mesh, VertexBuffer.Type.BindPosePosition, rotation);
        rotateBuffer(mesh, VertexBuffer.Type.Normal, rotation);
        rotateBuffer(mesh, VertexBuffer.Type.BindPoseNormal, rotation);
        // TODO binormals?
        mesh.updateBound();
    }

    /**
     * Apply the specified rotation to all data in the specified VertexBuffer.
     *
     * @param mesh the subject mesh (not null)
     * @param bufferType which buffer to modify (not null)
     * @param rotation the rotation to apply (not null, unaffected)
     */
    public static void rotateBuffer(
            Mesh mesh, VertexBuffer.Type bufferType, Quaternion rotation) {
        Validate.nonNull(bufferType, "buffer type");
        Validate.nonNull(rotation, "rotation");

        VertexBuffer vertexBuffer = mesh.getBuffer(bufferType);
        if (vertexBuffer != null) {
            FloatBuffer floatBuffer = (FloatBuffer) vertexBuffer.getData();
            int numVertices = mesh.getVertexCount();
            int numFloats = numAxes * numVertices;
            MyBuffer.rotate(floatBuffer, 0, numFloats, rotation);

            vertexBuffer.setUpdateNeeded();
        }
    }

    /**
     * Apply the specified rotation to all data in the specified tangent buffer.
     *
     * @param mesh the subject mesh (not null)
     * @param bufferType which buffer to modify (not null)
     * @param rotation the rotation to apply (not null, unaffected)
     */
    public static void rotateTangentBuffer(
            Mesh mesh, VertexBuffer.Type bufferType, Quaternion rotation) {
        Validate.nonNull(bufferType, "buffer type");
        Validate.nonNull(rotation, "rotation");

        VertexBuffer vertexBuffer = mesh.getBuffer(bufferType);
        if (vertexBuffer != null) {
            FloatBuffer floatBuffer = (FloatBuffer) vertexBuffer.getData();
            int numVertices = mesh.getVertexCount();
            Vector3f tmpV3 = new Vector3f();
            Vector4f tmpV4 = new Vector4f();
            for (int vertexI = 0; vertexI < numVertices; ++vertexI) {
                vertexVector4f(mesh, bufferType, vertexI, tmpV4);

                tmpV3.set(tmpV4.x, tmpV4.y, tmpV4.z);
                MyQuaternion.rotate(rotation, tmpV3, tmpV3);
                tmpV4.x = tmpV3.x;
                tmpV4.y = tmpV3.y;
                tmpV4.z = tmpV3.z;

                int floatIndex = numAxes * vertexI;
                floatBuffer.put(floatIndex, tmpV4.x);
                floatBuffer.put(floatIndex + 1, tmpV4.y);
                floatBuffer.put(floatIndex + 2, tmpV4.z);
                floatBuffer.put(floatIndex + 3, tmpV4.w);
            }

            vertexBuffer.setUpdateNeeded();
        }
    }

    /**
     * Scale all vertex positions uniformly by the specified factor.
     *
     * @param mesh the Mesh to scale (not null, modified)
     * @param factor the scale factor to apply
     */
    public static void scale(Mesh mesh, float factor) {
        int numVertices = mesh.getVertexCount();
        int numFloats = numAxes * numVertices;
        Vector3f scale = new Vector3f(factor, factor, factor);

        VertexBuffer posBuffer = mesh.getBuffer(VertexBuffer.Type.Position);
        FloatBuffer posFloats = (FloatBuffer) posBuffer.getData();
        MyBuffer.scale(posFloats, 0, numFloats, scale);
        posBuffer.setUpdateNeeded();

        VertexBuffer bindPosBuffer
                = mesh.getBuffer(VertexBuffer.Type.BindPosePosition);
        if (bindPosBuffer != null) {
            FloatBuffer bpFloats = (FloatBuffer) bindPosBuffer.getData();
            MyBuffer.scale(bpFloats, 0, numFloats, scale);
            bindPosBuffer.setUpdateNeeded();
        }

        mesh.updateBound();
    }

    /**
     * Replace the BoneIndexBuffer of a Mesh.
     *
     * @param mesh the Mesh to modify (not null)
     * @param wpv the number of bone weights per vertex (&ge;1, &le;4)
     * @param indexBuffer the desired IndexBuffer (not null, alias created)
     */
    public static void setBoneIndexBuffer(
            Mesh mesh, int wpv, IndexBuffer indexBuffer) {
        Validate.nonNull(mesh, "mesh");
        Validate.inRange(wpv, "weights per vertex", 1, maxWeights);

        Buffer buffer = indexBuffer.getBuffer();
        VertexBuffer.Type type = VertexBuffer.Type.BoneIndex;
        if (buffer instanceof ByteBuffer) {
            mesh.setBuffer(type, wpv, (ByteBuffer) buffer);
        } else if (buffer instanceof IntBuffer) {
            mesh.setBuffer(type, wpv, (IntBuffer) buffer);
        } else if (buffer instanceof ShortBuffer) {
            mesh.setBuffer(type, wpv, (ShortBuffer) buffer);
        } else {
            String message = buffer.getClass().getName();
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Smooth the normals of a Mesh by averaging them across all uses of each
     * distinct vertex position.
     *
     * @param mesh the Mesh to modify (not null, with normals)
     */
    public static void smoothNormals(Mesh mesh) {
        Validate.nonNull(mesh, "mesh");
        Validate.require(hasAnyNormals(mesh), "have normals");

        smoothNormals(
                mesh, VertexBuffer.Type.Normal, VertexBuffer.Type.Position);

        VertexBuffer bpNormal
                = mesh.getBuffer(VertexBuffer.Type.BindPoseNormal);
        if (bpNormal != null) {
            smoothNormals(mesh, VertexBuffer.Type.BindPoseNormal,
                    VertexBuffer.Type.BindPosePosition);
        }
    }

    /**
     * Smooth the normals of a Mesh by averaging them across all uses of each
     * distinct vertex position.
     *
     * @param mesh the Mesh to modify (not null, with normals)
     * @param normalBufferType the normal-buffer type (Normal or BindPoseNormal)
     * @param positionBufferType the position-buffer type (Position or
     */
    public static void smoothNormals(
            Mesh mesh, VertexBuffer.Type normalBufferType,
            VertexBuffer.Type positionBufferType) {
        Validate.nonNull(mesh, "mesh");
        FloatBuffer positionBuffer = mesh.getFloatBuffer(positionBufferType);
        int numVertices = positionBuffer.limit() / numAxes;

        Map<Vector3f, Integer> mapPosToDpid = new HashMap<>(numVertices);
        int numDistinctPositions = 0;
        for (int vertexIndex = 0; vertexIndex < numVertices; ++vertexIndex) {
            int start = vertexIndex * numAxes;
            Vector3f position = new Vector3f();
            MyBuffer.get(positionBuffer, start, position);
            MyVector3f.standardize(position, position);
            if (!mapPosToDpid.containsKey(position)) {
                mapPosToDpid.put(position, numDistinctPositions);
                ++numDistinctPositions;
            }
        }

        // Initialize the normal sum for each distinct position.
        Vector3f[] normalSum = new Vector3f[numDistinctPositions];
        for (int dpid = 0; dpid < numDistinctPositions; ++dpid) {
            normalSum[dpid] = new Vector3f(0f, 0f, 0f);
        }

        IndexBuffer indexList = mesh.getIndicesAsList();
        int numIndices = indexList.size();

        FloatBuffer normalBuffer = mesh.getFloatBuffer(normalBufferType);
        Vector3f tmpPosition = new Vector3f();
        Vector3f tmpNormal = new Vector3f();
        for (int ibPosition = 0; ibPosition < numIndices; ++ibPosition) {
            int vertexIndex = indexList.get(ibPosition);
            int start = vertexIndex * numAxes;
            MyBuffer.get(positionBuffer, start, tmpPosition);
            MyVector3f.standardize(tmpPosition, tmpPosition);
            int dpid = mapPosToDpid.get(tmpPosition);

            MyBuffer.get(normalBuffer, start, tmpNormal);
            normalSum[dpid].addLocal(tmpNormal);
        }

        // Re-normalize the normal sum for each distinct position.
        for (Vector3f vector3f : normalSum) {
            MyVector3f.normalizeLocal(vector3f);
        }

        // Write new normals to the buffer.
        for (int vertexIndex = 0; vertexIndex < numVertices; ++vertexIndex) {
            int start = vertexIndex * numAxes;
            MyBuffer.get(positionBuffer, start, tmpPosition);
            MyVector3f.standardize(tmpPosition, tmpPosition);
            int dpid = mapPosToDpid.get(tmpPosition);
            MyBuffer.put(normalBuffer, start, normalSum[dpid]);
        }
    }

    /**
     * Subdivide the specified Lines-mode mesh by the specified ratio.
     *
     * @param in the input mesh (not null, mode=Lines, unaffected)
     * @param ratio the input edge lengths divided by result edge lengths
     * (&gt;1)
     * @return a new Mesh (mode=Lines, not indexed)
     */
    public static Mesh subdivideLines(Mesh in, int ratio) {
        Validate.nonNull(in, "input mesh");
        Validate.require(in.getMode() == Mesh.Mode.Lines, "be in Lines mode");
        Validate.inRange(ratio, "ratio", 2, Integer.MAX_VALUE);

        IndexBuffer indexList = in.getIndicesAsList();
        int inEdgeCount = in.getTriangleCount();
        assert inEdgeCount * vpe == indexList.size() : inEdgeCount;
        int outVertexCount = indexList.size() * ratio;

        // Create a shallow clone of the input mesh.
        Mesh out = in.clone();

        // Create output buffers.
        for (VertexBuffer inVertexBuffer : in.getBufferList()) {
            VertexBuffer.Type type = inVertexBuffer.getBufferType();
            out.clearBuffer(type);

            if (type != VertexBuffer.Type.Index) {
                VertexBuffer.Format format = VertexBuffer.Format.Float;
                int numCperE = inVertexBuffer.getNumComponents();
                numCperE = MyMath.clamp(numCperE, 1, 4); // to avoid an IAE
                Buffer data = VertexBuffer.createBuffer(
                        format, numCperE, outVertexCount);
                out.setBuffer(type, numCperE, format, data);
            }
        }

        // Interpolate all vertex data to the output Mesh.
        int outVI = 0;
        for (int inEI = 0; inEI < inEdgeCount; ++inEI) {
            int inVI0 = indexList.get(vpe * inEI);
            int inVI1 = indexList.get(vpe * inEI + 1);
            for (int subI = 0; subI < ratio; ++subI) {
                float t0 = subI / (float) ratio;
                float t1 = (subI + 1) / (float) ratio;
                for (VertexBuffer outVB : out.getBufferList()) {
                    VertexBuffer.Type type = outVB.getBufferType();
                    VertexBuffer inVB = in.getBuffer(type);
                    assert inVB != outVB;
                    if (inVB.getNumElements() > 0) {
                        Element.lerp(t0, inVB, inVI0, inVI1, outVB, outVI);
                        ++outVI;
                        Element.lerp(t1, inVB, inVI0, inVI1, outVB, outVI);
                        ++outVI;
                    }
                }
            }
        }

        // Flip each buffer.
        for (VertexBuffer outVB : out.getBufferList()) {
            Buffer data = outVB.getData();
            int endPosition = data.capacity();
            data.position(endPosition);
            data.flip();
        }

        out.updateCounts();

        assert out.getMode() == Mesh.Mode.Lines : out.getMode();
        assert !hasIndices(out);
        return out;
    }

    /**
     * Uniformly subdivide the specified Triangles-mode mesh by the specified
     * ratio.
     *
     * @param in the input mesh (not null, mode=Triangles, unaffected)
     * @param ratio the input edge lengths divided by output edge lengths
     * (&gt;1)
     * @return a new Mesh (mode=Triangles, not indexed)
     */
    public static Mesh subdivideTriangles(Mesh in, int ratio) {
        Validate.nonNull(in, "input mesh");
        Validate.require(
                in.getMode() == Mesh.Mode.Triangles, "be in Triangles mode");
        Validate.inRange(ratio, "ratio", 2, Integer.MAX_VALUE);

        IndexBuffer indexList = in.getIndicesAsList();
        int inTriangleCount = in.getTriangleCount();
        assert inTriangleCount * vpt == indexList.size() : inTriangleCount;
        int outVertexCount = indexList.size() * ratio * ratio;

        // Create a shallow clone of the input mesh.
        Mesh out = in.clone();

        // Create output buffers.
        for (VertexBuffer inVertexBuffer : in.getBufferList()) {
            VertexBuffer.Type type = inVertexBuffer.getBufferType();
            out.clearBuffer(type);

            if (type != VertexBuffer.Type.Index) {
                VertexBuffer.Format format = VertexBuffer.Format.Float;
                int numCperE = inVertexBuffer.getNumComponents();
                numCperE = MyMath.clamp(numCperE, 1, 4); // to avoid an IAE
                Buffer data = VertexBuffer.createBuffer(
                        format, numCperE, outVertexCount);
                out.setBuffer(type, numCperE, format, data);
            }
        }

        // Interpolate all vertex data to the output Mesh.
        int outVI = 0;
        for (int inTI = 0; inTI < inTriangleCount; ++inTI) {
            int inVI0 = indexList.get(vpt * inTI);
            int inVI1 = indexList.get(vpt * inTI + 1);
            int inVI2 = indexList.get(vpt * inTI + 2);
            /*
             * First, output the triangles that point
             * in the same direction as the input triangle ...
             */
            for (int subI = 0; subI < ratio; ++subI) {
                for (int subJ = 0; subJ < ratio - subI; ++subJ) {
                    int subK = ratio - subI - subJ - 1;

                    float t1a = subJ / (float) ratio;
                    float t1b = (subJ + 1) / (float) ratio;
                    float t2a = subK / (float) ratio;
                    float t2b = (subK + 1) / (float) ratio;

                    for (VertexBuffer outVB : out.getBufferList()) {
                        VertexBuffer.Type type = outVB.getBufferType();
                        VertexBuffer inVB = in.getBuffer(type);
                        assert inVB != outVB;
                        if (inVB.getNumElements() > 0) {
                            Element.lerp3(t1a, t2a, inVB, inVI0, inVI1, inVI2,
                                    outVB, outVI);
                            Element.lerp3(t1b, t2a, inVB, inVI0, inVI1, inVI2,
                                    outVB, outVI + 1);
                            Element.lerp3(t1a, t2b, inVB, inVI0, inVI1, inVI2,
                                    outVB, outVI + 2);
                        }
                    }
                    outVI += vpt;
                }
            }
            /*
             * Now, output the (smaller number of) triangles that point
             * in the opposite direction from the input triangle ...
             */
            for (int subI = 0; subI < ratio - 1; ++subI) {
                for (int subJ = 0; subJ < ratio - subI - 1; ++subJ) {
                    int subK = ratio - subI - subJ - 2;

                    float t1a = subJ / (float) ratio;
                    float t1b = (subJ + 1) / (float) ratio;
                    float t2a = subK / (float) ratio;
                    float t2b = (subK + 1) / (float) ratio;

                    for (VertexBuffer outVB : out.getBufferList()) {
                        VertexBuffer.Type type = outVB.getBufferType();
                        VertexBuffer inVB = in.getBuffer(type);
                        assert inVB != outVB;
                        if (inVB.getNumElements() > 0) {
                            Element.lerp3(t1b, t2a, inVB, inVI0, inVI1, inVI2,
                                    outVB, outVI);
                            Element.lerp3(t1b, t2b, inVB, inVI0, inVI1, inVI2,
                                    outVB, outVI + 1);
                            Element.lerp3(t1a, t2b, inVB, inVI0, inVI1, inVI2,
                                    outVB, outVI + 2);
                        }
                    }
                    outVI += vpt;
                }
            }
        }

        // Flip each buffer.
        for (VertexBuffer outVB : out.getBufferList()) {
            Buffer data = outVB.getData();
            int endPosition = data.capacity();
            data.position(endPosition);
            data.flip();
        }

        out.updateCounts();

        assert out.getMode() == Mesh.Mode.Triangles : out.getMode();
        assert !hasIndices(out);
        return out;
    }

    /**
     * Apply the specified coordinate transform to all data in the specified
     * VertexBuffer.
     *
     * @param mesh the subject mesh (not null)
     * @param bufferType which buffer to read (not null)
     * @param transform the Transform to apply (not null, unaffected)
     */
    public static void transformBuffer(
            Mesh mesh, VertexBuffer.Type bufferType, Transform transform) {
        Validate.nonNull(bufferType, "buffer type");
        Validate.nonNull(transform, "transform");

        VertexBuffer vertexBuffer = mesh.getBuffer(bufferType);
        if (vertexBuffer != null) {
            FloatBuffer floatBuffer = (FloatBuffer) vertexBuffer.getData();
            int numVertices = mesh.getVertexCount();
            int numFloats = numAxes * numVertices;
            MyBuffer.transform(floatBuffer, 0, numFloats, transform);

            vertexBuffer.setUpdateNeeded();
        }
    }

    /**
     * Translate all vertices in the specified Mesh by the specified offset.
     *
     * @param mesh the Mesh to translate (not null, modified)
     * @param offset the amount to add to each position (not null, unaffected)
     */
    public static void translate(Mesh mesh, Vector3f offset) {
        int numVertices = mesh.getVertexCount();
        int numFloats = numAxes * numVertices;

        VertexBuffer posBuffer = mesh.getBuffer(VertexBuffer.Type.Position);
        FloatBuffer posFloats = (FloatBuffer) posBuffer.getData();
        MyBuffer.translate(posFloats, 0, numFloats, offset);
        posBuffer.setUpdateNeeded();

        VertexBuffer bindPosBuffer
                = mesh.getBuffer(VertexBuffer.Type.BindPosePosition);
        if (bindPosBuffer != null) {
            FloatBuffer bpFloats = (FloatBuffer) bindPosBuffer.getData();
            MyBuffer.translate(bpFloats, 0, numFloats, offset);
            bindPosBuffer.setUpdateNeeded();
        }

        mesh.updateBound();
    }

    /**
     * Convert mesh triangles to lines.
     *
     * @param mesh the Mesh to modify (not null,
     * mode=Triangles/TriangleFan/TriangleStrip)
     */
    public static void trianglesToLines(Mesh mesh) {
        Validate.nonNull(mesh, "mesh");
        Validate.require(hasTriangles(mesh), "contain triangles");

        IndexBuffer indexList = mesh.getIndicesAsList();
        int numTriangles = indexList.size() / vpt;
        Set<IntPair> edgeSet = new HashSet<>(vpt * numTriangles);
        for (int triIndex = 0; triIndex < numTriangles; ++triIndex) {
            int intOffset = vpt * triIndex;
            int ti0 = indexList.get(intOffset);
            int ti1 = indexList.get(intOffset + 1);
            int ti2 = indexList.get(intOffset + 2);

            edgeSet.add(new IntPair(ti0, ti1));
            edgeSet.add(new IntPair(ti0, ti2));
            edgeSet.add(new IntPair(ti1, ti2));
        }
        int numEdges = edgeSet.size();
        int numIndices = vpe * numEdges;
        int numVertices = mesh.getVertexCount();

        mesh.clearBuffer(VertexBuffer.Type.Index);

        IndexBuffer ib = IndexBuffer.createIndexBuffer(numVertices, numIndices);
        for (IntPair edge : edgeSet) {
            ib.put(edge.smaller());
            ib.put(edge.larger());
        }
        VertexBuffer.Format ibFormat = ib.getFormat();
        Buffer ibData = ib.getBuffer();
        ibData.flip();
        mesh.setBuffer(VertexBuffer.Type.Index, vpe, ibFormat, ibData);

        mesh.setMode(Mesh.Mode.Lines);
    }

    /**
     * Copy the bone indices for the indexed vertex.
     *
     * @param mesh the subject mesh (not null)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param storeResult storage for the result (modified if not null)
     * @return the data vector (either storeResult or a new instance)
     */
    public static int[] vertexBoneIndices(
            Mesh mesh, int vertexIndex, int[] storeResult) {
        Validate.nonNull(mesh, "mesh");
        Validate.nonNegative(vertexIndex, "vertex index");
        int[] result;
        if (storeResult == null) {
            result = new int[maxWeights];
        } else {
            assert storeResult.length >= maxWeights : storeResult.length;
            result = storeResult;
        }

        int maxWeightsPerVert = mesh.getMaxNumWeights();
        if (maxWeightsPerVert <= 0) {
            maxWeightsPerVert = 1;
        }

        VertexBuffer biBuf = mesh.getBuffer(VertexBuffer.Type.BoneIndex);
        Buffer boneIndexBuffer = biBuf.getDataReadOnly();
        boneIndexBuffer.position(maxWeights * vertexIndex);
        for (int wIndex = 0; wIndex < maxWeightsPerVert; ++wIndex) {
            int boneIndex = MyBuffer.readIndex(boneIndexBuffer);
            result[wIndex] = boneIndex;
        }

        // Fill with -1s.
        int length = result.length;
        for (int wIndex = maxWeightsPerVert; wIndex < length; ++wIndex) {
            result[wIndex] = -1;
        }

        return result;
    }

    /**
     * Copy the bone weights for the indexed vertex.
     *
     * @param mesh the subject mesh (not null, unaffected)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param storeResult storage for the result (modified if not null)
     * @return the data vector (either storeResult or a new instance)
     */
    public static float[] vertexBoneWeights(
            Mesh mesh, int vertexIndex, float[] storeResult) {
        Validate.nonNull(mesh, "mesh");
        Validate.nonNegative(vertexIndex, "vertex index");
        float[] result;
        if (storeResult == null) {
            result = new float[maxWeights];
        } else {
            assert storeResult.length >= maxWeights : storeResult.length;
            result = storeResult;
        }

        int maxWeightsPerVert = mesh.getMaxNumWeights();
        if (maxWeightsPerVert <= 0) {
            maxWeightsPerVert = 1;
        }

        FloatBuffer weightBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.BoneWeight);
        int startIndex = maxWeights * vertexIndex;
        for (int wIndex = 0; wIndex < maxWeightsPerVert; ++wIndex) {
            result[wIndex] = weightBuffer.get(startIndex + wIndex);
        }

        // Fill with 0s.
        int length = result.length;
        for (int wIndex = maxWeightsPerVert; wIndex < length; ++wIndex) {
            result[wIndex] = 0f;
        }

        return result;
    }

    /**
     * Copy the color of the indexed vertex from the color buffer of the
     * specified Mesh. The buffer must have 4 components per element.
     *
     * @param mesh the subject mesh (not null, unaffected)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param storeResult storage for the result (modified if not null)
     * @return the color (either storeResult or a new instance)
     */
    public static ColorRGBA vertexColor(
            Mesh mesh, int vertexIndex, ColorRGBA storeResult) {
        Validate.nonNull(mesh, "mesh");
        Validate.nonNegative(vertexIndex, "vertex index");
        ColorRGBA result
                = (storeResult == null) ? new ColorRGBA() : storeResult;

        VertexBuffer vertexBuffer = mesh.getBuffer(VertexBuffer.Type.Color);
        int numComponents = vertexBuffer.getNumComponents();
        Validate.require(numComponents == 4, "4 components per element");

        int bufferPosition = numComponents * vertexIndex;
        Buffer data = vertexBuffer.getDataReadOnly();
        if (data instanceof ByteBuffer) {
            // BitmapTextPage (for example) puts byte data in its Color buffer!
            ByteBuffer byteBuffer = (ByteBuffer) data;
            int r = byteBuffer.get(bufferPosition) & 0xFF;
            int g = byteBuffer.get(bufferPosition + 1) & 0xFF;
            int b = byteBuffer.get(bufferPosition + 2) & 0xFF;
            int a = byteBuffer.get(bufferPosition + 3) & 0xFF;
            result.set(r, g, b, a);
            result.multLocal(1f / 255);

        } else {
            FloatBuffer floatBuffer = (FloatBuffer) data;
            result.r = floatBuffer.get(bufferPosition);
            result.g = floatBuffer.get(bufferPosition + 1);
            result.b = floatBuffer.get(bufferPosition + 2);
            result.a = floatBuffer.get(bufferPosition + 3);
        }

        return result;
    }

    /**
     * Determine the location of the indexed vertex in mesh space using the
     * skinning matrices provided.
     *
     * @param mesh the subject mesh (not null)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param skinningMatrices (not null, unaffected)
     * @param storeResult storage for the result (modified if not null)
     * @return mesh coordinates (either storeResult or a new instance)
     */
    public static Vector3f vertexLocation(Mesh mesh, int vertexIndex,
            Matrix4f[] skinningMatrices, Vector3f storeResult) {
        Validate.nonNull(mesh, "mesh");
        Validate.nonNegative(vertexIndex, "vertex index");
        Validate.nonNull(skinningMatrices, "skinning matrices");
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;

        if (isAnimated(mesh)) {
            Vector3f b = vertexVector3f(mesh,
                    VertexBuffer.Type.BindPosePosition, vertexIndex, null);

            FloatBuffer weightBuffer
                    = mesh.getFloatBuffer(VertexBuffer.Type.BoneWeight);
            weightBuffer.position(maxWeights * vertexIndex);

            VertexBuffer biBuf = mesh.getBuffer(VertexBuffer.Type.BoneIndex);
            Buffer boneIndexBuffer = biBuf.getDataReadOnly();
            boneIndexBuffer.position(maxWeights * vertexIndex);

            result.zero();
            int maxWeightsPerVertex = mesh.getMaxNumWeights();
            for (int wIndex = 0; wIndex < maxWeightsPerVertex; ++wIndex) {
                float weight = weightBuffer.get();
                int boneIndex = MyBuffer.readIndex(boneIndexBuffer);
                if (weight != 0f) {
                    Matrix4f s;
                    if (boneIndex < skinningMatrices.length) {
                        s = skinningMatrices[boneIndex];
                    } else {
                        s = matrixIdentity;
                    }
                    float xOf = s.m00 * b.x + s.m01 * b.y + s.m02 * b.z + s.m03;
                    float yOf = s.m10 * b.x + s.m11 * b.y + s.m12 * b.z + s.m13;
                    float zOf = s.m20 * b.x + s.m21 * b.y + s.m22 * b.z + s.m23;
                    result.x += weight * xOf;
                    result.y += weight * yOf;
                    result.z += weight * zOf;
                }
            }

        } else { // not an animated mesh
            vertexVector3f(
                    mesh, VertexBuffer.Type.Position, vertexIndex, result);
        }

        return result;
    }

    /**
     * Determine the normal of the indexed vertex in mesh space using the
     * skinning matrices provided.
     *
     * @param mesh the subject mesh (not null)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param skinningMatrices (not null, unaffected)
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector in mesh space (either storeResult or a new
     * instance)
     */
    public static Vector3f vertexNormal(Mesh mesh, int vertexIndex,
            Matrix4f[] skinningMatrices, Vector3f storeResult) {
        Validate.nonNull(mesh, "mesh");
        Validate.nonNegative(vertexIndex, "vertex index");
        Validate.nonNull(skinningMatrices, "skinning matrices");
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;

        if (isAnimated(mesh)) {
            Vector3f b = vertexVector3f(
                    mesh, VertexBuffer.Type.BindPoseNormal, vertexIndex, null);

            FloatBuffer weightBuffer
                    = mesh.getFloatBuffer(VertexBuffer.Type.BoneWeight);
            weightBuffer.position(maxWeights * vertexIndex);

            VertexBuffer biBuf = mesh.getBuffer(VertexBuffer.Type.BoneIndex);
            Buffer boneIndexBuffer = biBuf.getDataReadOnly();
            boneIndexBuffer.position(maxWeights * vertexIndex);

            result.zero();
            int maxWeightsPerVertex = mesh.getMaxNumWeights();
            for (int wIndex = 0; wIndex < maxWeightsPerVertex; ++wIndex) {
                float weight = weightBuffer.get();
                int boneIndex = MyBuffer.readIndex(boneIndexBuffer);
                if (weight != 0f) {
                    Matrix4f s;
                    if (boneIndex < skinningMatrices.length) {
                        s = skinningMatrices[boneIndex];
                    } else {
                        s = matrixIdentity;
                    }
                    float xOf = s.m00 * b.x + s.m01 * b.y + s.m02 * b.z;
                    float yOf = s.m10 * b.x + s.m11 * b.y + s.m12 * b.z;
                    float zOf = s.m20 * b.x + s.m21 * b.y + s.m22 * b.z;
                    result.x += weight * xOf;
                    result.y += weight * yOf;
                    result.z += weight * zOf;
                }
            }
            MyVector3f.normalizeLocal(result);

        } else { // not an animated mesh
            vertexVector3f(mesh, VertexBuffer.Type.Normal, vertexIndex, result);
        }

        return result;
    }

    /**
     * Read the size of the indexed vertex from the size buffer.
     *
     * @param mesh the subject mesh (not null, unaffected)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @return the size (in pixels)
     */
    public static float vertexSize(Mesh mesh, int vertexIndex) {
        Validate.nonNull(mesh, "mesh");
        Validate.nonNegative(vertexIndex, "vertex index");

        FloatBuffer floatBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Size);
        float result = floatBuffer.get(vertexIndex);

        return result;
    }

    /**
     * Determine the tangent of the indexed vertex in mesh space using the
     * skinning matrices provided.
     *
     * @param mesh the subject mesh (not null)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param skinningMatrices (not null, unaffected)
     * @param storeResult storage for the result (modified if not null)
     * @return the tangent vector (either storeResult or a new instance)
     */
    public static Vector4f vertexTangent(Mesh mesh, int vertexIndex,
            Matrix4f[] skinningMatrices, Vector4f storeResult) {
        Validate.nonNull(mesh, "mesh");
        Validate.nonNegative(vertexIndex, "vertex index");
        Validate.nonNull(skinningMatrices, "skinning matrices");
        Vector4f result = (storeResult == null) ? new Vector4f() : storeResult;

        if (isAnimated(mesh)) {
            Vector4f b = vertexVector4f(
                    mesh, VertexBuffer.Type.BindPoseTangent, vertexIndex, null);

            FloatBuffer weightBuffer
                    = mesh.getFloatBuffer(VertexBuffer.Type.BoneWeight);
            weightBuffer.position(maxWeights * vertexIndex);

            VertexBuffer biBuf = mesh.getBuffer(VertexBuffer.Type.BoneIndex);
            Buffer boneIndexBuffer = biBuf.getDataReadOnly();
            boneIndexBuffer.position(maxWeights * vertexIndex);

            result.zero();
            int maxWeightsPerVertex = mesh.getMaxNumWeights();
            for (int wIndex = 0; wIndex < maxWeightsPerVertex; ++wIndex) {
                float weight = weightBuffer.get();
                int boneIndex = MyBuffer.readIndex(boneIndexBuffer);
                if (weight != 0f) {
                    Matrix4f s;
                    if (boneIndex < skinningMatrices.length) {
                        s = skinningMatrices[boneIndex];
                    } else {
                        s = matrixIdentity;
                    }
                    float xOf = s.m00 * b.x + s.m01 * b.y + s.m02 * b.z;
                    float yOf = s.m10 * b.x + s.m11 * b.y + s.m12 * b.z;
                    float zOf = s.m20 * b.x + s.m21 * b.y + s.m22 * b.z;
                    result.x += weight * xOf;
                    result.y += weight * yOf;
                    result.z += weight * zOf;
                }
            }
            result.normalizeLocal();
            result.w = b.w; // copy the binormal parity

        } else { // not an animated mesh
            vertexVector4f(
                    mesh, VertexBuffer.Type.Tangent, vertexIndex, result);
        }

        return result;
    }

    /**
     * Copy texture coordinates of the indexed vertex from the specified vertex
     * buffer.
     *
     * @param mesh the subject mesh (not null, unaffected)
     * @param bufferType which buffer to read (8 legal values)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param storeResult storage for the result (modified if not null)
     * @return the texture coordinates (either storeResult or a new instance)
     */
    public static Vector2f vertexVector2f(
            Mesh mesh, VertexBuffer.Type bufferType, int vertexIndex,
            Vector2f storeResult) {
        assert Validate.require(bufferType == VertexBuffer.Type.TexCoord
                || bufferType == VertexBuffer.Type.TexCoord2
                || bufferType == VertexBuffer.Type.TexCoord3
                || bufferType == VertexBuffer.Type.TexCoord4
                || bufferType == VertexBuffer.Type.TexCoord5
                || bufferType == VertexBuffer.Type.TexCoord6
                || bufferType == VertexBuffer.Type.TexCoord7
                || bufferType == VertexBuffer.Type.TexCoord8,
                "legal VertexBuffer type");
        Validate.nonNegative(vertexIndex, "vertex index");
        Vector2f result = (storeResult == null) ? new Vector2f() : storeResult;

        FloatBuffer floatBuffer = mesh.getFloatBuffer(bufferType);
        int floatIndex = 2 * vertexIndex;
        result.x = floatBuffer.get(floatIndex);
        result.y = floatBuffer.get(floatIndex + 1);

        return result;
    }

    /**
     * Copy Vector3f data for the indexed vertex from the specified
     * VertexBuffer.
     * <p>
     * A software skin update is required BEFORE reading positions/normals from
     * an animated mesh.
     *
     * @param mesh the subject mesh (not null, unaffected)
     * @param bufferType which buffer to read (5 legal values)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param storeResult storage for the result (modified if not null)
     * @return the data vector (either storeResult or a new instance)
     */
    public static Vector3f vertexVector3f(
            Mesh mesh, VertexBuffer.Type bufferType, int vertexIndex,
            Vector3f storeResult) {
        assert Validate.require(bufferType == VertexBuffer.Type.BindPoseNormal
                || bufferType == VertexBuffer.Type.BindPosePosition
                || bufferType == VertexBuffer.Type.Binormal
                || bufferType == VertexBuffer.Type.Normal
                || bufferType == VertexBuffer.Type.Position,
                "legal VertexBuffer type");
        Validate.nonNegative(vertexIndex, "vertex index");
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;

        FloatBuffer floatBuffer = mesh.getFloatBuffer(bufferType);
        int floatIndex = MyVector3f.numAxes * vertexIndex;
        MyBuffer.get(floatBuffer, floatIndex, result);

        return result;
    }

    /**
     * Copy Vector4f data for the indexed vertex from the specified vertex
     * buffer.
     * <p>
     * A software skin update is required BEFORE reading tangents from an
     * animated mesh.
     *
     * @param mesh the subject mesh (not null, unaffected)
     * @param bufferType which buffer to read (5 legal values)
     * @param vertexIndex index into the mesh's vertices (&ge;0)
     * @param storeResult storage for the result (modified if not null)
     * @return the data vector (either storeResult or a new instance)
     */
    public static Vector4f vertexVector4f(
            Mesh mesh, VertexBuffer.Type bufferType, int vertexIndex,
            Vector4f storeResult) {
        assert Validate.require(bufferType == VertexBuffer.Type.BindPoseTangent
                || bufferType == VertexBuffer.Type.BoneWeight
                || bufferType == VertexBuffer.Type.Color
                || bufferType == VertexBuffer.Type.HWBoneWeight
                || bufferType == VertexBuffer.Type.Tangent,
                "legal VertexBuffer type");
        Validate.nonNegative(vertexIndex, "vertex index");
        Vector4f result = (storeResult == null) ? new Vector4f() : storeResult;

        FloatBuffer floatBuffer = mesh.getFloatBuffer(bufferType);
        int floatIndex = 4 * vertexIndex;
        result.x = floatBuffer.get(floatIndex);
        result.y = floatBuffer.get(floatIndex + 1);
        result.z = floatBuffer.get(floatIndex + 2);
        result.w = floatBuffer.get(floatIndex + 3);

        return result;
    }

    /**
     * Determine the location of the indexed vertex in world space using the
     * skinning matrices provided.
     *
     * @param geometry Geometry containing the subject mesh (not null)
     * @param vertexIndex index into the geometry's vertices (&ge;0)
     * @param skinningMatrices (not null, unaffected)
     * @param storeResult storage for the result (modified if not null)
     * @return the location in world coordinates (either storeResult or a new
     * instance)
     */
    public static Vector3f vertexWorldLocation(
            Geometry geometry, int vertexIndex, Matrix4f[] skinningMatrices,
            Vector3f storeResult) {
        Validate.nonNegative(vertexIndex, "vertex index");
        Validate.nonNull(skinningMatrices, "skinning matrices");
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;

        Mesh mesh = geometry.getMesh();
        Vector3f meshLocation
                = vertexLocation(mesh, vertexIndex, skinningMatrices, null);
        if (geometry.isIgnoreTransform()) {
            result.set(meshLocation);
        } else {
            geometry.localToWorld(meshLocation, result);
        }

        return result;
    }
}
