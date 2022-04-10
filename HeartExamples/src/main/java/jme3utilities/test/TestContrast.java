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
package jme3utilities.test;

import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.CameraInput;
import com.jme3.input.KeyInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.ContrastAdjustmentFilter;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyCamera;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;
import jme3utilities.mesh.RectangleMesh;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.HelpUtils;
import jme3utilities.ui.InputMode;

/**
 * Test the ContrastAdjustmentFilter class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestContrast extends ActionApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestContrast.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestContrast.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * status displayed in the upper-left corner of the GUI node
     */
    private BitmapText statusLine;
    /**
     * filter under test
     */
    private ContrastAdjustmentFilter filter;
    /**
     * dump debugging information to System.out
     */
    final private Dumper dumper = new Dumper();
    /**
     * exponent passed to the filter
     */
    private float exponent = 1f;
    /**
     * GUI node for displaying hotkey help/hints
     */
    private Node helpNode;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestContrast application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TestContrast application = new TestContrast();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setRenderer(AppSettings.LWJGL_OPENGL32);
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
    public void actionInitializeApplication() {
        configureCamera();
        configureDumper();

        ColorRGBA bgColor = new ColorRGBA(0.2f, 0.2f, 0.2f, 1f);
        viewPort.setBackgroundColor(bgColor);

        addFloor();
        addLighting();
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        /*
         * Apply a contrast adjustment filter to the viewport.
         */
        int numSamples = settings.getSamples();
        FilterPostProcessor fpp
                = Heart.getFpp(viewPort, assetManager, numSamples);
        filter = new ContrastAdjustmentFilter(exponent);
        fpp.addFilter(filter);
        /*
         * Add the status text to the GUI.
         */
        statusLine = new BitmapText(guiFont);
        statusLine.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusLine);

        Spatial jaime = assetManager.loadModel("Models/Jaime/Jaime.j3o");
        rootNode.attachChild(jaime);
    }

    /**
     * Callback invoked when the active InputMode changes.
     *
     * @param oldMode the old mode, or null if none
     * @param newMode the new mode, or null if none
     */
    @Override
    public void inputModeChange(InputMode oldMode, InputMode newMode) {
        if (newMode != null) {
            if (helpNode != null) {
                helpNode.removeFromParent();
            }

            float x = 10f;
            float y = cam.getHeight() - 30f;
            float width = cam.getWidth() - 20f;
            float height = cam.getHeight() - 20f;
            Rectangle rectangle = new Rectangle(x, y, width, height);

            float space = 20f;
            helpNode = HelpUtils.buildNode(newMode, rectangle, guiFont, space);
            guiNode.attachChild(helpNode);
        }
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();

        dim.bind("dump viewport", KeyInput.KEY_P);
        dim.bind("next exponent", KeyInput.KEY_EQUALS, KeyInput.KEY_NUMPAD6);
        dim.bind("previous exponent", KeyInput.KEY_MINUS, KeyInput.KEY_NUMPAD4);

        dim.bindSignal(CameraInput.FLYCAM_LOWER, KeyInput.KEY_DOWN);
        dim.bindSignal(CameraInput.FLYCAM_RISE, KeyInput.KEY_UP);
        dim.bindSignal("orbitLeft", KeyInput.KEY_LEFT);
        dim.bindSignal("orbitRight", KeyInput.KEY_RIGHT);

        dim.bind("toggle help", KeyInput.KEY_H);
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
            switch (actionString) {
                case "dump viewport":
                    dumper.dump(viewPort);
                    return;
                case "next exponent":
                    increaseExponent(+1);
                    return;
                case "previous exponent":
                    increaseExponent(-1);
                    return;
                case "toggle help":
                    toggleHelp();
                    return;
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        updateStatusLine();
    }
    // *************************************************************************
    // private methods

    /**
     * Add a horizontal square to the scene.
     */
    private void addFloor() {
        float x1 = -2f;
        float x2 = 2f;
        float y1 = -2f;
        float y2 = 2f;
        float zNorm = -1f;
        Mesh squareMesh = new RectangleMesh(x1, x2, y1, y2, zNorm);
        Geometry floor = new Geometry("floor", squareMesh);
        floor.rotate(FastMath.HALF_PI, 0f, 0f);

        ColorRGBA floorColor = new ColorRGBA(0.2f, 0.4f, 0.1f, 1f);
        Material floorMaterial
                = MyAsset.createShadedMaterial(assetManager, floorColor);
        floorMaterial.setName("floorMaterial");
        floor.setMaterial(floorMaterial);
        rootNode.attachChild(floor);
    }

    /**
     * Add lighting and shadows to the scene.
     */
    private void addLighting() {
        ColorRGBA ambientColor = new ColorRGBA(0.2f, 0.2f, 0.2f, 1f);
        AmbientLight ambient = new AmbientLight(ambientColor);
        rootNode.addLight(ambient);
        ambient.setName("ambient");

        Vector3f direction = new Vector3f(1f, -2f, -2f).normalizeLocal();
        DirectionalLight sun = new DirectionalLight(direction);
        rootNode.addLight(sun);
        sun.setName("sun");

        DirectionalLightShadowRenderer dlsr
                = new DirectionalLightShadowRenderer(assetManager, 4_096, 3);
        dlsr.setLight(sun);
        dlsr.setShadowIntensity(0.5f);
        viewPort.addProcessor(dlsr);
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        float near = 0.002f;
        float far = 20f;
        MyCamera.setNearFar(cam, near, far);

        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(3f);

        cam.setName("cam");
        cam.setLocation(new Vector3f(1f, 2.8f, 3.4f));
        cam.setRotation(new Quaternion(-0.05f, 0.942325f, -0.2883f, -0.16263f));

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, "orbitLeft", "orbitRight");
        stateManager.attach(orbitState);
    }

    /**
     * Configure the PhysicsDumper during startup.
     */
    private void configureDumper() {
        dumper.setDumpTransform(true);
    }

    /**
     * Increase the exponent by the specified number of steps.
     *
     * @param numSteps the number of steps to increase (negative to decrease)
     */
    private void increaseExponent(int numSteps) {
        exponent *= FastMath.exp(0.1f * numSteps);
        filter.setExponent(exponent);
    }

    /**
     * Toggle visibility of the helpNode.
     */
    private void toggleHelp() {
        if (helpNode.getCullHint() == Spatial.CullHint.Always) {
            helpNode.setCullHint(Spatial.CullHint.Never);
        } else {
            helpNode.setCullHint(Spatial.CullHint.Always);
        }
    }

    /**
     * Update the status line.
     */
    private void updateStatusLine() {
        String message = String.format("exponent = %6.3f", exponent);
        statusLine.setText(message);
    }
}
