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
package jme3utilities.mesh.test;

import com.jme3.font.BitmapText;
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
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyMesh;
import jme3utilities.MySpatial;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;
import jme3utilities.mesh.Cone;
import jme3utilities.mesh.Dodecahedron;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.mesh.Icosahedron;
import jme3utilities.mesh.Icosphere;
import jme3utilities.mesh.Octahedron;
import jme3utilities.mesh.Octasphere;
import jme3utilities.mesh.Prism;
import jme3utilities.mesh.Tetrahedron;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;

/**
 * Test the Cone, DomeMesh, Icosahedron, Icosphere, Octahedron, Prism, and
 * Tetrahedron classes. Also test the MyMesh.reverseNormals() and
 * MyMesh.reverseWinding() methods.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestSolidMeshes extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestSolidMeshes.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestSolidMeshes.class.getSimpleName();
    /**
     * names of all library materials, in alphabetic order
     */
    final private static String[] materialNames = {
        "back-only lit",
        "back-only wireframe",
        "debug",
        "front-only lit",
        "front-only wireframe",
        "unshaded texture"
    };
    // *************************************************************************
    // fields

    /**
     * status displayed in the upper-left corner of the GUI node
     */
    private BitmapText statusText;
    /**
     * true if the normals are inward-facing
     */
    private boolean inwardNormals;
    /**
     * true if the triangles are wound to be inward-facing
     */
    private boolean inwardWinding;
    /**
     * dump debugging information to System.out
     */
    final private Dumper dumper = new Dumper();
    /**
     * all geometries in the scene
     */
    private List<Geometry> allGeometries;
    /**
     * name of the material that's currently applied (or about to be applied)
     */
    private String materialName = materialNames[1];
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestSolidMeshes application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TestSolidMeshes application = new TestSolidMeshes();
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
        generateMaterials();

        addGeometries();
        addLighting();
        applyMaterial();
        /*
         * Add the status text to the GUI.
         */
        statusText = new BitmapText(guiFont);
        guiNode.attachChild(statusText);

        super.acorusInit();
    }

    /**
     * Initialize the library of named materials. Invoke during startup.
     */
    @Override
    public void generateMaterials() {
        ColorRGBA green = new ColorRGBA(0f, 0.12f, 0f, 1f);
        Material mat;

        mat = MyAsset.createShadedMaterial(assetManager, green);
        registerMaterial("front-only lit", mat);

        mat = MyAsset.createShadedMaterial(assetManager, green);
        mat.getAdditionalRenderState()
                .setFaceCullMode(RenderState.FaceCullMode.Front);
        registerMaterial("back-only lit", mat);

        mat = MyAsset.createWireframeMaterial(assetManager, ColorRGBA.White);
        registerMaterial("front-only wireframe", mat);

        mat = MyAsset.createWireframeMaterial(assetManager, ColorRGBA.White);
        mat.getAdditionalRenderState()
                .setFaceCullMode(RenderState.FaceCullMode.Front);
        registerMaterial("back-only wireframe", mat);

        boolean isSrgb = renderer.isMainFrameBufferSrgb();
        float gamma = isSrgb ? 2.2f : 1f;
        mat = MyAsset.createDebugMaterial(assetManager, gamma);
        registerMaterial("debug", mat);

        String assetPath = "Interface/Logo/Monkey.jpg";
        boolean generateMips = true;
        Texture texture
                = MyAsset.loadTexture(assetManager, assetPath, generateMips);
        mat = MyAsset.createUnshadedMaterial(assetManager, texture);
        registerMaterial("unshaded texture", mat);
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();

        dim.bind("dump", KeyInput.KEY_P);
        dim.bind("flip normals", KeyInput.KEY_F);
        dim.bind("next material", KeyInput.KEY_N);
        dim.bindSignal("orbitLeft", KeyInput.KEY_LEFT);
        dim.bindSignal("orbitRight", KeyInput.KEY_RIGHT);
        dim.bind("reverse winding", KeyInput.KEY_R);
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
                case "dump":
                    dumper.dump(rootNode);
                    return;

                case "flip normals":
                    flipNormals();
                    return;

                case "next material":
                    nextMaterial();
                    return;

                case "reverse winding":
                    reverseWinding();
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
     * Add geometries to the scene, without materials.
     */
    private void addGeometries() {
        boolean generateNormals, generatePyramid;
        float height, radius;
        Geometry geometry;
        int numSides, radialSamples, zSamples;
        Mesh mesh;

        inwardNormals = false;
        inwardWinding = false;

        radius = 1f;
        zSamples = 32;
        radialSamples = 32;
        mesh = new Sphere(zSamples, radialSamples, radius);
        geometry = new Geometry("sphere-original", mesh);
        rootNode.attachChild(geometry);
        geometry.move(-2f, -2f, 0f);

        numSides = 40;
        height = 1f;
        generatePyramid = false;
        mesh = new Cone(numSides, radius, height, generatePyramid);
        mesh = MyMesh.addIndices(mesh);
        geometry = new Geometry("cone", mesh);
        rootNode.attachChild(geometry);
        geometry.move(-2f, 0f, 0f);

        int rimSamples = 20;
        int quadrantSamples = 6;
        float topU = 0.5f;
        float topV = 0.5f;
        float uvScale = 0.4f;
        mesh = new DomeMesh(rimSamples, quadrantSamples, topU, topV, uvScale,
                inwardWinding);
        geometry = new Geometry("dome", mesh);
        rootNode.attachChild(geometry);
        geometry.move(-2f, 2f, 0f);

        generateNormals = true;
        mesh = new Icosahedron(radius, generateNormals);
        geometry = new Geometry("icosahedron", mesh);
        rootNode.attachChild(geometry);
        geometry.move(-2f, 4f, 0f);

        mesh = new Sphere(zSamples, radialSamples, radius);
        ((Sphere) mesh).setTextureMode(Sphere.TextureMode.Projected);
        geometry = new Geometry("sphere-projected", mesh);
        rootNode.attachChild(geometry);
        geometry.move(0f, -2f, 0f);

        int refineSteps = 3;
        mesh = new Icosphere(refineSteps, radius);
        geometry = new Geometry("icoSphere", mesh);
        rootNode.attachChild(geometry);
        geometry.move(0f, 0f, 0f);

        mesh = new Octahedron(radius, generateNormals);
        geometry = new Geometry("octahedron", mesh);
        rootNode.attachChild(geometry);
        geometry.move(0f, 2f, 0f);

        numSides = 3;
        mesh = new Prism(numSides, radius, height, generateNormals);
        geometry = new Geometry("prism", mesh);
        rootNode.attachChild(geometry);
        geometry.move(0f, 4f, 0f);

        mesh = new Sphere(zSamples, radialSamples, radius);
        ((Sphere) mesh).setTextureMode(Sphere.TextureMode.Polar);
        geometry = new Geometry("sphere-polar", mesh);
        rootNode.attachChild(geometry);
        geometry.move(2f, -2f, 0f);

        numSides = 4;
        generatePyramid = true;
        mesh = new Cone(numSides, radius, height, generatePyramid);
        geometry = new Geometry("pyramid", mesh);
        rootNode.attachChild(geometry);
        geometry.move(2f, 0f, 0f);

        mesh = new Tetrahedron(radius, generateNormals);
        geometry = new Geometry("tetrahedron", mesh);
        rootNode.attachChild(geometry);
        geometry.move(2f, 2f, 0f);

        mesh = new Dodecahedron(radius, Mesh.Mode.Triangles);
        mesh = MyMesh.expand(mesh);
        MyMesh.generateNormals(mesh);
        geometry = new Geometry("dodecahedron", mesh);
        rootNode.attachChild(geometry);
        geometry.move(2f, 4f, 0f);

        refineSteps = 3;
        mesh = new Octasphere(refineSteps, radius);
        geometry = new Geometry("octaSphere", mesh);
        rootNode.attachChild(geometry);
        geometry.move(4f, 0f, 0f);

        allGeometries
                = MySpatial.listSpatials(rootNode, Geometry.class, null);
    }

    /**
     * Add lighting to the scene during startup.
     */
    private void addLighting() {
        ColorRGBA gray = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
        viewPort.setBackgroundColor(gray);

        ColorRGBA ambientColor = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
        AmbientLight ambient = new AmbientLight(ambientColor);
        rootNode.addLight(ambient);

        Vector3f direction = new Vector3f(1f, -2f, -2f).normalizeLocal();
        DirectionalLight sun = new DirectionalLight(direction);
        rootNode.addLight(sun);
    }

    /**
     * Apply the current Material to all geometries in the scene.
     */
    private void applyMaterial() {
        Material material = findMaterial(materialName);
        for (Geometry geometry : allGeometries) {
            geometry.setMaterial(material);
        }
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(10f);

        cam.setLocation(new Vector3f(-0.9f, 6.8f, 9f));
        cam.setRotation(new Quaternion(0.026f, 0.9644f, -0.243f, 0.1f));

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, "orbitLeft", "orbitRight");
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
     * Reverse the normals of all meshes in the scene.
     */
    private void flipNormals() {
        for (Geometry geometry : allGeometries) {
            Mesh mesh = geometry.getMesh();
            MyMesh.reverseNormals(mesh);
            mesh.setDynamic();
        }
        inwardNormals = !inwardNormals;
    }

    /**
     * Apply the next Material to all geometries in the scene.
     */
    private void nextMaterial() {
        materialName = advanceString(materialNames, materialName, 1);
        applyMaterial();
    }

    /**
     * Reverse the triangle winding of all meshes in the scene.
     */
    private void reverseWinding() {
        for (Geometry geometry : allGeometries) {
            Mesh mesh = geometry.getMesh();
            MyMesh.reverseWinding(mesh);
            mesh.setDynamic();
        }
        inwardWinding = !inwardWinding;
    }

    /**
     * Update the status text in the GUI.
     */
    private void updateStatusText() {
        String materialName = allGeometries.get(0).getMaterial().getName();
        String message = String.format(
                "material=%s  normals=%s  winding=%s",
                MyString.quote(materialName),
                inwardNormals ? "in" : "out",
                inwardWinding ? "in" : "out");
        statusText.setText(message);
    }
}
