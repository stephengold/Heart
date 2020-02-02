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
package jme3utilities.mesh.test;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Mesh;
import jme3utilities.Heart;
import jme3utilities.mesh.Cone;
import jme3utilities.mesh.Dodecahedron;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.mesh.Icosahedron;
import jme3utilities.mesh.Icosphere;
import jme3utilities.mesh.LoopMesh;
import jme3utilities.mesh.Octahedron;
import jme3utilities.mesh.PointMesh;
import jme3utilities.mesh.Prism;
import jme3utilities.mesh.RectangleMesh;
import jme3utilities.mesh.RectangleOutlineMesh;
import jme3utilities.mesh.RoundedRectangle;
import jme3utilities.mesh.Tetrahedron;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cloning/saving/loading various meshes.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestCloneMeshes {
    // *************************************************************************
    // fields

    /**
     * AssetManager required by the BinaryImporter
     */
    final private static AssetManager assetManager = new DesktopAssetManager();
    // *************************************************************************
    // new methods exposed

    /**
     * Test cloning/saving/loading various meshes.
     */
    @Test
    public void testClone() {
        int numSides = 32;
        float radius = 1f;
        float yHeight = 3f;
        boolean generatePyramid = false;
        Cone cone = new Cone(numSides, radius, yHeight, generatePyramid);
        Cone coneClone = (Cone) Heart.deepCopy(cone);
        cloneTest(cone, coneClone);

        Mesh.Mode mode = Mesh.Mode.Triangles;
        Dodecahedron dodec = new Dodecahedron(radius, mode);
        Dodecahedron dodecClone = (Dodecahedron) Heart.deepCopy(dodec);
        cloneTest(dodec, dodecClone);

        int rimSamples = 48;
        int quadrantSamples = 12;
        DomeMesh dome = new DomeMesh(rimSamples, quadrantSamples);
        DomeMesh domeClone = (DomeMesh) Heart.deepCopy(dome);
        cloneTest(dome, domeClone);

        boolean generateNormals = true;
        Icosahedron ico = new Icosahedron(radius, generateNormals);
        Icosahedron icoClone = (Icosahedron) Heart.deepCopy(ico);
        cloneTest(ico, icoClone);

        int numRefineSteps = 1;
        Icosphere sphere = new Icosphere(numRefineSteps, radius);
        Icosphere sphereClone = (Icosphere) Heart.deepCopy(sphere);
        cloneTest(sphere, sphereClone);

        int vertexCount = 10;
        LoopMesh loop = new LoopMesh(vertexCount);
        LoopMesh loopClone = (LoopMesh) Heart.deepCopy(loop);
        cloneTest(loop, loopClone);

        Octahedron oct = new Octahedron(radius, generateNormals);
        Octahedron octClone = (Octahedron) Heart.deepCopy(oct);
        cloneTest(oct, octClone);

        PointMesh point = new PointMesh();
        PointMesh pointClone = (PointMesh) Heart.deepCopy(point);
        cloneTest(point, pointClone);

        Prism prism = new Prism(numSides, radius, yHeight, generateNormals);
        Prism prismClone = (Prism) Heart.deepCopy(prism);
        cloneTest(prism, prismClone);

        RectangleMesh rect = new RectangleMesh();
        RectangleMesh rectClone = (RectangleMesh) Heart.deepCopy(rect);
        cloneTest(rect, rectClone);

        RectangleOutlineMesh ro = new RectangleOutlineMesh();
        RectangleOutlineMesh roClone
                = (RectangleOutlineMesh) Heart.deepCopy(ro);
        cloneTest(ro, roClone);

        RoundedRectangle round = new RoundedRectangle();
        RoundedRectangle roundClone = (RoundedRectangle) Heart.deepCopy(round);
        cloneTest(round, roundClone);

        Tetrahedron tetra = new Tetrahedron(radius, generateNormals);
        Tetrahedron tetraClone = (Tetrahedron) Heart.deepCopy(tetra);
        cloneTest(tetra, tetraClone);
    }
    // *************************************************************************
    // private methods

    private static void cloneTest(Mesh mesh, Mesh meshClone) {
        assert meshClone != mesh;
        Assert.assertEquals(meshClone.getVertexCount(), mesh.getVertexCount());

        Mesh copyMesh = BinaryExporter.saveAndLoad(assetManager, mesh);
        Assert.assertNotNull(copyMesh);

        Mesh copyMeshClone
                = BinaryExporter.saveAndLoad(assetManager, meshClone);
        Assert.assertNotNull(copyMeshClone);
    }
}
