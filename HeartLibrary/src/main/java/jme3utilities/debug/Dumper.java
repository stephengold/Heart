/*
 Copyright (c) 2013-2023, Stephen Gold
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

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimTrack;
import com.jme3.anim.Armature;
import com.jme3.anim.Joint;
import com.jme3.anim.MorphTrack;
import com.jme3.anim.TransformTrack;
import com.jme3.anim.util.HasLocalTransform;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingVolume;
import com.jme3.font.BitmapText;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.scene.mesh.MorphTarget;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.NamedAppState;
import jme3utilities.Validate;
import jme3utilities.math.MyColor;

/**
 * Dump portions of a jME3 scene graph for debugging.
 * <p>
 * {@link #dump(com.jme3.scene.Spatial)} is the usual interface to this class.
 * The level of detail can be configured dynamically.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Dumper implements Cloneable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Dumper.class.getName());
    // *************************************************************************
    // fields

    /**
     * enable dumping of world bounds (for spatials)
     */
    private boolean dumpBoundsFlag = false;
    /**
     * enable dumping of render-queue bucket assignments (for spatials)
     */
    private boolean dumpBucketFlag = false;
    /**
     * enable dumping of cull hints (for spatials)
     */
    private boolean dumpCullFlag = false;
    /**
     * enable dumping of material parameters (for spatials)
     */
    private boolean dumpMatParamFlag = false;
    /**
     * enable dumping of material-parameter overrides (for spatials)
     */
    private boolean dumpOverrideFlag = false;
    /**
     * enable dumping of shadow modes (for spatials)
     */
    private boolean dumpShadowFlag = false;
    /**
     * enable dumping of location, rotation, and scaling (for spatials)
     */
    private boolean dumpTransformFlag = false;
    /**
     * enable dumping of user data (for spatials)
     */
    private boolean dumpUserFlag = true;
    /**
     * enable dumping of mesh-vertex data (for meshes)
     */
    private boolean dumpVertexFlag = false;
    /**
     * describer for JME objects
     */
    private Describer describer;
    /**
     * maximum number of children per Node to dump
     */
    private int maxChildren = Integer.MAX_VALUE;
    /**
     * stream to use for output: set by constructor
     */
    final protected PrintStream stream;
    /**
     * indentation increment for each level of a dump
     */
    private String indentIncrement = "  ";
    // *************************************************************************
    // constructors

    /**
     * Instantiate a dumper that will use System.out for output.
     */
    public Dumper() {
        this.describer = new Describer();
        this.stream = System.out;
    }

    /**
     * Instantiate a dumper that will use the specified output stream.
     *
     * @param printStream the output stream (not null, alias created)
     */
    public Dumper(PrintStream printStream) {
        Validate.nonNull(printStream, "print stream");

        this.describer = new Describer();
        this.stream = printStream;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Dump the specified AnimClip.
     *
     * @param clip the clip to dump (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(AnimClip clip, String indent) {
        addLine(indent);
        AnimTrack<?>[] tracks = clip.getTracks();
        int numTracks = tracks.length;
        stream.printf("AnimClip%s with %d track%s:",
                MyString.quoteName(clip.getName()), numTracks,
                (numTracks == 1) ? "" : "s");

        String moreIndent = indent + indentIncrement;
        String mmIndent = moreIndent + indentIncrement;
        for (AnimTrack<?> track : tracks) {
            addLine(moreIndent);
            stream.print(track.getClass().getSimpleName());

            if (track instanceof MorphTrack) {
                MorphTrack morphTrack = (MorphTrack) track;
                Geometry target = morphTrack.getTarget();
                String desc = describer.describeTrackTarget(target);
                stream.print(desc);

                addLine(mmIndent);
                stream.print("times");
                float[] times = morphTrack.getTimes(); // alias
                desc = describer.describeFloats(times);
                stream.print(desc);

                addLine(mmIndent);
                stream.print("weights");
                float[] weights = morphTrack.getWeights(); // alias
                desc = describer.describeFloats(weights);
                stream.print(desc);

            } else if (track instanceof TransformTrack) {
                TransformTrack transformTrack = (TransformTrack) track;
                HasLocalTransform target = transformTrack.getTarget();
                String desc = describer.describeTrackTarget(target);
                stream.print(desc);

                addLine(mmIndent);
                stream.print("times");
                float[] times = transformTrack.getTimes(); // alias
                desc = describer.describeFloats(times);
                stream.print(desc);
            }
        }
        stream.println();
    }

    /**
     * Dump the specified AppStateManager.
     *
     * @param manager (not null, unaffected)
     */
    public void dump(AppStateManager manager) {
        Method getInitializing;
        Method getStates;
        Method getTerminating;
        try {
            getInitializing = AppStateManager.class
                    .getDeclaredMethod("getInitializing");
            getStates = AppStateManager.class.getDeclaredMethod("getStates");
            getTerminating = AppStateManager.class
                    .getDeclaredMethod("getTerminating");
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException(exception);
        }
        getInitializing.setAccessible(true);
        getStates.setAccessible(true);
        getTerminating.setAccessible(true);

        AppState[] initializing;
        AppState[] active;
        AppState[] terminating;
        try {
            initializing = (AppState[]) getInitializing.invoke(manager);
            active = (AppState[]) getStates.invoke(manager);
            terminating = (AppState[]) getTerminating.invoke(manager);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }

        stream.println();
        String className = manager.getClass().getSimpleName();
        stream.print(className);

        int numInitializing = initializing.length;
        int numActive = active.length;
        int numTerminating = terminating.length;
        int total = numInitializing + numActive + numTerminating;
        stream.printf(" with %d state", total);
        if (total == 0) {
            stream.println("s.");
            return;
        }

        if (total == 1) {
            stream.print(':');
        } else {
            stream.print("s:");
            String separator = "";
            if (numInitializing > 0) {
                stream.printf(" %d initializing", numInitializing);
                separator = ",";
            }
            if (numActive > 0) {
                stream.printf("%s %d active", separator, numActive);
            }
            if (numTerminating > 0) {
                stream.printf("%s %d terminating", separator, numTerminating);
            }
        }

        for (int index = 0; index < numInitializing; ++index) {
            stream.printf("%n initializing[%d]: ", index);
            dump(initializing[index], indentIncrement);
        }
        for (int index = 0; index < numActive; ++index) {
            stream.printf("%n active[%d]: ", index);
            dump(active[index], indentIncrement);
        }
        for (int index = 0; index < numTerminating; ++index) {
            stream.printf("%n terminating[%d]: ", index);
            dump(terminating[index], indentIncrement);
        }
        stream.println();
    }

    /**
     * Dump the specified Armature and all its joints.
     *
     * @param armature the armature to dump (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(Armature armature, String indent) {
        Validate.nonNull(armature, "armature");

        stream.print(indent);
        String description = describer.describe(armature);
        stream.println(description);

        Joint[] rootJoints = armature.getRoots();
        String moreIndent = indent + indentIncrement;
        for (Joint rootJoint : rootJoints) {
            dump(rootJoint, moreIndent);
        }

        stream.println();
        stream.flush();
    }

    /**
     * Dump the specified Bone, including its children.
     *
     * @param bone (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(Bone bone, String indent) {
        Validate.nonNull(bone, "bone");

        stream.print(indent);
        String description = describer.describe(bone);
        stream.println(description);

        List<Bone> children = bone.getChildren();
        String moreIndent = indent + indentIncrement;
        for (Bone childBone : children) {
            dump(childBone, moreIndent);
        }
    }

    /**
     * Dump the specified Camera.
     *
     * @param camera (not null, unaffected)
     */
    public void dump(Camera camera) {
        Validate.nonNull(camera, "camera");

        dump(camera, "");
        stream.flush();
    }

    /**
     * Dump the specified Camera, with indentation.
     *
     * @param camera (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(Camera camera, String indent) {
        Validate.nonNull(camera, "camera");

        stream.print(indent);
        String description = describer.describe(camera);
        stream.print(description);
        addLine(indent);
        String desc2 = describer.describeMore(camera);
        stream.print(' ');
        stream.print(desc2);
    }

    /**
     * Dump the specified Joint, including its children.
     *
     * @param joint (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(Joint joint, String indent) {
        Validate.nonNull(joint, "joint");

        stream.print(indent);
        String description = describer.describe(joint);
        stream.println(description);

        List<Joint> children = joint.getChildren();
        String moreIndent = indent + indentIncrement;
        for (Joint childJoint : children) {
            dump(childJoint, moreIndent);
        }
    }

    /**
     * Dump the specified Light, with indentation.
     *
     * @param light (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(Light light, String indent) {
        Validate.nonNull(light, "light");

        stream.print(indent);
        String description = describer.describe(light);
        stream.print(description);
    }

    /**
     * Dump the specified list of scenes.
     *
     * @param sceneList the root nodes of the scenes to dump (not null,
     * unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(List<Spatial> sceneList, String indent) {
        Validate.nonNull(indent, "indent");

        int numScenes = sceneList.size();
        if (numScenes == 0) {
            stream.print("no scenes");
        } else if (numScenes == 1) {
            stream.print("one scene:");
        } else {
            stream.printf("%d scenes:", numScenes);
        }
        stream.println();

        for (Spatial scene : sceneList) {
            dump(scene, indent + indentIncrement);
        }
    }

    /**
     * Dump the specified RenderManager.
     *
     * @param renderManager which RenderManager to dump (not null, unaffected)
     */
    public void dump(RenderManager renderManager) {
        String className = renderManager.getClass().getSimpleName();
        stream.println();
        stream.print(className);

        Renderer renderer = renderManager.getRenderer();
        className = renderer.getClass().getSimpleName();
        stream.print(" renderer=");
        stream.print(className);
        int aniso = renderer.getDefaultAnisotropicFilter();
        boolean atoc = renderer.getAlphaToCoverage();
        String atocString = atoc ? "" : "NO";
        stream.printf("[aniso=%d, %satoc]", aniso, atocString);

        List<ViewPort> pres = renderManager.getPreViews();
        int numPres = pres.size();
        List<ViewPort> mains = renderManager.getMainViews();
        int numMains = mains.size();
        List<ViewPort> posts = renderManager.getPostViews();
        int numPosts = posts.size();

        stream.printf(" with %d preView%s, %d mainView%s, and ",
                numPres, (numPres == 1) ? "" : "s",
                numMains, (numMains == 1) ? "" : "s");
        stream.printf("%d postView%s%n", numPosts, (numPosts == 1) ? "" : "s");

        // Dump all the viewports in order.
        for (int index = 0; index < numPres; ++index) {
            stream.printf("preView %d:%n", index);
            dump(pres.get(index), indentIncrement);
        }
        for (int index = 0; index < numMains; ++index) {
            stream.printf("mainView %d:%n", index);
            dump(mains.get(index), indentIncrement);
        }
        for (int index = 0; index < numPosts; ++index) {
            stream.printf("postView %d:%n", index);
            dump(posts.get(index), indentIncrement);
        }
    }

    /**
     * Dump the specified skeleton and all its bones.
     *
     * @param skeleton the skeleton to dump (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(Skeleton skeleton, String indent) {
        Validate.nonNull(skeleton, "skeleton");

        stream.print(indent);
        String description = describer.describe(skeleton);
        stream.print(description);
        stream.println(':');

        Bone[] rootBones = skeleton.getRoots();
        String moreIndent = indent + indentIncrement;
        for (Bone rootBone : rootBones) {
            dump(rootBone, moreIndent);
        }

        stream.println();
        stream.flush();
    }

    /**
     * Dump the specified subtree of a scene graph.
     *
     * @param spatial root of the subtree (may be null, unaffected)
     */
    public void dump(Spatial spatial) {
        dump(spatial, "");
        stream.flush();
    }

    /**
     * Dump the specified subtree of a scene graph. Note: recursive!
     *
     * @param spatial root of the subtree (may be null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(Spatial spatial, String indent) {
        Validate.nonNull(indent, "indent");

        if (spatial == null) {
            return;
        }
        stream.print(indent);

        int elementCount = spatial.getTriangleCount();
        stream.printf("%c[%d] ", describer.describeType(spatial), elementCount);

        String name = MyString.quoteName(spatial.getName());
        stream.print(name);

        if (indent.isEmpty() && spatial.getParent() != null) {
            stream.print(" (has parent)");
        }

        String description = describer.describeControls(spatial);
        addDescription(description);

        LightList lights = spatial.getLocalLightList(); // alias
        description = describer.describe(lights);
        addDescription(description);

        if (dumpTransformFlag) {
            description = describer.describeLocation(spatial);
            addDescription(description);

            description = describer.describeOrientation(spatial);
            addDescription(description);

            description = describer.describeScale(spatial);
            addDescription(description);
        }

        if (dumpBoundsFlag) {
            stream.print(" bound[");
            BoundingVolume worldBound = spatial.getWorldBound();
            String desc = describer.describe(worldBound);
            stream.print(desc);
            stream.print(']');
        }

        if (dumpUserFlag) {
            description = describer.describeUserData(spatial);
            addDescription(description);
        }

        if (spatial instanceof AudioNode) {
            description = describer.describeAudioNode(spatial);
            addDescription(description);

        } else {
            if (dumpBucketFlag) {
                description = describer.describeBucket(spatial);
                addDescription(description);
            }
            if (dumpShadowFlag) {
                description = describer.describeShadow(spatial);
                addDescription(description);
            }
            if (dumpCullFlag) {
                description = describer.describeCull(spatial);
                addDescription(description);
            }
            if (dumpOverrideFlag) {
                description = describer.describeOverrides(spatial);
                addDescription(description);
            }
        }

        if (spatial instanceof BitmapText) {
            stream.print(" text=");
            String text = ((BitmapText) spatial).getText();
            stream.print(MyString.quote(text));
        }

        if (spatial instanceof Geometry) {
            dumpGeometry((Geometry) spatial, indent);
        }
        stream.println();
        if (spatial instanceof Node) {
            dumpChildren((Node) spatial, indent + indentIncrement);
        }
    }

    /**
     * Dump the specified ViewPort.
     *
     * @param viewPort which ViewPort to dump (not null, unaffected)
     */
    public void dump(ViewPort viewPort) {
        Validate.nonNull(viewPort, "view port");

        dump(viewPort, "");
        stream.flush();
    }

    /**
     * Dump the specified ViewPort.
     *
     * @param viewPort which ViewPort to dump (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    public void dump(ViewPort viewPort, String indent) {
        Validate.nonNull(indent, "indent");

        String className = viewPort.getClass().getSimpleName();
        String name = viewPort.getName();
        stream.printf("%s%s %s ", indent, className, MyString.quoteName(name));
        if (viewPort.isEnabled()) {
            stream.print("enabled ");

            String desc = describer.describeFlags(viewPort);
            stream.print(desc);
            if (viewPort.isClearColor()) {
                ColorRGBA backColor = viewPort.getBackgroundColor();
                stream.printf(" bg(%s)", MyColor.describe(backColor));
            }
            stream.println();

            Camera camera = viewPort.getCamera();
            dump(camera, indent + "  ");

            List<SceneProcessor> processors = viewPort.getProcessors();
            dumpProcs(processors, indent);
            stream.print(" and ");
            List<Spatial> scenes = viewPort.getScenes();
            dump(scenes, indent);

        } else {
            stream.println("disabled");
        }
    }

    /**
     * Access the Describer used by this dumper.
     *
     * @return the pre-existing instance (not null)
     */
    public Describer getDescriber() {
        assert describer != null;
        return describer;
    }

    /**
     * Return the indent increment.
     *
     * @return (not null, may be empty)
     */
    public String indentIncrement() {
        assert indentIncrement != null;
        return indentIncrement;
    }

    /**
     * Test whether world bounds will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpBounds() {
        return dumpBoundsFlag;
    }

    /**
     * Test whether render-queue bucket assignments will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpBucket() {
        return dumpBucketFlag;
    }

    /**
     * Test whether cull hints will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpCull() {
        return dumpCullFlag;
    }

    /**
     * Test whether material parameters will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpMatParam() {
        return dumpMatParamFlag;
    }

    /**
     * Test whether material-parameter overrides will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpOverride() {
        return dumpOverrideFlag;
    }

    /**
     * Test whether shadow modes will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpShadow() {
        return dumpShadowFlag;
    }

    /**
     * Test whether location and scaling will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpTransform() {
        return dumpTransformFlag;
    }

    /**
     * Test whether user data will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpUser() {
        return dumpUserFlag;
    }

    /**
     * Test whether mesh-vertex data will be dumped.
     *
     * @return true if they'll be dumped, otherwise false
     */
    public boolean isDumpVertex() {
        return dumpVertexFlag;
    }

    /**
     * Return the maximum number of children per Node.
     *
     * @return the current limit (&ge;0, default=MAX_VALUE)
     */
    public int maxChildren() {
        assert maxChildren >= 0 : maxChildren;
        return maxChildren;
    }

    /**
     * Replace the current Describer with the specified one.
     *
     * @param newDescriber the desired Describer (not null, alias created)
     * @return this instance for chaining
     */
    public Dumper setDescriber(Describer newDescriber) {
        Validate.nonNull(newDescriber, "new describer");
        this.describer = newDescriber;
        return this;
    }

    /**
     * Configure dumping of world bounds.
     *
     * @param newValue true to enable, false to disable (default=false)
     * @return this instance for chaining
     */
    public Dumper setDumpBounds(boolean newValue) {
        this.dumpBoundsFlag = newValue;
        return this;
    }

    /**
     * Configure dumping of render-queue bucket assignments.
     *
     * @param newValue true to enable, false to disable (default=false)
     * @return this instance for chaining
     */
    public Dumper setDumpBucket(boolean newValue) {
        this.dumpBucketFlag = newValue;
        return this;
    }

    /**
     * Configure dumping of cull hints.
     *
     * @param newValue true to enable, false to disable (default=false)
     * @return this instance for chaining
     */
    public Dumper setDumpCull(boolean newValue) {
        this.dumpCullFlag = newValue;
        return this;
    }

    /**
     * Configure dumping of material parameters.
     *
     * @param newValue true to enable, false to disable (default=false)
     * @return this instance for chaining
     */
    public Dumper setDumpMatParam(boolean newValue) {
        this.dumpMatParamFlag = newValue;
        return this;
    }

    /**
     * Configure dumping of material-parameter overrides.
     *
     * @param newValue true to enable, false to disable (default=false)
     * @return this instance for chaining
     */
    public Dumper setDumpOverride(boolean newValue) {
        this.dumpOverrideFlag = newValue;
        return this;
    }

    /**
     * Configure dumping of shadow modes.
     *
     * @param newValue true to enable, false to disable (default=false)
     * @return this instance for chaining
     */
    public Dumper setDumpShadow(boolean newValue) {
        this.dumpShadowFlag = newValue;
        return this;
    }

    /**
     * Configure dumping of location and scaling.
     *
     * @param newValue true to enable, false to disable (default=false)
     * @return this instance for chaining
     */
    public Dumper setDumpTransform(boolean newValue) {
        this.dumpTransformFlag = newValue;
        return this;
    }

    /**
     * Configure dumping of user data.
     *
     * @param newValue true to enable, false to disable (default=true)
     * @return this instance for chaining
     */
    public Dumper setDumpUser(boolean newValue) {
        this.dumpUserFlag = newValue;
        return this;
    }

    /**
     * Configure dumping of mesh-vertex data.
     *
     * @param newValue true to enable, false to disable (default=false)
     * @return this instance for chaining
     */
    public Dumper setDumpVertex(boolean newValue) {
        this.dumpVertexFlag = newValue;
        return this;
    }

    /**
     * Configure the indent increment.
     *
     * @param newValue (not null, default=" ")
     * @return this instance for chaining
     */
    public Dumper setIndentIncrement(String newValue) {
        Validate.nonNull(newValue, "increment");
        this.indentIncrement = newValue;
        return this;
    }

    /**
     * Configure the maximum number of children per Node.
     *
     * @param newLimit the desired limit (&ge;0, default=MAX_VALUE)
     * @return this instance for chaining
     */
    public Dumper setMaxChildren(int newLimit) {
        Validate.nonNegative(newLimit, "newLimit");
        this.maxChildren = newLimit;
        return this;
    }
    // *************************************************************************
    // new protected methods

    /**
     * If the specified description is non-empty, print it to the stream,
     * prefixed by a blank.
     *
     * @param description (not null)
     */
    protected void addDescription(String description) {
        Validate.nonNull(description, "description");
        if (!description.isEmpty()) {
            stream.print(' ');
            stream.print(description);
        }
    }

    /**
     * Print a newline, followed by the specified indentation.
     *
     * @param indent (not null)
     */
    protected void addLine(String indent) {
        Validate.nonNull(indent, "indent");

        stream.println();
        stream.print(indent);
    }

    /**
     * Dump the specified AppState.
     *
     * @param appState the app state to dump (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    protected void dump(AppState appState, String indent) {
        Validate.nonNull(indent, "indent");

        String id = appState.getId();
        if (id == null) {
            id = appState.getClass().getSimpleName();
        } else {
            id = MyString.quote(id);
        }
        stream.print(id);

        if (appState.isEnabled()) {
            stream.print(" en");
        } else {
            stream.print(" dis");
        }
        stream.print("abled");

        if (appState instanceof NamedAppState) {
            NamedAppState namedAppState = (NamedAppState) appState;
            Collection<AppState> influences = namedAppState.getInfluence();
            if (!influences.isEmpty()) {
                stream.print("  influences[");
                for (AppState appState2 : influences) {
                    stream.print(' ');
                    String id2 = appState2.getId();
                    if (id2 == null) {
                        id2 = appState2.getClass().getSimpleName();
                    } else {
                        id2 = MyString.quote(id2);
                    }
                    stream.print(id2);
                }
                stream.print(" ]");
            }
        }
        addLine(indent);
    }
    // *************************************************************************
    // Cloneable methods

    /**
     * Create a deep copy of this Dumper.
     *
     * @return a new instance, equivalent to this one, with its own Describer
     * @throws CloneNotSupportedException if the superclass isn't cloneable
     */
    @Override
    public Dumper clone() throws CloneNotSupportedException {
        Dumper clone = (Dumper) super.clone();
        this.describer = describer.clone();
        // stream not cloned

        return clone;
    }
    // *************************************************************************
    // private methods

    /**
     * Dump the specified material-parameter map.
     *
     * @param map the map from names to parameters (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    private void dump(Map<String, MatParam> map, String indent) {
        if (!map.isEmpty()) {
            stream.print(':');

            // Loop through the parameter names in order.
            Set<String> names = new TreeSet<>(map.keySet());
            for (String name : names) {
                addLine(indent);
                MatParam matParam = map.get(name);
                String description = describer.describe(matParam);
                stream.print(description);
            }
        }
    }

    /**
     * Dump children of the specified scene-graph Node.
     *
     * @param node the Node to dump (not null, unaffected)
     * @param childIndent the indent text for children (not null, may be empty)
     */
    private void dumpChildren(Node node, String childIndent) {
        List<Spatial> children = node.getChildren();
        int numChildren = children.size();
        if (numChildren <= maxChildren) {
            // Dump all children.
            for (Spatial child : children) {
                dump(child, childIndent);
            }
        } else {
            // Dump the head and tail of the list, just the specified number.
            int numTail = maxChildren / 3;
            int numHead = maxChildren - numTail;
            for (int childI = 0; childI < numHead; ++childI) {
                Spatial child = children.get(childI);
                dump(child, childIndent);
            }
            int numSkipped = numChildren - numHead - numTail;
            stream.printf("%s... %d child spatial%s",
                    childIndent, numSkipped, (numSkipped == 1) ? "" : "s");
            stream.println(" skipped ...");
            for (int i = numChildren - numTail; i < numChildren; ++i) {
                Spatial child = children.get(i);
                dump(child, childIndent);
            }
        }
    }

    /**
     * Dump the material and mesh of the specified Geometry.
     *
     * @param geometry (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    private void dumpGeometry(Geometry geometry, String indent) {
        Material material = geometry.getMaterial();
        String moreIndent = indent + indentIncrement;
        String description = describer.describe(material);
        if (!description.isEmpty()) {
            addLine(indent);
            stream.print(" mat");
            stream.print(description);
            if (dumpMatParamFlag) {
                dump(material.getParamsMap(), moreIndent);
            }
        }

        Mesh mesh = geometry.getMesh();
        int numVertices = mesh.getVertexCount();
        description = describer.describe(mesh);
        addLine(indent);
        addDescription(description);

        if (dumpVertexFlag) {
            IndexBuffer indexBuffer = mesh.getIndexBuffer();
            Mesh.Mode mode = mesh.getMode();
            if (indexBuffer != null) {
                addLine(moreIndent);
                stream.print("index[");
                for (int ibPos = 0; ibPos < indexBuffer.size(); ++ibPos) {
                    if (ibPos > 0) {
                        if (mode == Mesh.Mode.Triangles && ibPos % 3 == 0
                                || mode == Mesh.Mode.Lines && ibPos % 2 == 0) {
                            stream.print('|');
                        } else {
                            stream.print(' ');
                        }
                    }
                    int index = indexBuffer.get(ibPos);
                    stream.print(index);
                }
                stream.print(']');
            }

            for (int vertexI = 0; vertexI < numVertices; ++vertexI) {
                addLine(moreIndent);
                description = describer.describeVertexData(mesh, vertexI);
                stream.print(description);
            }
        }

        MorphTarget[] targets = mesh.getMorphTargets();
        for (int targetIndex = 0; targetIndex < targets.length; ++targetIndex) {
            addLine(moreIndent);
            MorphTarget target = targets[targetIndex];
            String name = MyString.quoteName(target.getName());
            EnumMap<VertexBuffer.Type, FloatBuffer> bufferMap
                    = target.getBuffers();
            int numBuffers = bufferMap.size();
            stream.printf("target%d%s with %d buffer%s", targetIndex, name,
                    numBuffers, numBuffers == 1 ? "" : "s");

            if (dumpVertexFlag) {
                if (numVertices > 0) {
                    stream.print(':');
                }
                String mmIndent = moreIndent + indentIncrement;
                for (int vertexI = 0; vertexI < numVertices; ++vertexI) {
                    addLine(mmIndent);
                    description = describer.describeVertexData(mesh, vertexI);
                    stream.print(description);
                }
            }
        }
    }

    /**
     * Dump the specified list of scene processors.
     *
     * @param processors the list to dump (not null, unaffected)
     * @param indent the indent text (not null, may be empty)
     */
    private void dumpProcs(List<SceneProcessor> processors, String indent) {
        addLine(indent);
        stream.print(" with ");
        int numProcessors = processors.size();
        if (numProcessors == 0) {
            stream.print("no scene processors");

        } else {
            if (numProcessors == 1) {
                stream.print("one SceneProcessor:");
            } else {
                stream.printf("%d scene processors:", numProcessors);
            }

            for (SceneProcessor processor : processors) {
                addLine(indent + "  ");
                String desc = describer.describe(processor);
                stream.print(desc);
            }
            addLine(indent);
        }
    }
}
