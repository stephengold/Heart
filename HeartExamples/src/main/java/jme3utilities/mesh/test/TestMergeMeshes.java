/*
 Copyright (c) 2022, Stephen Gold
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

import com.jme3.font.Rectangle;
import com.jme3.input.CameraInput;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.CenterQuad;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;
import com.jme3.util.clone.Cloner;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyCamera;
import jme3utilities.MyMesh;
import jme3utilities.MySpatial;
import jme3utilities.debug.Dumper;
import jme3utilities.math.MyBuffer;
import jme3utilities.ui.AbstractDemo;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;

/**
 * Demonstrate translation and merging of meshes.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestMergeMeshes extends AbstractDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestMergeMeshes.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestMergeMeshes.class.getSimpleName();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestMergeMeshes application.
     *
     * @param unused array of command-line arguments (not null)
     */
    public static void main(String[] unused) {
        /*
         * Mute the chatty loggers found in some imported packages.
         */
        Heart.setLoggingLevels(Level.WARNING);

        TestMergeMeshes application = new TestMergeMeshes();
        /*
         * Customize the window's title bar.
         */
        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setTitle(applicationName);

        settings.setAudioRenderer(null);
        application.setSettings(settings);
        application.start();
        /*
         * ... and onward to TestMergeMeshes.actionInitializeApplication()!
         */
    }
    // *************************************************************************
    // AbstractDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void actionInitializeApplication() {
        ColorRGBA bgColor = new ColorRGBA(0.2f, 0.2f, 0.2f, 1f);
        viewPort.setBackgroundColor(bgColor);
        /*
         * The dimensions of the board, in terms of squares:
         *  numColumns along the X axis
         *  numRows along the Y axis
         */
        int numColumns = 10;
        int numRows = 10;

        float cameraX = 1f + numColumns / 2f;
        configureCamera(cameraX);
        /*
         * Even squares, such as (0, 0), will be dark green.
         * Odd squares, such as (0, 1), will be white.
         */
        ColorRGBA evenColor = new ColorRGBA(0f, 0.3f, 0f, 1f);
        ColorRGBA oddColor = ColorRGBA.White;
        /*
         * Create one checkerboard (board1) with a mesh for each square
         * and another (board2) with a merged mesh.
         */
        Node board1 = new Node("board1");
        Mesh mesh2 = null;
        for (int rowI = 0; rowI < numRows; ++rowI) {
            for (int columnI = 0; columnI < numColumns; ++columnI) {
                boolean isOddSquare = ((rowI + columnI) % 2 == 1);

                // Calculate the center location in world coordinates.
                float x = columnI - numColumns / 2f;
                float y = rowI - numRows / 2f;
                float z = isOddSquare ? 0.4f : 0f;
                Vector3f centerInWorld = new Vector3f(x, y, z);

                // Create a square mesh with the appropriate color.
                ColorRGBA color = isOddSquare ? oddColor : evenColor;
                Mesh squareMesh = coloredSquare(color);

                // Attach a translated geometry to board1.
                String name = String.format("square[%d,%d]", rowI, columnI);
                Geometry squareGeometry = new Geometry(name, squareMesh);
                board1.attachChild(squareGeometry);
                squareGeometry.move(centerInWorld);

                // Merge a translated clone of squareMesh into mesh2.
                Mesh translatedClone = Cloner.deepClone(squareMesh);
                MyMesh.translate(translatedClone, centerInWorld);
                if (mesh2 == null) {
                    mesh2 = translatedClone;
                } else {
                    mesh2 = MyMesh.merge(mesh2, translatedClone);
                }
            }
        }
        /*
         * MyMesh.merge() returns an unindexed Mesh, which is inefficient.
         * To reduce the complexity of mesh2, we add indices.
         */
        mesh2 = MyMesh.addIndices(mesh2);

        Geometry board2 = new Geometry("board2", mesh2);
        /*
         * Move board2 so that board1 and board2 can be compared visually.
         */
        board2.move(2f * cameraX, 0f, 0f);
        /*
         * Create an unshaded material and apply it to both checkerboards.
         */
        Material material = new Material(assetManager, Materials.UNSHADED);
        material.setBoolean("VertexColor", true);
        board1.setMaterial(material);
        board2.setMaterial(material);
        /*
         * Attach both checkerboards to the scene graph.
         */
        rootNode.attachChild(board1);
        rootNode.attachChild(board2);
        /*
         * Compare their complexity.
         */
        int numGeometries1 = MySpatial.countSpatials(board1, Geometry.class);
        int numVertices1 = MySpatial.countVertices(board1);
        System.out.printf(
                "With a mesh for each square:  %d geometries and %d vertices%n",
                numGeometries1, numVertices1);

        int numGeometries2 = MySpatial.countSpatials(board2, Geometry.class);
        int numVertices2 = MySpatial.countVertices(board2);
        System.out.printf(
                "With a merged mesh:  %d geometry and %d vertices%n",
                numGeometries2, numVertices2);
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
        /*
         * The help node can't be created until all hotkeys are bound.
         */
        float x = 10f;
        float y = cam.getHeight() - 10f;
        float width = cam.getWidth() - 20f;
        float height = cam.getHeight() - 20f;
        Rectangle rectangle = new Rectangle(x, y, width, height);
        attachHelpNode(rectangle);
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
                case "dump render":
                    new Dumper().dump(renderManager);
                    return;
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Generate a centered 1x1 square mesh in the X-Y plane, with all vertices
     * having the specified color.
     *
     * @param color the desired color (not null, unaffected)
     * @return a new Mesh with no normals and no texture coordinates
     */
    private static Mesh coloredSquare(ColorRGBA color) {
        float width = 1f; // mesh units
        float height = 1f; // mesh units
        Mesh result = new CenterQuad(width, height);
        result.clearBuffer(VertexBuffer.Type.Normal);
        result.clearBuffer(VertexBuffer.Type.TexCoord);
        /*
         * Add a color buffer.
         */
        int numVertices = result.getVertexCount();
        int floatsPerColor = 4;
        FloatBuffer colorBuffer
                = BufferUtils.createFloatBuffer(numVertices * floatsPerColor);
        MyBuffer.fill(colorBuffer, color.r, color.g, color.b, color.a);
        result.setBuffer(VertexBuffer.Type.Color, floatsPerColor, colorBuffer);

        return result;
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera(float cameraX) {
        float near = 0.002f * cameraX;
        float far = 50f * cameraX;
        MyCamera.setNearFar(cam, near, far);

        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(cameraX);

        cam.setName("cam");
        float cameraZ = 4f * cameraX;
        cam.setLocation(new Vector3f(cameraX, 0f, cameraZ));

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, "orbitLeft", "orbitRight");
        stateManager.attach(orbitState);
    }
}
