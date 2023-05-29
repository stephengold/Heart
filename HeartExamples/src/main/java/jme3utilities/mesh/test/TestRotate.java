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
import com.jme3.input.KeyInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyMesh;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.noise.Generator;
import jme3utilities.mesh.Dodecahedron;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;

/**
 * Test/demonstrate the MyMesh.rotate() utility method.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestRotate extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestRotate.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestRotate.class.getSimpleName();
    /**
     * action string to dump the main scene graph
     */
    final private static String asDump = "dump";
    /**
     * action string to orbit to the camera's left
     */
    final private static String asOrbitLeft = "orbit left";
    /**
     * action string to orbit to the camera's right
     */
    final private static String asOrbitRight = "orbit right";
    /**
     * action string to rotate the meshes
     */
    final private static String asRotate = "rotate";
    // *************************************************************************
    // fields

    /**
     * dump debugging information to System.out
     */
    final private static Dumper dumper = new Dumper();
    /**
     * pseudo-random number generator
     */
    final private static Generator generator = new Generator();
    /**
     * visualize a dodecahedron
     */
    private static Geometry dodecaGeometry;
    /**
     * visualize a monkey
     */
    private static Geometry jaimeGeometry;
    /**
     * current mesh rotation
     */
    final private static Quaternion currentRotation = new Quaternion();
    /**
     * final mesh rotation
     */
    final private static Quaternion finalRotation = new Quaternion();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestSubdivide application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TestRotate application = new TestRotate();
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
        loadDodeca();
        loadJaime();

        // Hide the render-statistics overlay.
        stateManager.getState(StatsAppState.class).toggleStats();

        super.acorusInit();
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();

        dim.bind(asDump, KeyInput.KEY_P);
        dim.bind(asRotate, KeyInput.KEY_RETURN);
        dim.bind(asToggleHelp, KeyInput.KEY_H);

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

                case asRotate:
                    generator.nextQuaternion(finalRotation);
                    return;

                default:
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

        float angleBetween
                = MyQuaternion.angleBetween(currentRotation, finalRotation);
        if (angleBetween < 0.0001f) {
            return;
        }

        Quaternion q = currentRotation.inverse();
        finalRotation.mult(q, q);

        float maxAngle = 0.6f * tpf;
        if (angleBetween <= maxAngle) {
            currentRotation.set(finalRotation);
        } else {
            MyQuaternion.pow(q, maxAngle / angleBetween, q);
            q.mult(currentRotation, currentRotation);
        }

        MyMesh.rotate(dodecaGeometry.getMesh(), q);
        MyMesh.rotate(jaimeGeometry.getMesh(), q);
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
        cam.setLocation(new Vector3f(0f, 0f, 4f));

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

    /**
     * Generate and attach a solid green dodecahedron.
     */
    private void loadDodeca() {
        float radius = 0.6f;
        Mesh mesh = new Dodecahedron(radius, Mesh.Mode.Triangles);
        mesh = MyMesh.expand(mesh);
        MyMesh.generateFacetNormals(mesh);

        dodecaGeometry = new Geometry("dodeca", mesh);
        dodecaGeometry.move(1f, 0f, 0f);

        ColorRGBA green = new ColorRGBA(0f, 0.2f, 0f, 1f);
        Material material = MyAsset.createShinyMaterial(assetManager, green);
        dodecaGeometry.setMaterial(material);

        rootNode.attachChild(dodecaGeometry);
    }

    /**
     * Load and attach a Jaime model.
     */
    private void loadJaime() {
        Node cgModel = (Node) assetManager.loadModel("Models/Jaime/Jaime.j3o");
        jaimeGeometry = (Geometry) cgModel.getChild(0);
        RenderState rs = jaimeGeometry.getMaterial().getAdditionalRenderState();
        rs.setFaceCullMode(RenderState.FaceCullMode.Off);
        MyMesh.translate(jaimeGeometry.getMesh(), new Vector3f(0f, -0.6f, 0f));

        jaimeGeometry.move(-1f, 0f, 0f);
        jaimeGeometry.setLocalRotation(Quaternion.IDENTITY);
        rootNode.attachChild(jaimeGeometry);
    }
}
