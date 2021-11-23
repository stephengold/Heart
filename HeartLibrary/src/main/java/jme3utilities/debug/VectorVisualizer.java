/*
 Copyright (c) 2014-2021, Stephen Gold
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
package jme3utilities.debug;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.util.clone.Cloner;
import java.io.IOException;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.SubtreeControl;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * A SubtreeControl to visualize a vector.
 * <p>
 * The controlled spatial must be a Node.
 * <p>
 * A new Control is disabled by default. When enabled, it attaches an arrow
 * geometry to its subtree.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class VectorVisualizer extends SubtreeControl {
    // *************************************************************************
    // constants and loggers

    /**
     * default depth-test setting (disabled)
     */
    final private static boolean defaultDepthTest = false;
    /**
     * magic width value used to specify a solid arrow
     */
    final public static float widthForSolid = 0f;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(VectorVisualizer.class.getName());
    /**
     * asset path to the solid arrow model
     */
    final private static String modelAssetPath
            = "Models/indicators/arrow/arrow.j3o";
    /**
     * name for the subtree node
     */
    final private static String subtreeName = "vector node";
    /**
     * field names for serialization
     */
    final private static String tagColor = "color";
    final private static String tagDepthTest = "depthTest";
    final private static String tagLineWidth = "lineWidth";
    final private static String tagTipOffset = "tipOffset";
    // *************************************************************************
    // fields

    /**
     * asset manager to use (not null)
     */
    private AssetManager assetManager;
    /**
     * true &rarr; enabled, false &rarr; disabled
     *
     * The test provides depth cues, but often hides the arrow.
     */
    private boolean depthTest = defaultDepthTest;
    /**
     * color of the arrow
     */
    private ColorRGBA color = new ColorRGBA(1f, 1f, 1f, 1f);
    /**
     * line width for wireframe arrow (in pixels, &ge;1) or 0 for solid arrow
     */
    private float lineWidth;
    /**
     * offset of the arrow's tip (in local coordinates)
     */
    private Vector3f tipOffset = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil.
     */
    protected VectorVisualizer() {
        assetManager = null;
    }

    /**
     * Instantiate a (disabled) arrow.
     *
     * @param manager for loading material definitions (not null, alias created)
     * @param width thickness of the arrow (in pixels, &ge;0)
     */
    public VectorVisualizer(AssetManager manager, float width) {
        super();
        Validate.nonNull(manager, "asset manager");
        Validate.nonNegative(width, "line width");

        this.assetManager = manager;
        this.lineWidth = width;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the color of the arrow.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return the color value (either storeResult or a new instance)
     */
    public ColorRGBA color(ColorRGBA storeResult) {
        if (storeResult == null) {
            return color.clone();
        } else {
            return storeResult.set(color);
        }
    }

    /**
     * Determine the depth-test setting.
     * <p>
     * The test provides depth cues, but might hide portions of the
     * visualization.
     *
     * @return true if the test is enabled, otherwise false
     */
    public boolean isDepthTest() {
        return depthTest;
    }

    /**
     * Determine the line width of the arrow.
     *
     * @return width (in pixels, &ge;1) or 0 for a solid arrow
     */
    public float lineWidth() {
        assert lineWidth >= 0f : lineWidth;
        return lineWidth;
    }

    /**
     * Alter the color of the arrow.
     *
     * @param color the desired color (not null, unaffected)
     */
    public void setColor(ColorRGBA color) {
        Validate.nonNull(color, "color");
        this.color.set(color);
    }

    /**
     * Alter the depth-test setting. The test provides depth cues, but often
     * hides the arrow.
     *
     * @param newSetting true to enable test, false to disable it
     */
    public void setDepthTest(boolean newSetting) {
        this.depthTest = newSetting;
    }

    /**
     * Alter the line width.
     *
     * @param width (in pixels, &ge;1) or 0 for a solid arrow
     */
    public void setLineWidth(float width) {
        Validate.inRange(width, "width", 0f, Float.MAX_VALUE);
        this.lineWidth = width;
    }

    /**
     * Alter the offset of the arrow's tip in local coordinates.
     *
     * @param offset the desired offset (not null, unaffected)
     */
    public void setTipOffset(Vector3f offset) {
        Validate.nonNull(offset, "offset");
        this.tipOffset.set(offset);
    }

    /**
     * Determine the location of the arrow's tip.
     *
     * @return a new vector (in world coordinates) or null if not displayed
     */
    public Vector3f tipLocation() {
        Vector3f result = null;
        if (isEnabled()) {
            Node subtreeNode = (Node) getSubtree();
            result = subtreeNode.localToWorld(tipOffset, null);
        }

        return result;
    }

    /**
     * Determine the offset of the arrow's tip in local coordinates.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return the offset (in local coordinates, either storeResult or a new
     * instance)
     */
    public Vector3f tipOffset(Vector3f storeResult) {
        if (storeResult == null) {
            return tipOffset.clone();
        } else {
            return storeResult.set(tipOffset);
        }
    }
    // *************************************************************************
    // SubtreeControl methods

    /**
     * Create a shallow copy of this Control.
     *
     * @return a new Control, equivalent to this one
     * @throws CloneNotSupportedException if superclass isn't cloneable
     */
    @Override
    public VectorVisualizer clone() throws CloneNotSupportedException {
        VectorVisualizer clone = (VectorVisualizer) super.clone();
        return clone;
    }

    /**
     * Convert this shallow-cloned Control into a deep-cloned one, using the
     * specified Cloner and original to resolve copied fields.
     *
     * @param cloner the Cloner currently cloning this Control
     * @param original the instance from which this Control was shallow-cloned
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);

        color = color.clone();
        tipOffset = tipOffset.clone();
    }

    /**
     * Callback invoked when the controlled spatial's geometric state is about
     * to be updated, once per frame while attached and enabled.
     *
     * @param updateInterval time interval between updates (in seconds, &ge;0)
     */
    @Override
    protected void controlUpdate(float updateInterval) {
        super.controlUpdate(updateInterval);

        Node subtreeNode = (Node) getSubtree();
        int numChildren = subtreeNode.getQuantity();
        if (numChildren != 1) {
            subtreeNode.detachAllChildren();
            addArrow();
        } else {
            Geometry arrow = (Geometry) subtreeNode.getChild(0);
            Mesh mesh = arrow.getMesh();
            boolean isWireArrow = mesh instanceof Arrow;

            if (lineWidth >= 1f && isWireArrow) {
                updateWireArrow();
            } else if (lineWidth < 1f && !isWireArrow) {
                updateSolidArrow();
            } else {
                subtreeNode.detachAllChildren();
                addArrow();
            }
        }
    }

    /**
     * De-serialize this Control from the specified importer, for example when
     * loading from a J3O file.
     *
     * @param importer (not null)
     * @throws IOException from the importer
     */
    @Override
    public void read(JmeImporter importer) throws IOException {
        super.read(importer);
        assetManager = importer.getAssetManager();
        InputCapsule capsule = importer.getCapsule(this);

        color = (ColorRGBA) capsule.readSavable(tagColor, null);
        depthTest = capsule.readBoolean(tagDepthTest, defaultDepthTest);
        lineWidth = capsule.readFloat(tagLineWidth, 0f);
        tipOffset = (Vector3f) capsule.readSavable(tagTipOffset, null);
    }

    /**
     * Alter the visibility of the visualization.
     *
     * @param newState if true, reveal the visualization; if false, hide it
     */
    @Override
    public void setEnabled(boolean newState) {
        if (newState && getSubtree() == null) {
            /*
             * Before enabling this Control for the first time,
             * create the subtree.
             */
            Node subtreeNode = new Node(subtreeName);
            subtreeNode.setQueueBucket(RenderQueue.Bucket.Transparent);
            subtreeNode.setShadowMode(RenderQueue.ShadowMode.Off);
            setSubtree(subtreeNode);
        }

        super.setEnabled(newState);
    }

    /**
     * Serialize this Control to the specified exporter, for example when saving
     * to a J3O file.
     *
     * @param exporter (not null)
     * @throws IOException from the exporter
     */
    @Override
    public void write(JmeExporter exporter) throws IOException {
        super.write(exporter);
        OutputCapsule capsule = exporter.getCapsule(this);

        capsule.write(color, tagColor, null);
        capsule.write(depthTest, tagDepthTest, defaultDepthTest);
        capsule.write(lineWidth, tagLineWidth, 0f);
        capsule.write(tipOffset, tagTipOffset, null);
    }
    // *************************************************************************
    // private methods

    /**
     * Create an arrow geometry and add it to the subtree.
     */
    private void addArrow() {
        assert ((Node) getSubtree()).getQuantity() == 0;
        if (lineWidth >= 1f) {
            addWireArrow();
        } else {
            addSolidArrow();
        }
    }

    /**
     * Create and attach a solid arrow geometry.
     */
    private void addSolidArrow() {
        assert assetManager != null;
        assert color != null;

        Node node = (Node) assetManager.loadModel(modelAssetPath);
        Node node2 = (Node) node.getChild(0);
        Node node3 = (Node) node2.getChild(0);
        Geometry geometry = (Geometry) node3.getChild(0);
        ((Node) getSubtree()).attachChild(geometry);

        Material material = MyAsset.createUnshadedMaterial(assetManager, color);
        geometry.setMaterial(material);
        material.setName("arrowMaterial");

        geometry.setName("arrow");
        updateSolidArrow();
    }

    /**
     * Create and attach a wire arrow geometry.
     */
    private void addWireArrow() {
        Arrow mesh = new Arrow(tipOffset);
        Geometry geometry = new Geometry("arrow", mesh);
        Node subtreeNode = (Node) getSubtree();
        subtreeNode.attachChild(geometry);

        Material material
                = MyAsset.createWireframeMaterial(assetManager, color);
        geometry.setMaterial(material);
        material.setName("arrowMaterial");

        RenderState state = material.getAdditionalRenderState();
        state.setDepthTest(depthTest);
        state.setLineWidth(lineWidth);
    }

    /**
     * Update the "Color" parameter in the specified Material.
     *
     * @param material the Material to update (not null)
     */
    private void updateColor(Material material) {
        ColorRGBA oldColor = material.getParamValue("Color");
        if (!oldColor.equals(color)) {
            material.setColor("Color", color.clone());
        }
    }

    /**
     * Update the color, depth test, and tip offset of the existing solid arrow
     * geometry.
     */
    private void updateSolidArrow() {
        Node subtreeNode = (Node) getSubtree();
        Geometry geometry = (Geometry) subtreeNode.getChild(0);

        float length = tipOffset.length();
        geometry.setLocalScale(length);
        if (length > 0f) {
            Vector3f xDir = tipOffset.clone();
            Vector3f yDir = new Vector3f();
            Vector3f zDir = new Vector3f();
            MyVector3f.generateBasis(xDir, yDir, zDir);
            Quaternion orientation = new Quaternion();
            orientation.fromAxes(xDir, yDir, zDir);
            geometry.setLocalRotation(orientation);
        }

        Material material = geometry.getMaterial();
        updateColor(material);
        RenderState state = material.getAdditionalRenderState();
        state.setDepthTest(depthTest);
    }

    /**
     * Update the color, depth test, line width, and tip offset of the existing
     * wire arrow geometry.
     */
    private void updateWireArrow() {
        Node subtreeNode = (Node) getSubtree();
        Geometry geometry = (Geometry) subtreeNode.getChild(0);
        Arrow mesh = (Arrow) geometry.getMesh();
        mesh.setArrowExtent(tipOffset);

        Material material = geometry.getMaterial();
        updateColor(material);
        RenderState state = material.getAdditionalRenderState();
        state.setDepthTest(depthTest);
        state.setLineWidth(lineWidth);
    }
}
