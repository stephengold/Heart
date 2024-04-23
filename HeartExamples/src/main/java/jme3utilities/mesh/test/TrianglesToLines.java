/*
 Copyright (c) 2022-2024 Stephen Gold
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

import com.jme3.app.StatsAppState;
import com.jme3.input.CameraInput;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyCamera;
import jme3utilities.MyMesh;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;
import jme3utilities.mesh.RoundedRectangle;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;

/**
 * Demonstrate conversion of a triangle mesh to a line mesh.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TrianglesToLines extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TrianglesToLines.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TrianglesToLines.class.getSimpleName();
    /**
     * action string to dump the main scene graph
     */
    final private static String asDump = "dump scene";
    /**
     * action string to orbit to the camera's left
     */
    final private static String asOrbitLeft = "orbit left";
    /**
     * action string to orbit to the camera's right
     */
    final private static String asOrbitRight = "orbit right";
    // *************************************************************************
    // fields

    /**
     * dump debugging information to System.out
     */
    final private static Dumper dumper = new Dumper();
    // *************************************************************************
    // constructors

    /**
     * Instantiate the TrianglesToLines application.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public TrianglesToLines() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TrianglesToLines application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TrianglesToLines application = new TrianglesToLines();
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

        ColorRGBA backgroundColor = new ColorRGBA(0.2f, 0.2f, 1f, 1f);
        viewPort.setBackgroundColor(backgroundColor);

        configureCamera();
        configureDumper();

        // Hide the render-statistics overlay.
        stateManager.getState(StatsAppState.class).toggleStats();

        // Generate a mesh in TriangleFan mode:
        Mesh triMesh = new RoundedRectangle();

        // Derive a Lines-mode mesh (with identical vertices) from it:
        Mesh linesMesh = triMesh.deepClone();
        MyMesh.trianglesToLines(linesMesh);

        // Visualize the meshes side by side:
        Material solidWhite
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.White);
        solidWhite.getAdditionalRenderState()
                .setFaceCullMode(RenderState.FaceCullMode.Off); // double-sided

        Geometry triGeometry = new Geometry("triangles", triMesh);
        rootNode.attachChild(triGeometry);
        triGeometry.move(-1.1f, -0.5f, 0f);
        triGeometry.setMaterial(solidWhite);

        Geometry linesGeometry = new Geometry("lines", linesMesh);
        rootNode.attachChild(linesGeometry);
        linesGeometry.move(0.1f, -0.5f, 0f);
        linesGeometry.setMaterial(solidWhite);
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();

        dim.bind(asDump, KeyInput.KEY_P);
        dim.bindSignal(CameraInput.FLYCAM_LOWER, KeyInput.KEY_DOWN);
        dim.bindSignal(CameraInput.FLYCAM_RISE, KeyInput.KEY_UP);
        dim.bindSignal(asOrbitLeft, KeyInput.KEY_LEFT);
        dim.bindSignal(asOrbitRight, KeyInput.KEY_RIGHT);
        dim.bind(asToggleHelp, KeyInput.KEY_H);
    }

    /**
     * Process an action that wasn't handled by the active InputMode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            switch (actionString) {
                case asDump:
                    dumper.dump(rootNode);
                    return;

                default:
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        float near = 0.002f;
        float far = 50f;
        MyCamera.setNearFar(cam, near, far);

        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(1f);

        cam.setName("cam");
        cam.setLocation(new Vector3f(0f, 0f, 3f));

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, asOrbitLeft, asOrbitRight);
        stateManager.attach(orbitState);
    }

    /**
     * Configure the Dumper during startup.
     */
    private static void configureDumper() {
        dumper.setDumpTransform(true);
        dumper.setDumpVertex(true);
    }
}
