/*
 Copyright (c) 2020-2021, Stephen Gold
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
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.mesh.Cone;
import jme3utilities.mesh.DiscMesh;
import jme3utilities.mesh.Dodecahedron;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.mesh.Icosahedron;
import jme3utilities.mesh.Icosphere;
import jme3utilities.mesh.LoopMesh;
import jme3utilities.mesh.Octahedron;
import jme3utilities.mesh.Octasphere;
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
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestCloneMeshes.class.getName());
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
        Assert.assertEquals(62, cone.getTriangleCount());
        Assert.assertEquals(186, cone.getVertexCount());
        Cone coneClone = Heart.deepCopy(cone);
        cloneTest(cone, coneClone);

        DiscMesh disc = new DiscMesh(radius, numSides);
        Assert.assertEquals(30, disc.getTriangleCount());
        Assert.assertEquals(32, disc.getVertexCount());
        DiscMesh discClone = Heart.deepCopy(disc);
        cloneTest(disc, discClone);

        Mesh.Mode mode = Mesh.Mode.Triangles;
        Dodecahedron dodecahedron = new Dodecahedron(radius, mode);
        Assert.assertEquals(36, dodecahedron.getTriangleCount());
        Assert.assertEquals(20, dodecahedron.getVertexCount());
        Dodecahedron dodecahedronClone = Heart.deepCopy(dodecahedron);
        cloneTest(dodecahedron, dodecahedronClone);

        int rimSamples = 48;
        int quadrantSamples = 12;
        DomeMesh dome = new DomeMesh(rimSamples, quadrantSamples);
        Assert.assertEquals(1008, dome.getTriangleCount());
        Assert.assertEquals(529, dome.getVertexCount());
        DomeMesh domeClone = Heart.deepCopy(dome);
        cloneTest(dome, domeClone);

        boolean generateNormals = true;
        Icosahedron ico = new Icosahedron(radius, generateNormals);
        Assert.assertEquals(20, ico.getTriangleCount());
        Assert.assertEquals(60, ico.getVertexCount());
        Icosahedron icoClone = Heart.deepCopy(ico);
        cloneTest(ico, icoClone);

        int numRefineSteps = 1;
        Icosphere sphere = new Icosphere(numRefineSteps, radius);
        Assert.assertEquals(80, sphere.getTriangleCount());
        Assert.assertEquals(42, sphere.getVertexCount());
        Icosphere sphereClone = Heart.deepCopy(sphere);
        cloneTest(sphere, sphereClone);

        int vertexCount = 10;
        LoopMesh loop = new LoopMesh(vertexCount);
        Assert.assertEquals(vertexCount, loop.getTriangleCount());
        Assert.assertEquals(vertexCount, loop.getVertexCount());
        LoopMesh loopClone = Heart.deepCopy(loop);
        cloneTest(loop, loopClone);

        Octahedron oct = new Octahedron(radius, generateNormals);
        Assert.assertEquals(8, oct.getTriangleCount());
        Assert.assertEquals(24, oct.getVertexCount());
        Octahedron octClone = Heart.deepCopy(oct);
        cloneTest(oct, octClone);

        Octasphere oSphere = new Octasphere(numRefineSteps, radius);
        Assert.assertEquals(32, oSphere.getTriangleCount());
        Assert.assertEquals(21, oSphere.getVertexCount());
        Octasphere oSphereClone = Heart.deepCopy(oSphere);
        cloneTest(oSphere, oSphereClone);

        PointMesh point = new PointMesh();
        Assert.assertEquals(1, point.getTriangleCount());
        Assert.assertEquals(1, point.getVertexCount());
        PointMesh pointClone = Heart.deepCopy(point);
        cloneTest(point, pointClone);

        Prism prism = new Prism(numSides, radius, yHeight, generateNormals);
        Assert.assertEquals(124, prism.getTriangleCount());
        Assert.assertEquals(372, prism.getVertexCount());
        Prism prismClone = Heart.deepCopy(prism);
        cloneTest(prism, prismClone);

        RectangleMesh rect = new RectangleMesh();
        Assert.assertEquals(2, rect.getTriangleCount());
        Assert.assertEquals(4, rect.getVertexCount());
        RectangleMesh rectClone = Heart.deepCopy(rect);
        cloneTest(rect, rectClone);

        RectangleOutlineMesh ro = new RectangleOutlineMesh();
        Assert.assertEquals(4, ro.getTriangleCount());
        Assert.assertEquals(4, ro.getVertexCount());
        RectangleOutlineMesh roClone = Heart.deepCopy(ro);
        cloneTest(ro, roClone);

        RoundedRectangle round = new RoundedRectangle();
        Assert.assertEquals(20, round.getTriangleCount());
        Assert.assertEquals(21, round.getVertexCount());
        RoundedRectangle roundClone = Heart.deepCopy(round);
        cloneTest(round, roundClone);

        Tetrahedron tetra = new Tetrahedron(radius, generateNormals);
        Assert.assertEquals(4, tetra.getTriangleCount());
        Assert.assertEquals(12, tetra.getVertexCount());
        Tetrahedron tetraClone = Heart.deepCopy(tetra);
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
