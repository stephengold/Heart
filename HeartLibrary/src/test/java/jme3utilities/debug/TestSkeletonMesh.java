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
package jme3utilities.debug;

import com.jme3.anim.Armature;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.util.AnimMigrationUtils;
import com.jme3.animation.Skeleton;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryLoader;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.texture.plugins.AWTLoader;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MySkeleton;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cloning/saving/loading SkeletonMesh.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestSkeletonMesh {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestSkeletonMesh.class.getName());
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
    public void testCloneSkeletonMesh() {
        assetManager.registerLoader(AWTLoader.class, "jpg", "png");
        assetManager.registerLoader(BinaryLoader.class, "j3o");
        assetManager.registerLoader(J3MLoader.class, "j3m", "j3md");

        assetManager.registerLocator(null, ClasspathLocator.class);

        Spatial oldJaime = assetManager.loadModel("Models/Jaime/Jaime.j3o");
        Skeleton skeleton = MySkeleton.findSkeleton(oldJaime);

        Spatial newJaime = Heart.deepCopy(oldJaime);
        AnimMigrationUtils.migrate(newJaime);
        SkinningControl skinning = newJaime.getControl(SkinningControl.class);
        Armature armature = skinning.getArmature();

        SkeletonMesh al = new SkeletonMesh(armature, null, Mesh.Mode.Lines);
        SkeletonMesh alClone = Heart.deepCopy(al);
        cloneTest(al, alClone);

        SkeletonMesh ap = new SkeletonMesh(armature, null, Mesh.Mode.Points);
        SkeletonMesh apClone = Heart.deepCopy(ap);
        cloneTest(ap, apClone);

        SkeletonMesh sl = new SkeletonMesh(null, skeleton, Mesh.Mode.Lines);
        SkeletonMesh slClone = Heart.deepCopy(sl);
        cloneTest(sl, slClone);

        SkeletonMesh sp = new SkeletonMesh(null, skeleton, Mesh.Mode.Points);
        SkeletonMesh spClone = Heart.deepCopy(sp);
        cloneTest(sp, spClone);
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
