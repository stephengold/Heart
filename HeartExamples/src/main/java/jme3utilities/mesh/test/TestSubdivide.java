/*
 Copyright (c) 2022-2023, Stephen Gold
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
import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.CenterQuad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyMesh;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;

/**
 * Test/demonstrate the MyMesh.subdivideTriangles() utility method.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestSubdivide extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestSubdivide.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestSubdivide.class.getSimpleName();
    /**
     * action string to dump the main scene graph
     */
    final private static String asDump = "dump";
    /**
     * action string to increment the subdivision ratio
     */
    final private static String asNextRatio = "next ratio";
    /**
     * action string to orbit to the camera's left
     */
    final private static String asOrbitLeft = "orbit left";
    /**
     * action string to orbit to the camera's right
     */
    final private static String asOrbitRight = "orbit right";
    /**
     * action string to decrement the subdivision ratio
     */
    final private static String asPreviousRatio = "previous ratio";
    /**
     * action string to toggle wireframe material
     */
    final private static String asToggleWireframe = "toggle wireframe";
    // *************************************************************************
    // fields

    /**
     * status displayed in the upper-left corner of the GUI node
     */
    private static BitmapText statusText;
    /**
     * enable for wireframe material
     */
    private static boolean wireframe = false;
    /**
     * dump debugging information to System.out
     */
    final private static Dumper dumper = new Dumper();
    /**
     * visualize a subdivided quad
     */
    private static Geometry quadGeometry;
    /**
     * original edge lengths divided by displayed edge lengths (&ge;1)
     */
    private static int ratio = 2;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestSubdivide application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TestSubdivide application = new TestSubdivide();
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
        addLighting();
        configureCamera();
        configureDumper();
        generateMaterials();
        updateMeshes();

        // Hide the render-statistics overlay.
        stateManager.getState(StatsAppState.class).toggleStats();

        // Add the status text to the GUI.
        statusText = new BitmapText(guiFont);
        guiNode.attachChild(statusText);

        super.acorusInit();
    }

    /**
     * Initialize the library of named materials. Invoke during startup.
     */
    @Override
    public void generateMaterials() {
        String assetPath = "Interface/Logo/Monkey.jpg";
        boolean flipY = true;
        TextureKey key = new TextureKey(assetPath, flipY);
        key.setGenerateMips(true);
        Texture texture = assetManager.loadTexture(key);
        Material mat = MyAsset.createShadedMaterial(assetManager, texture);
        registerMaterial("shaded texture", mat);

        mat = MyAsset.createWireframeMaterial(assetManager, ColorRGBA.White);
        mat.getAdditionalRenderState()
                .setFaceCullMode(RenderState.FaceCullMode.Off);
        registerMaterial("wireframe", mat);

    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();

        dim.bind(asDump, KeyInput.KEY_P);
        dim.bind(asNextRatio, KeyInput.KEY_EQUALS, KeyInput.KEY_NUMPAD6);
        dim.bind(asPreviousRatio, KeyInput.KEY_MINUS, KeyInput.KEY_NUMPAD4);
        dim.bind(asToggleHelp, KeyInput.KEY_H);
        dim.bind(asToggleWireframe, KeyInput.KEY_TAB);

        dim.bindSignal(asOrbitLeft, KeyInput.KEY_LEFT);
        dim.bindSignal(asOrbitRight, KeyInput.KEY_RIGHT);
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

                case asNextRatio:
                    ++ratio;
                    updateMeshes();
                    return;

                case asPreviousRatio:
                    ratio = Math.max(1, ratio - 1);
                    updateMeshes();
                    return;

                case asToggleWireframe:
                    wireframe = !wireframe;
                    updateMeshes();
                    return;

                default:
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
        statusText.setLocalTranslation(0f, newHeight, 0f);
        super.onViewPortResize(newWidth, newHeight);
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
    // *************************************************************************
    // private methods

    /**
     * Add lighting to the scene during startup.
     */
    private void addLighting() {
        // Set the background to light blue.
        ColorRGBA backgroundColor = new ColorRGBA(0.2f, 0.2f, 1f, 1f);
        viewPort.setBackgroundColor(backgroundColor);

        ColorRGBA ambientColor = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
        AmbientLight ambient = new AmbientLight(ambientColor);
        rootNode.addLight(ambient);

        Vector3f direction = new Vector3f(1f, -2f, -2f).normalizeLocal();
        DirectionalLight sun = new DirectionalLight(direction);
        rootNode.addLight(sun);
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(10f);

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, asOrbitLeft, asOrbitRight);
        stateManager.attach(orbitState);
    }

    /**
     * Configure the Dumper during startup.
     */
    private void configureDumper() {
        dumper.setDumpTransform(true);
        //dumper.setDumpVertex(true);
    }

    private void updateMeshes() {
        String materialName = wireframe ? "wireframe" : "shaded texture";
        Material material = findMaterial(materialName);

        if (quadGeometry != null) {
            quadGeometry.removeFromParent();
        }
        Mesh mesh = new CenterQuad(2f, 2f);
        if (ratio > 1) {
            mesh = MyMesh.subdivideTriangles(mesh, ratio);
        }
        quadGeometry = new Geometry("quad", mesh);
        quadGeometry.setMaterial(material);
        rootNode.attachChild(quadGeometry);
    }

    /**
     * Update the status text in the GUI.
     */
    private void updateStatusText() {
        String message
                = String.format("ratio = %d   wire = %s", ratio, wireframe);
        statusText.setText(message);
    }
}
