/*
 Copyright (c) 2020-2023, Stephen Gold
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
package jme3utilities.math.test;

import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.CameraInput;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyCamera;
import jme3utilities.MyString;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.noise.Generator;
import jme3utilities.mesh.Icosphere;
import jme3utilities.mesh.PointMesh;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;

/**
 * Eyeball test for the pseudo-random number generator.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestGenerator extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * color for visualizing sample points (white)
     */
    final private static ColorRGBA sampleColor = new ColorRGBA(1f, 1f, 1f, 1f);
    /**
     * size for visualizing sample points (in pixels)
     */
    final private static float samplePointSize = 2f;
    /**
     * number of sample points per trial
     */
    final private static int samplesPerTrial = 200;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestGenerator.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestGenerator.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * status displayed in the upper-left corner of the GUI node
     */
    private static BitmapText statusLine;
    /**
     * enhanced pseudo-random generator
     */
    final private static Generator random = new Generator();
    /**
     * material for visualizing sample points
     */
    private static Material samplePointMaterial;
    /**
     * scene-graph node for the current trial
     */
    private static Node trialNode = null;
    /**
     * name of the test being run
     */
    private static String testName = "nextQuaternion";
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestGenerator application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TestGenerator application = new TestGenerator();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setResizable(true);
        settings.setSamples(4); // anti-aliasing
        settings.setTitle(title); // Customize the window's title bar.
        application.setSettings(settings);

        application.start();
    }
    // *************************************************************************
    // ActionApplication methods

    /**
     * Initialize this application.
     */
    @Override
    public void acorusInit() {
        configureCamera();

        // Add the status text to the GUI.
        statusLine = new BitmapText(guiFont);
        guiNode.attachChild(statusLine);

        super.acorusInit();

        samplePointMaterial = MyAsset.createWireframeMaterial(assetManager,
                sampleColor, samplePointSize);

        trial();
    }

    /**
     * Calculate screen bounds for a detailed help node. Meant to be overridden.
     *
     * @param viewPortWidth (in pixels, &gt;0)
     * @param viewPortHeight (in pixels, &gt;0)
     * @return a new instance
     */
    public Rectangle detailedHelpBounds(int viewPortWidth, int viewPortHeight) {
        // Position help nodes along the top of the viewport.
        float margin = 10f; // in pixels
        float height = viewPortHeight - (2f * margin)
                - 20f; // 20px so as not to cover the status line
        float width = viewPortWidth - (2f * margin);
        float leftX = margin;
        float topY = margin + height;
        Rectangle result = new Rectangle(leftX, topY, width, height);

        return result;
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();

        dim.bindSignal(CameraInput.FLYCAM_LOWER, KeyInput.KEY_DOWN);
        dim.bindSignal(CameraInput.FLYCAM_RISE, KeyInput.KEY_UP);
        dim.bindSignal("orbitLeft", KeyInput.KEY_LEFT);
        dim.bindSignal("orbitRight", KeyInput.KEY_RIGHT);

        dim.bind("test nextQuaternion", KeyInput.KEY_F1);
        dim.bind("test nextUnitVector3f", KeyInput.KEY_F2);
        dim.bind("test nextVector3f", KeyInput.KEY_F3);

        dim.bind(asToggleHelp, KeyInput.KEY_H);
    }

    /**
     * Process an action that wasn't handled by the active input mode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            String[] words = actionString.split(" ");
            if (words.length >= 2 && "test".equals(words[0])) {
                test(words[1]);
                return;
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Update the GUI layout and proposed settings after a resize.
     *
     * @param newWidth the new width of the framebuffer (in pixels, &gt;0)
     * @param newHeight the new height of the framebuffer (in pixels, &gt;0)
     */
    @Override
    public void onViewPortResize(int newWidth, int newHeight) {
        statusLine.setLocalTranslation(0f, newHeight, 0f);
        super.onViewPortResize(newWidth, newHeight);
    }
    // *************************************************************************
    // private methods

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        float near = 0.002f;
        float far = 20f;
        MyCamera.setNearFar(cam, near, far);

        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(3f);

        cam.setLocation(new Vector3f(0f, 0f, 6f));

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, "orbitLeft", "orbitRight");
        stateManager.attach(orbitState);
    }

    /**
     * Clear the existing trial and start a new one.
     *
     * @param name the name of the test to run (not null)
     */
    private void test(String name) {
        testName = name;
        trialNode.removeFromParent();
        trial();
    }

    /**
     * Perform a new trial.
     */
    private void trial() {
        statusLine.setText("Test: " + testName);

        trialNode = new Node("trialNode");
        rootNode.attachChild(trialNode);

        // Visualize a unit sphere.
        int numRefinementSteps = 1;
        float radius = 1f;
        Mesh sphereMesh = new Icosphere(numRefinementSteps, radius);
        Geometry sphereGeometry = new Geometry("sphere", sphereMesh);
        Material wireMaterial
                = MyAsset.createWireframeMaterial(assetManager, ColorRGBA.Red);
        sphereGeometry.setMaterial(wireMaterial);
        trialNode.attachChild(sphereGeometry);

        // Generate sample points.
        Collection<Vector3f> sampleLocations = new ArrayList<>(samplesPerTrial);
        for (int sampleI = 0; sampleI < samplesPerTrial; ++sampleI) {
            Vector3f location;
            switch (testName) {
                case "nextQuaternion":
                    location = new Vector3f(radius, 0f, 0f);
                    MyQuaternion.rotate(
                            random.nextQuaternion(), location, location);
                    break;

                case "nextUnitVector3f":
                    location = random.nextUnitVector3f().multLocal(radius);
                    break;

                case "nextVector3f":
                    location = random.nextVector3f().multLocal(radius);
                    break;

                default:
                    String message = "testName = " + MyString.quote(testName);
                    throw new IllegalStateException(message);
            }

            sampleLocations.add(location);
        }

        // Visualize the sample points.
        for (Vector3f location : sampleLocations) {
            PointMesh pointMesh = new PointMesh();
            pointMesh.setLocation(location);
            Geometry sampleGeometry = new Geometry("sample", pointMesh);
            sampleGeometry.setMaterial(samplePointMaterial);
            trialNode.attachChild(sampleGeometry);
        }
    }
}
