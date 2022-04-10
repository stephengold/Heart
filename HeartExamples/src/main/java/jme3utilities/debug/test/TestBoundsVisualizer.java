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
package jme3utilities.debug.test;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyMesh;
import jme3utilities.MyString;
import jme3utilities.debug.BoundsVisualizer;
import jme3utilities.debug.Dumper;
import jme3utilities.debug.SphereMeshes;
import jme3utilities.math.MyVector3f;
import jme3utilities.mesh.Cone;
import jme3utilities.ui.AbstractDemo;
import jme3utilities.ui.InputMode;

/**
 * Test the BoundsVisualizer class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestBoundsVisualizer extends AbstractDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestBoundsVisualizer.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestBoundsVisualizer.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * status displayed in the upper-left corner of the GUI node
     */
    private BitmapText statusText;
    /**
     * control under test
     */
    private BoundsVisualizer visualizer;
    /**
     * dump debugging information to System.out
     */
    final private Dumper dumper = new Dumper();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestBoundsVisualizer application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TestBoundsVisualizer application = new TestBoundsVisualizer();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setRenderer(AppSettings.LWJGL_OPENGL32);
        settings.setSamples(4); // anti-aliasing
        settings.setTitle(title); // Customize the window's title bar.
        application.setSettings(settings);

        application.start();
    }
    // *************************************************************************
    // AbstractDemo methods

    /**
     * Process an action from the InputManager. TODO re-order methods
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            switch (actionString) {
                case "billboard off":
                    visualizer.disableBillboarding();
                    return;
                case "billboard X":
                    visualizer.enableBillboarding(cam, MyVector3f.xAxis);
                    return;
                case "billboard Y":
                    visualizer.enableBillboarding(cam, MyVector3f.yAxis);
                    return;
                case "billboard Z":
                    visualizer.enableBillboarding(cam, MyVector3f.zAxis);
                    return;

                case "dump":
                    dumper.dump(rootNode);
                    return;

                case "toggle bounds":
                    toggleBounds();
                    return;
                case "toggle depthTest":
                    toggleDepthTest();
                    return;
            }
            if (actionString.startsWith("sphere ")) {
                String name = actionString.split(" ")[1];
                SphereMeshes sphereType = SphereMeshes.valueOf(name);
                visualizer.setSphereType(sphereType);
                return;
            }
            if (actionString.startsWith("width ")) {
                int width = Integer.parseInt(actionString.split(" ")[1]);
                visualizer.setLineWidth(width);
                return;
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Initialize this application.
     */
    @Override
    public void actionInitializeApplication() {
        ColorRGBA gray = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
        viewPort.setBackgroundColor(gray);

        configureCamera();
        configureDumper();
        /*
         * Create a wireframe pyramid and attach it to the scene.
         */
        int numSides = 4;
        float radius = 2f;
        float height = 1f;
        boolean generatePyramid = true;
        Mesh mesh = new Cone(numSides, radius, height, generatePyramid);
        mesh = MyMesh.addIndices(mesh);
        Spatial pyramid = new Geometry("pyramid", mesh);
        rootNode.attachChild(pyramid);

        Material material = MyAsset.createWireframeMaterial(assetManager,
                ColorRGBA.Green);
        pyramid.setMaterial(material);
        /*
         * Add a bounds visualizer for the pyramid.
         */
        visualizer = new BoundsVisualizer(assetManager);
        rootNode.addControl(visualizer);
        visualizer.enableBillboarding(cam, MyVector3f.yAxis);
        visualizer.setSubject(pyramid);
        visualizer.setEnabled(true);
        /*
         * Create status text and attach it to the GUI.
         */
        statusText = new BitmapText(guiFont);
        statusText.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusText);
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        updateStatusText();
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bind("billboard off", KeyInput.KEY_N);
        dim.bind("billboard X", KeyInput.KEY_X);
        dim.bind("billboard Y", KeyInput.KEY_C);
        dim.bind("billboard Z", KeyInput.KEY_V);
        dim.bind("dump", KeyInput.KEY_P);
        dim.bind("sphere Icosphere", KeyInput.KEY_F2);
        dim.bind("sphere LoopMesh", KeyInput.KEY_F3);
        dim.bind("sphere PoleSphere", KeyInput.KEY_F1);
        dim.bind("sphere WireSphere", KeyInput.KEY_F4);
        dim.bind("toggle bounds", KeyInput.KEY_B);
        dim.bind(asToggleHelp, KeyInput.KEY_H);
        dim.bind("toggle depthTest", KeyInput.KEY_T);
        dim.bind("width 1", KeyInput.KEY_1);
        dim.bind("width 2", KeyInput.KEY_2);
        dim.bind("width 3", KeyInput.KEY_3);
        dim.bind("width 4", KeyInput.KEY_4);
        dim.bind("width 5", KeyInput.KEY_5);
        dim.bind("width 6", KeyInput.KEY_6);
    }
    // *************************************************************************
    // private methods

    /**
     * Configure the Camera during startup.
     */
    private void configureCamera() {
        flyCam.setMoveSpeed(10f);
        cam.setLocation(new Vector3f(10.5f, 3f, 8.3f));
        cam.setRotation(new Quaternion(-0.049f, 0.90316f, -0.1082f, -0.4126f));
    }

    /**
     * Configure the Dumper during startup.
     */
    private void configureDumper() {
        dumper.setDumpTransform(true);
        //dumper.setDumpVertex(true);
    }

    /**
     * Toggle the subject's bounds between AABB and sphere.
     */
    private void toggleBounds() {
        Geometry subject = (Geometry) visualizer.getSubject();
        Mesh mesh = subject.getMesh();
        if (mesh.getBound() instanceof BoundingBox) {
            mesh.setBound(new BoundingSphere());
        } else {
            mesh.setBound(new BoundingBox());
        }
        subject.updateModelBound();
    }

    /**
     * Toggle the depth-test setting.
     */
    private void toggleDepthTest() {
        boolean state = visualizer.isDepthTest();
        visualizer.setDepthTest(!state);
    }

    /**
     * Update the status text in the GUI.
     */
    private void updateStatusText() {
        BoundingVolume bound = visualizer.getSubject().getWorldBound();
        BoundingVolume.Type boundsType = bound.getType();
        String message = "bounds=" + boundsType;

        if (boundsType == BoundingVolume.Type.Sphere) {
            int axisIndex = visualizer.billboardAxis();
            String axis;
            if (axisIndex == -1) {
                axis = "none";
            } else {
                axis = MyString.axisName(axisIndex);
            }
            SphereMeshes sphere = visualizer.sphereType();
            message += String.format("  sphere=%s  axis=%s", sphere, axis);
        }

        boolean depth = visualizer.isDepthTest();
        float width = visualizer.lineWidth();
        message += String.format("  depth=%s  width=%.0f", depth, width);

        statusText.setText(message);
    }
}
