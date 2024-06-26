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
package jme3utilities.debug.test;

import com.jme3.animation.AnimControl;
import com.jme3.animation.SkeletonControl;
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
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.InfluenceUtil;
import jme3utilities.MyAsset;
import jme3utilities.MyCamera;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;
import jme3utilities.debug.SkeletonVisualizer;
import jme3utilities.mesh.RectangleMesh;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;

/**
 * Test the SkeletonVisualizer class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestSkeletonVisualizer extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestSkeletonVisualizer.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestSkeletonVisualizer.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * status displayed in the upper-left corner of the GUI node
     */
    private static BitmapText statusLine;
    /**
     * dump debugging information to System.out
     */
    final private static Dumper dumper = new Dumper();
    /**
     * visualizer for the skeleton of the C-G model
     */
    private static SkeletonVisualizer sv;
    // *************************************************************************
    // constructors

    /**
     * Instantiate the TestSkeletonVisualizer application.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public TestSkeletonVisualizer() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestSkeletonVisualizer application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TestSkeletonVisualizer application = new TestSkeletonVisualizer();
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
    // AcorusDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void acorusInit() {
        configureCamera();
        configureDumper();

        // Set the background to light blue.
        ColorRGBA backgroundColor = new ColorRGBA(0.2f, 0.2f, 1f, 1f);
        viewPort.setBackgroundColor(backgroundColor);

        addFloor();
        addLighting();
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Add the status text to the GUI.
        statusLine = new BitmapText(guiFont);
        statusLine.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusLine);

        super.acorusInit();

        Spatial jaime = assetManager.loadModel("Models/Jaime/Jaime.j3o");
        rootNode.attachChild(jaime);

        AnimControl animControl = jaime.getControl(AnimControl.class);
        animControl.createChannel().setAnim("Punches");
        SkeletonControl sc = jaime.getControl(SkeletonControl.class);
        // or, to test the new animation system:
        // AnimMigrationUtils.migrate(jaime);
        // jaime.getControl(AnimComposer.class).setCurrentAction("Punches");
        // SkinningControl sc = jaime.getControl(SkinningControl.class);

        sv = new SkeletonVisualizer(assetManager, sc);
        sv.setLineColor(ColorRGBA.Yellow);
        /*
         * Clean up Jaime's skeleton visualization by hiding the "IK" bones,
         * which don't influence any mesh vertices.
         */
        InfluenceUtil.hideNonInfluencers(sv, sc);
        jaime.addControl(sv);
        sv.setEnabled(true);
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
        float height = viewPortHeight - (2f * margin);
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

        dim.bind("dump render", KeyInput.KEY_P);

        dim.bindSignal(CameraInput.FLYCAM_LOWER, KeyInput.KEY_DOWN);
        dim.bindSignal(CameraInput.FLYCAM_RISE, KeyInput.KEY_UP);
        dim.bindSignal("orbitLeft", KeyInput.KEY_LEFT);
        dim.bindSignal("orbitRight", KeyInput.KEY_RIGHT);
        dim.bind(asToggleHelp, KeyInput.KEY_H);
        dim.bind(asTogglePause, KeyInput.KEY_PERIOD, KeyInput.KEY_PAUSE);
        dim.bind("toggle skeleton", KeyInput.KEY_V);
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
                case "dump render":
                    dumper.dump(renderManager);
                    return;
                case "toggle skeleton":
                    toggleSkeleton();
                    return;
                default:
            }
        }

        // The action is not handled: forward it to the superclass.
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
     * Configure the main camera during startup.
     */
    private void configureCamera() {
        float near = 0.002f;
        float far = 20f;
        MyCamera.setNearFar(cam, near, far);

        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(4f);

        cam.setName("cam");
        cam.setLocation(new Vector3f(-1.7f, 1.9f, 1.2f));
        cam.setRotation(new Quaternion(0.1044f, 0.86585f, -0.20378f, 0.44483f));

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, "orbitLeft", "orbitRight");
        stateManager.attach(orbitState);
    }

    /**
     * Configure the Dumper during startup.
     */
    private static void configureDumper() {
        dumper.setDumpTransform(true);
    }

    /**
     * Toggle the SkeletonVisualizer on/off.
     */
    private static void toggleSkeleton() {
        boolean enabled = sv.isEnabled();
        sv.setEnabled(!enabled);
    }

    /**
     * Update the status line in the GUI.
     */
    private void updateStatusLine() {
        boolean isPaused = (speed <= pausedSpeed);
        String message = isPaused ? "  PAUSED" : "";
        statusLine.setText(message);
    }
}
