/*
 Copyright (c) 2018-2022, Stephen Gold
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

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;
import jme3utilities.mesh.LoopMesh;
import jme3utilities.ui.AcorusDemo;

/**
 * Test the LoopMesh class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class LoopMeshTest extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(LoopMeshTest.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = LoopMeshTest.class.getSimpleName();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the LoopMeshTest application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        LoopMeshTest application = new LoopMeshTest();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setResizable(true);
        settings.setTitle(title); // Customize the window's title bar.
        application.setSettings(settings);

        application.start();
    }
    // *************************************************************************
    // AcorusDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void acorusInit() {
        super.acorusInit();

        ColorRGBA gray = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
        viewPort.setBackgroundColor(gray);

        flyCam.setEnabled(false);
        cam.setLocation(Vector3f.UNIT_Y.mult(10f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z.clone());

        Material material = MyAsset.createWireframeMaterial(assetManager,
                ColorRGBA.Yellow, 8f);

        LoopMesh loopMesh = new LoopMesh(4);
        Geometry loopGeometry = new Geometry("loop", loopMesh);
        loopGeometry.setMaterial(material);
        rootNode.attachChild(loopGeometry);

        LoopMesh pointsMesh = new LoopMesh(4);
        pointsMesh.setMode(Mesh.Mode.Points);
        Geometry pointsGeometry = new Geometry("points", pointsMesh);
        pointsGeometry.setMaterial(material);
        rootNode.attachChild(pointsGeometry);

        Dumper d = new Dumper();
        d.setDumpTransform(true);
        d.dump(renderManager);
    }
}
