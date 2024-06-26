/*
 Copyright (c) 2013-2024 Stephen Gold
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

import com.jme3.anim.Armature;
import com.jme3.anim.Joint;
import com.jme3.anim.util.HasLocalTransform;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.light.LightProbe;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamOverride;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.Control;
import com.jme3.scene.mesh.MorphTarget;
import com.jme3.shadow.AbstractShadowRenderer;
import com.jme3.shadow.CompareMode;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.shadow.PointLightShadowFilter;
import com.jme3.shadow.PointLightShadowRenderer;
import com.jme3.shadow.SpotLightShadowFilter;
import com.jme3.shadow.SpotLightShadowRenderer;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture3D;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.IntMap;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.MyControl;
import jme3utilities.MyLight;
import jme3utilities.MyMesh;
import jme3utilities.MySkeleton;
import jme3utilities.MySpatial;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.math.MyColor;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.MyVector3f;

/**
 * Generate compact textual descriptions of jME3 objects.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Describer implements Cloneable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Describer.class.getName());
    /**
     * separator between items in lists (not null, may be empty)
     */
    private String listSeparator = " ";
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Describer with the default separator.
     * <p>
     * This no-arg constructor was made explicit to avoid javadoc warnings from
     * JDK 18+.
     */
    public Describer() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Generate a compact, textual description of the specified Armature, not
     * including its joints.
     *
     * @param armature the Armature to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describe(Armature armature) {
        Validate.nonNull(armature, "armature");

        int numRoots = MySkeleton.countRootJoints(armature);
        int numJoints = armature.getJointCount();
        String result = String.format("Armature with %d root%s and %d joint%s:",
                numRoots, numRoots == 1 ? "" : "s",
                numJoints, numJoints == 1 ? "" : "s");

        return result;
    }

    /**
     * Generate a compact, textual description of the specified Bone, not
     * including its children.
     *
     * @param bone the Bone to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describe(Bone bone) {
        StringBuilder builder = new StringBuilder(30);
        String nameText = MyString.quote(bone.getName());
        builder.append(nameText);

        String flags = "";
        if (MySkeleton.getAttachments(bone) != null) {
            flags += 'A';
        }
        if (bone.hasUserControl()) {
            flags += 'U';
        }
        if (!flags.isEmpty()) {
            builder.append(' ');
            builder.append(flags);
        }

        List<Bone> children = bone.getChildren();
        if (!children.isEmpty()) {
            int numChildren = children.size();
            String childText = String.format(" with %d child%s:", numChildren,
                    (numChildren == 1 ? "" : "ren"));
            builder.append(childText);
        }

        return builder.toString();
    }

    /**
     * Generate a compact, textual description of the specified BoundingBox.
     *
     * @param aabb the box to describe (not null, unaffected)
     * @return description (not null, not empty)
     */
    public String describe(BoundingBox aabb) {
        StringBuilder builder = new StringBuilder(80);

        builder.append("loc[");
        Vector3f location = aabb.getCenter(); // alias
        String desc = MyVector3f.describe(location);
        builder.append(desc);
        builder.append(']');

        Vector3f he = aabb.getExtent(null);
        desc = describeHalfExtents(he);
        builder.append(desc);

        return builder.toString();
    }

    /**
     * Generate a compact, textual description of the specified BoundingSphere.
     *
     * @param sphere the sphere to describe (not null, unaffected)
     * @return description (not null, not empty)
     */
    public String describe(BoundingSphere sphere) {
        StringBuilder builder = new StringBuilder(80);

        builder.append("r=");
        float radius = sphere.getRadius();
        String desc = MyString.describe(radius);
        builder.append(desc);
        builder.append(' ');
        Vector3f location = sphere.getCenter(); // alias
        desc = MyVector3f.describe(location);
        builder.append(desc);

        return builder.toString();
    }

    /**
     * Generate a compact, textual description of the specified BoundingVolume.
     *
     * @param boundingVolume the sphere to describe (not null, unaffected)
     * @return description (not null, not empty)
     */
    public String describe(BoundingVolume boundingVolume) {
        String result;
        if (boundingVolume == null) {
            result = "null";
        } else if (boundingVolume instanceof BoundingSphere) {
            result = describe((BoundingSphere) boundingVolume);
        } else {
            BoundingBox aabb = (BoundingBox) boundingVolume;
            result = describe(aabb);
        }
        return result;
    }

    /**
     * Generate a compact, textual description of the specified Joint, not
     * including its children.
     *
     * @param joint the Joint to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describe(Joint joint) {
        StringBuilder builder = new StringBuilder(30);
        String nameText = MyString.quote(joint.getName());
        builder.append(nameText);

        if (MySkeleton.getAttachments(joint) != null) {
            builder.append(" A");
        }

        List<Joint> children = joint.getChildren();
        if (!children.isEmpty()) {
            int numChildren = children.size();
            String childText = String.format(" with %d child%s:", numChildren,
                    (numChildren == 1 ? "" : "ren"));
            builder.append(childText);
        }

        return builder.toString();
    }

    /**
     * Generate a compact, textual description of the specified Material, not
     * including its parameters.
     *
     * @param material the Material to describe (may be null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describe(Material material) {
        if (material == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(80);

        String name = material.getName();
        result.append(MyString.quoteName(name));

        result.append(" def");
        MaterialDef def = material.getMaterialDef();
        String defName = (def == null) ? null : def.getName();
        String description = MyString.quote(defName);
        result.append(description);

        result.append(' ');
        RenderState state = material.getAdditionalRenderState();
        description = describe(state);
        result.append(description);

        Collection<MatParam> params = material.getParams();
        int numParams = params.size();
        String count = String.format(
                " with %d parm%s", numParams, (numParams == 1) ? "" : "s");
        result.append(count);

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the specified material
     * parameter.
     *
     * @param matParam the material parameter to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describe(MatParam matParam) {
        StringBuilder result = new StringBuilder(80);
        result.append(' ');
        String paramName = matParam.getName();
        result.append(paramName);
        result.append(": ");

        String valueString;
        Object obj = matParam.getValue();
        if (obj instanceof ColorRGBA) {
            ColorRGBA color = (ColorRGBA) obj;
            valueString = MyColor.describe(color);
        } else if (obj instanceof Float) {
            float value = (Float) obj;
            valueString = MyString.describe(value);
        } else if (obj instanceof Texture) {
            Texture texture = (Texture) obj;
            valueString = describe(texture);
        } else {
            valueString = matParam.getValueAsString();
        }
        result.append(valueString);

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the specified Mesh.
     *
     * @param mesh the Mesh to describe (may be null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describe(Mesh mesh) {
        StringBuilder result = new StringBuilder(80);
        boolean addSeparators = false;

        String name = mesh.getClass().getSimpleName();
        result.append(name);

        result.append(" mode=");
        Mesh.Mode mode = mesh.getMode();
        result.append(mode);

        result.append(" numV=");
        int numV = mesh.getVertexCount();
        result.append(numV);

        result.append(" bufs[");
        IntMap<VertexBuffer> buffers = mesh.getBuffers();
        for (IntMap.Entry<VertexBuffer> bufferEntry : buffers) {
            if (addSeparators) {
                result.append(listSeparator);
            } else {
                addSeparators = true;
            }
            VertexBuffer buffer = bufferEntry.getValue();
            String desc = describe(buffer);
            result.append(desc);
        }
        result.append(']');

        MorphTarget[] targets = mesh.getMorphTargets();
        int numTargets = targets.length;
        if (numTargets > 0) {
            result.append(" with ");
            result.append(numTargets);
            result.append(" target");
            if (numTargets > 1) {
                result.append('s');
            }
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the specified Skeleton, not
     * including its bones.
     *
     * @param skeleton the Skeleton to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describe(Skeleton skeleton) {
        int numRoots = MySkeleton.countRootBones(skeleton);
        int numBones = skeleton.getBoneCount();
        String result = String.format("Skeleton with %d root%s and %d bone%s:",
                numRoots, numRoots == 1 ? "" : "s",
                numBones, numBones == 1 ? "" : "s");

        return result;
    }

    /**
     * Generate a compact, textual description of the specified Texture.
     *
     * @param texture the Texture to describe (may be null, unaffected)
     * @return a textual description (not null, not empty)
     */
    public String describe(Texture texture) {
        String result = "null";

        if (texture != null) {
            TextureKey textureKey = (TextureKey) texture.getKey();
            if (textureKey == null) {
                result = "(no key)";
            } else {
                result = describe(textureKey);
            }

            Texture.MagFilter mag = texture.getMagFilter();
            result += " mag:" + mag.toString();
            Texture.MinFilter min = texture.getMinFilter();
            result += " min:" + min.toString();
            int aniso = texture.getAnisotropicFilter();
            result += " aniso:" + aniso;
            if (texture instanceof Texture3D
                    || texture instanceof TextureCubeMap) {
                Texture.WrapMode rWrap = texture.getWrap(Texture.WrapAxis.R);
                result += " r:" + rWrap.toString();
            }
            Texture.WrapMode sWrap = texture.getWrap(Texture.WrapAxis.S);
            result += " s:" + sWrap.toString();
            Texture.WrapMode tWrap = texture.getWrap(Texture.WrapAxis.T);
            result += " t:" + tWrap.toString();
        }

        assert result != null;
        assert !result.isEmpty();
        return result;
    }

    /**
     * Generate a compact, textual description of the specified VertexBuffer.
     *
     * @param buffer the buffer to describe (not null, unaffected)
     * @return a textual description (not null, not empty)
     */
    public String describe(VertexBuffer buffer) {
        VertexBuffer.Format format = buffer.getFormat();
        String formatString;
        if (format == null) {
            formatString = "nofmt";
        } else {
            formatString = format.toString();
            formatString = formatString.toLowerCase(Locale.ROOT);
            formatString = formatString.replace("float", "f");
            formatString = formatString.replace("unsigned", "u");
        }
        int numComponents = buffer.getNumComponents();
        VertexBuffer.Type type = buffer.getBufferType();
        String result = type + "%" + numComponents + formatString;

        return result;
    }

    /**
     * Generate a compact, textual description of an AudioNode.
     *
     * @param spatial the AudioNode to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describeAudioNode(Spatial spatial) {
        AudioNode audioNode = (AudioNode) spatial;
        StringBuilder result = new StringBuilder(80);

        AudioSource.Status status = audioNode.getStatus();
        result.append(status);

        if (status == AudioSource.Status.Playing) {
            result.append(",");

            AudioData.DataType type = audioNode.getAudioData().getDataType();
            result.append(type);
            result.append(",");

            boolean isDirectional = audioNode.isDirectional();
            if (!isDirectional) {
                result.append("NOT");
            }
            result.append("directional,");

            boolean isLooping = audioNode.isLooping();
            if (!isLooping) {
                result.append("NOT");
            }
            result.append("looping,");

            result.append("pitch=");
            float pitch = audioNode.getPitch();
            result.append(MyString.describe(pitch));

            result.append(",channel=");
            int channel = audioNode.getChannel();
            result.append(channel);
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the render-queue bucket to
     * which the specified Spatial is assigned.
     *
     * @param spatial the Spatial to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describeBucket(Spatial spatial) {
        StringBuilder result = new StringBuilder(20);

        // Describe its local assignment.
        result.append("bucket=");
        RenderQueue.Bucket bucket = spatial.getLocalQueueBucket();
        result.append(bucket);
        if (bucket == RenderQueue.Bucket.Inherit) {
            // Describe its effective assignment.
            result.append('/');
            bucket = spatial.getQueueBucket();
            result.append(bucket);
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of all controls added to the
     * specified Spatial.
     *
     * @param spatial the Spatial to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describeControls(Spatial spatial) {
        Validate.nonNull(spatial, "spatial");
        StringBuilder result = new StringBuilder(50);

        int count = spatial.getNumControls();
        boolean addSeparators = false;
        for (int controlI = 0; controlI < count; ++controlI) {
            if (addSeparators) {
                result.append(listSeparator);
            } else {
                addSeparators = true;
            }

            Control control = spatial.getControl(controlI);
            boolean isEnabled = isControlEnabled(control);
            if (isEnabled) {
                String description = describe(control);
                result.append(description);
            }
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the view-frustum culling hints
     * of the specified Spatial.
     *
     * @param spatial the Spatial to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describeCull(Spatial spatial) {
        StringBuilder result = new StringBuilder(20);

        // Describe its local cull hint.
        result.append("cull=");
        Spatial.CullHint mode = spatial.getLocalCullHint();
        result.append(mode);
        if (mode == Spatial.CullHint.Inherit) {
            // Describe its effective cull hint.
            result.append('/');
            mode = spatial.getCullHint();
            result.append(mode);
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the filters in the specified
     * FilterPostProcessor.
     *
     * @param fpp the processor to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describeFilters(FilterPostProcessor fpp) {
        StringBuilder result = new StringBuilder(20);
        boolean addSeparators = false;

        List<Filter> list = fpp.getFilterList();
        for (Filter filter : list) {
            if (addSeparators) {
                result.append(listSeparator);
            } else {
                addSeparators = true;
            }
            String description = describe(filter);
            result.append(description);
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the flags of the specified
     * ViewPort.
     *
     * @param viewPort the ViewPort to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describeFlags(ViewPort viewPort) {
        StringBuilder result = new StringBuilder(32);

        if (!viewPort.isClearColor()) {
            result.append("NO");
        }
        result.append("clColor,");

        if (!viewPort.isClearDepth()) {
            result.append("NO");
        }
        result.append("clDepth,");

        if (!viewPort.isClearStencil()) {
            result.append("NO");
        }
        result.append("clStencil");

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the specified single-precision
     * floating-point values.
     *
     * @param data the values to describe (not null)
     * @return a description (not null, not empty)
     */
    public String describeFloats(float... data) {
        StringBuilder result = new StringBuilder(170);

        result.append('(');
        result.append(data.length);
        result.append(')');

        for (float value : data) {
            if (result.length() > 150) {
                result.append(" ...");
                break;
            }
            result.append(listSeparator);
            String description = MyString.describe(value);
            result.append(description);
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the world location of the
     * specified Spatial.
     *
     * @param spatial the Spatial to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describeLocation(Spatial spatial) {
        Validate.nonNull(spatial, "spatial");
        StringBuilder result = new StringBuilder(30);

        Vector3f location = MySpatial.worldLocation(spatial, null);
        if (!MyVector3f.isZero(location)) {
            result.append("loc[");
            String locText = MyVector3f.describe(location);
            result.append(locText);
            result.append(']');
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the world orientation of the
     * specified Spatial.
     *
     * @param spatial the Spatial to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describeOrientation(Spatial spatial) {
        Validate.nonNull(spatial, "spatial");
        StringBuilder result = new StringBuilder(30);

        Quaternion orientation = MySpatial.worldOrientation(spatial, null);
        if (!MyQuaternion.isRotationIdentity(orientation)) {
            result.append("orient[");
            String orientText = MyQuaternion.describe(orientation);
            result.append(orientText);
            result.append(']');
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the material-parameter
     * overrides of the specified Spatial.
     *
     * @param spatial the Spatial to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describeOverrides(Spatial spatial) {
        Validate.nonNull(spatial, "spatial");
        StringBuilder result = new StringBuilder(60);

        result.append("mpo[");
        boolean addSeparators = false;

        List<MatParamOverride> list
                = spatial.getLocalMatParamOverrides(); // alias
        for (MatParamOverride override : list) {
            if (addSeparators) {
                result.append(listSeparator);
            } else {
                addSeparators = true;
            }
            String description = describe(override);
            result.append(description);
        }
        result.append(']');

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the scene processors of the
     * specified ViewPort.
     *
     * @param viewPort the ViewPort to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describeProcessors(ViewPort viewPort) {
        StringBuilder result = new StringBuilder(20);
        boolean addSeparators = false;

        List<SceneProcessor> pList = viewPort.getProcessors();
        for (SceneProcessor processor : pList) {
            if (addSeparators) {
                result.append(listSeparator);
            } else {
                addSeparators = true;
            }
            String description = describe(processor);
            result.append(description);
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the world scale of the
     * specified Spatial.
     *
     * @param spatial the Spatial to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describeScale(Spatial spatial) {
        Validate.nonNull(spatial, "spatial");

        Vector3f worldScale = MySpatial.worldScale(spatial, null);
        String result = describeScale(worldScale);

        return result;
    }

    /**
     * Generate a compact, textual description of the specified scale vector.
     *
     * @param vector the vector to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describeScale(Vector3f vector) {
        Validate.nonNull(vector, "vector");
        StringBuilder result = new StringBuilder(30);

        if (!MyVector3f.isScaleIdentity(vector)) {
            result.append("scale[");
            String vectorText = MyVector3f.describe(vector);
            result.append(vectorText);
            result.append(']');
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the shadow modes of the
     * specified Spatial.
     *
     * @param spatial the spatial to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public String describeShadow(Spatial spatial) {
        StringBuilder result = new StringBuilder(20);

        // Describe its local shadow mode.
        result.append("shadow=");
        RenderQueue.ShadowMode mode = spatial.getLocalShadowMode();
        result.append(mode);
        if (mode == RenderQueue.ShadowMode.Inherit) {
            // Describe its effective shadow mode.
            result.append('/');
            mode = spatial.getShadowMode();
            result.append(mode);
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the specified animation track
     * target.
     *
     * @param target the target to describe (may be null, unaffected)
     * @return a string of descriptive text (not null, not empty)
     */
    public String describeTrackTarget(HasLocalTransform target) {
        if (target == null) {
            return "null";
        }

        String targetName;
        if (target instanceof Spatial) {
            targetName = ((Spatial) target).getName();
        } else {
            targetName = ((Joint) target).getName();
        }

        String className = target.getClass().getSimpleName();
        String qTargetName = MyString.quoteName(targetName);
        String result
                = String.format(" targeting %s%s:", className, qTargetName);

        return result;
    }

    /**
     * Generate a compact, textual description of any user data in the specified
     * Spatial.
     *
     * @param spatial the Spatial to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    public String describeUserData(Spatial spatial) {
        StringBuilder result = new StringBuilder(50);
        boolean addSeparators = false;

        Collection<String> keys = spatial.getUserDataKeys();
        for (String key : keys) {
            if (addSeparators) {
                result.append(' ');
            } else {
                addSeparators = true;
            }
            result.append(key);
            result.append('=');
            Object value = spatial.getUserData(key);

            String valueString;
            if (value == null || value instanceof String) {
                valueString = MyString.quote((String) value);
            } else {
                valueString = value.toString();
            }
            result.append(valueString);
        }

        return result.toString();
    }

    /**
     * Generate a compact, textual description of the indexed vertex in the
     * specified Mesh.
     *
     * @param mesh (not null, unaffected)
     * @param vertexIndex (&ge;0)
     * @return a description (not null, not empty)
     */
    public String describeVertexData(Mesh mesh, int vertexIndex) {
        StringBuilder builder = new StringBuilder(80);
        String format;
        int numVertices = mesh.getVertexCount();
        if (numVertices <= 10) {
            format = "v%1d: ";
        } else if (numVertices <= 100) {
            format = "v%02d: ";
        } else if (numVertices <= 1000) {
            format = "v%03d: ";
        } else {
            format = "v%04d: ";
        }
        String desc = String.format(Locale.ROOT, format, vertexIndex);
        builder.append(desc);

        int length = builder.length();
        Vector3f pos = MyMesh.vertexVector3f(
                mesh, VertexBuffer.Type.Position, vertexIndex, null);
        desc = MyVector3f.describe(pos);
        builder.append(desc);
        int alignLength = length + 37;

        if (MyMesh.hasUV(mesh)) {
            length = builder.length();
            if (length < alignLength) {
                builder.append(MyString.repeat(" ", alignLength - length));
            }

            Vector2f norm = MyMesh.vertexVector2f(
                    mesh, VertexBuffer.Type.TexCoord, vertexIndex, null);
            builder.append(" u=");
            builder.append(norm.x);
            builder.append(" v=");
            builder.append(norm.y);
            alignLength += 26;
        }

        if (mesh.getBuffer(VertexBuffer.Type.TexCoord2) != null) {
            length = builder.length();
            if (length < alignLength) {
                builder.append(MyString.repeat(" ", alignLength - length));
            }

            Vector2f norm = MyMesh.vertexVector2f(
                    mesh, VertexBuffer.Type.TexCoord2, vertexIndex, null);
            builder.append(" u2=");
            builder.append(norm.x);
            builder.append(" v2=");
            builder.append(norm.y);
            alignLength += 28;
        }

        if (MyMesh.hasNormals(mesh)) {
            length = builder.length();
            if (length < alignLength) {
                builder.append(MyString.repeat(" ", alignLength - length));
            }

            builder.append(" N[");
            Vector3f norm = MyMesh.vertexVector3f(
                    mesh, VertexBuffer.Type.Normal, vertexIndex, null);
            desc = MyVector3f.describeDirection(norm);
            builder.append(desc);
            builder.append(']');
            alignLength += 32;
        }

        if (mesh.getBuffer(VertexBuffer.Type.Color) != null) {
            length = builder.length();
            if (length < alignLength) {
                builder.append(MyString.repeat(" ", alignLength - length));
            }

            builder.append(' ');
            ColorRGBA color = MyMesh.vertexColor(mesh, vertexIndex, null);
            desc = MyColor.describe(color);
            builder.append(desc);
            alignLength += 24;
        }

        if (mesh.getBuffer(VertexBuffer.Type.Tangent) != null) {
            length = builder.length();
            if (length < alignLength) {
                builder.append(MyString.repeat(" ", alignLength - length));
            }

            builder.append(" T");
            Vector4f tangent = MyMesh.vertexVector4f(
                    mesh, VertexBuffer.Type.Tangent, vertexIndex, null);
            builder.append(tangent);
        }

        return builder.toString();
    }

    /**
     * Return the list separator.
     *
     * @return separator text string (not null, may be empty, default=" ")
     */
    public String listSeparator() {
        assert listSeparator != null;
        return listSeparator;
    }

    /**
     * Alter the list separator.
     *
     * @param newSeparator (not null, may be empty)
     */
    public void setListSeparator(String newSeparator) {
        Validate.nonNull(newSeparator, "new separator");
        this.listSeparator = newSeparator;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Generate a textual description of a camera.
     *
     * @param camera the camera to describe (may be null, unaffected)
     * @return a description (not null, not empty)
     * @see #describeMore(com.jme3.renderer.Camera)
     */
    protected String describe(Camera camera) {
        String result = MyCamera.describe(camera);
        return result;
    }

    /**
     * Generate a textual description of a scene-graph control.
     *
     * @param control the control to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    protected String describe(Control control) {
        Validate.nonNull(control, "control");
        String result = MyControl.describe(control);
        return result;
    }

    /**
     * Generate a textual description of a filter.
     *
     * @param filter the filter to describe (unaffected)
     * @return a description (not null, not empty)
     */
    protected String describe(Filter filter) {
        String result;
        if (filter instanceof DirectionalLightShadowFilter) {
            result = "DShadow";
        } else if (filter instanceof PointLightShadowFilter) {
            result = "PShadow";
        } else if (filter instanceof SpotLightShadowFilter) {
            result = "SShadow";
        } else if (filter == null) {
            result = "null";
        } else {
            result = filter.getClass().getSimpleName();
            result = result.replace("Filter", "");
            if (result.isEmpty()) {
                result = "?";
            }
        }

        return result;
    }

    /**
     * Generate a brief textual description of a light.
     *
     * @param light the light to describe (unaffected)
     * @return a description (not null, not empty)
     */
    protected String describe(Light light) {
        String result;
        if (light == null) {
            result = "null";
        } else {
            String quotedName = MyString.quoteName(light.getName());
            String typeName = MyLight.describeType(light) + quotedName;
            ColorRGBA color = light.getColor();
            String rgb = MyColor.describe(color);
            if (light instanceof AmbientLight) {
                result = String.format("%s(%s)", typeName, rgb);

            } else if (light instanceof DirectionalLight) {
                Vector3f direction = ((DirectionalLight) light).getDirection();
                String dir = MyVector3f.describeDirection(direction);
                result = String.format("%s(%s; %s)", typeName, rgb, dir);

            } else if (light instanceof LightProbe) {
                LightProbe probe = (LightProbe) light;
                if (probe.isReady()) {
                    LightProbe.AreaType areaType = probe.getAreaType();
                    float radius = probe.getArea().getRadius();
                    String r = MyString.describe(radius);
                    Vector3f location = probe.getPosition(); // alias
                    String loc = MyVector3f.describe(location);
                    result = String.format(
                            "%s(%s r=%s %s)", typeName, areaType, r, loc);
                } else {
                    result = String.format("%s(unready)", typeName);
                }

            } else if (light instanceof PointLight) {
                Vector3f location = ((PointLight) light).getPosition();
                String loc = MyVector3f.describe(location);
                result = String.format("%s(%s; %s)", typeName, rgb, loc);

            } else if (light instanceof SpotLight) {
                SpotLight spotLight = (SpotLight) light;
                Vector3f location = spotLight.getPosition();
                String loc = MyVector3f.describe(location);
                Vector3f direction = spotLight.getDirection();
                String dir = MyVector3f.describeDirection(direction);
                result = String.format(
                        "%s(%s; %s; %s)", typeName, rgb, loc, dir);

            } else {
                result = String.format("%s(%s)", typeName, rgb);
            }

            if (!light.isEnabled()) {
                result += "DISABLED";
            }
        }

        return result;
    }

    /**
     * Generate a textual description of a light list.
     *
     * @param lightList the list to describe (not null, unaffected)
     * @return a description (not null, may be empty)
     */
    protected String describe(LightList lightList) {
        StringBuilder result = new StringBuilder(50);
        boolean addSeparators = false;

        for (Light light : lightList) {
            if (addSeparators) {
                result.append(listSeparator);
            } else {
                addSeparators = true;
            }
            String description = describe(light);
            result.append(description);
        }

        return result.toString();
    }

    /**
     * Generate a textual description of a material-parameter override.
     *
     * @param override the override to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    protected String describe(MatParamOverride override) {
        StringBuilder result = new StringBuilder(50);

        String name = override.getName();
        result.append(name);

        Object value = override.getValue();
        if (value == null) {
            result.append("=null");
        } else {
            String valueString = value.toString();
            if (valueString.length() <= 8) {
                result.append('=');
                result.append(valueString);
            }
        }
        if (!override.isEnabled()) {
            result.append("!DISABLED");
        }

        return result.toString();
    }

    /**
     * Generate a textual description of a RenderState.
     *
     * @param state the RenderState to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    protected String describe(RenderState state) {
        StringBuilder result = new StringBuilder(30);

        if (!state.isDepthTest()) {
            result.append("NO");
        }
        result.append("dTest,");

        if (!state.isDepthWrite()) {
            result.append("NO");
        }
        result.append("dWrite,");

        if (!state.isWireframe()) {
            result.append("NO");
        }
        result.append("wireframe");

        RenderState.FaceCullMode faceCullMode = state.getFaceCullMode();
        if (faceCullMode != RenderState.FaceCullMode.Back) {
            result.append(",faceCull=");
            result.append(faceCullMode);
        }

        result.append(",blend=");
        RenderState.BlendMode blendMode = state.getBlendMode();
        result.append(blendMode);

        if (blendMode == RenderState.BlendMode.Custom) {
            RenderState.BlendEquation equation = state.getBlendEquation();
            result.append("[eq=").append(equation);

            RenderState.BlendEquationAlpha eqA = state.getBlendEquationAlpha();
            result.append(",eqA=").append(eqA);

            RenderState.BlendFunc dAlpha = state.getCustomDfactorAlpha();
            result.append(",dAlpha=").append(dAlpha);

            RenderState.BlendFunc dRgb = state.getCustomDfactorRGB();
            result.append(",dRGB=").append(dRgb);

            RenderState.BlendFunc sAlpha = state.getCustomSfactorAlpha();
            result.append(",sAlpha=").append(sAlpha);

            RenderState.BlendFunc sRgb = state.getCustomSfactorRGB();
            result.append(",sRGB=").append(sRgb).append(']');
        }

        return result.toString();
    }

    /**
     * Generate a textual description of a SceneProcessor.
     *
     * @param processor the processor to describe (may be null, unaffected)
     * @return a description (not null, not empty)
     */
    protected String describe(SceneProcessor processor) {
        String result;
        if (processor instanceof DirectionalLightShadowRenderer) {
            result = "DirShadow" + describeShadowRenderer(processor);
        } else if (processor instanceof FilterPostProcessor) {
            FilterPostProcessor fpp = (FilterPostProcessor) processor;
            String desc = describeFilters(fpp);
            result = String.format("filters[%s]", desc);
        } else if (processor instanceof PointLightShadowRenderer) {
            result = "PointShadow" + describeShadowRenderer(processor);
        } else if (processor instanceof ScreenshotAppState) {
            result = "Screenshot";
        } else if (processor instanceof SpotLightShadowRenderer) {
            result = "SpotShadow" + describeShadowRenderer(processor);
        } else if (processor == null) {
            result = "null";
        } else {
            result = processor.getClass().getSimpleName();
            result = result.replace("Processor", "");
            if (result.isEmpty()) {
                result = "?";
            }
        }

        return result;
    }

    /**
     * Describe a TextureKey.
     *
     * @param textureKey (not null, unaffected)
     * @return a textual description (not null, not empty)
     */
    protected String describe(TextureKey textureKey) {
        String result = textureKey.toString();

        int anisotropy = textureKey.getAnisotropy();
        if (anisotropy != 0) {
            result += String.format(" (Anisotropy%d)", anisotropy);
        }

        assert result != null;
        assert !result.isEmpty();
        return result;
    }

    /**
     * Describe the specified half extents.
     *
     * @param he the half extent for each axis (not null, unaffected)
     * @return a bracketed description (not null, not empty)
     */
    protected String describeHalfExtents(Vector3f he) {
        String desc = MyVector3f.describe(he);
        String result = String.format(" he[%s]", desc);

        return result;
    }

    /**
     * Generate additional textual description of a Camera.
     *
     * @param camera the camera to describe (not null, unaffected)
     * @return a description (not null, not empty)
     * @see #describe(com.jme3.renderer.Camera)
     */
    protected String describeMore(Camera camera) {
        Validate.nonNull(camera, "camera");
        String result = MyCamera.describeMore(camera);
        return result;
    }

    /**
     * Generate a single-character description of the specified Spatial.
     *
     * @param spatial the spatial to describe (unaffected, may be null)
     * @return a mnemonic character
     */
    protected char describeType(Spatial spatial) {
        char result = MySpatial.describeType(spatial);
        return result;
    }

    /**
     * Test whether the specified scene-graph control is enabled.
     *
     * @param control the control to test (not null, unaffected)
     * @return true if the control is enabled, otherwise false
     */
    protected boolean isControlEnabled(Control control) {
        Validate.nonNull(control, "control");

        boolean result = !MyControl.canDisable(control)
                || MyControl.isEnabled(control);

        return result;
    }
    // *************************************************************************
    // Cloneable methods

    /**
     * Create a copy of this Describer.
     *
     * @return a new instance, equivalent to this one
     * @throws CloneNotSupportedException if the superclass isn't cloneable
     */
    @Override
    public Describer clone() throws CloneNotSupportedException {
        Describer clone = (Describer) super.clone();
        return clone;
    }
    // *************************************************************************
    // private methods

    /**
     * Generate a compact textual description of a shadow renderer, enclosed in
     * square brackets.
     *
     * @param processor the renderer to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    private String describeShadowRenderer(SceneProcessor processor) {
        StringBuilder result = new StringBuilder(20);
        AbstractShadowRenderer renderer = (AbstractShadowRenderer) processor;

        result.append("[inten=");
        float inten = renderer.getShadowIntensity();
        result.append(MyString.describeFraction(inten));

        result.append(" size=");
        int size = renderer.getShadowMapSize();
        result.append(size);

        result.append(" maps=");
        int maps = renderer.getNumShadowMaps();
        result.append(maps);

        result.append(" edge[");
        EdgeFilteringMode mode = renderer.getEdgeFilteringMode();
        result.append(mode);

        result.append(" thk=");
        int thk = renderer.getEdgesThickness();
        result.append(thk);

        result.append("] cmp=");
        CompareMode cmp = renderer.getShadowCompareMode();
        result.append(cmp);

        result.append(" zExt=");
        float zExt = renderer.getShadowZExtend();
        result.append(MyString.describe(zExt));

        result.append(" zFade=");
        float zFade = renderer.getShadowZFadeLength();
        result.append(MyString.describe(zFade));

        boolean backFaces = renderer.isRenderBackFacesShadows();
        result.append(backFaces ? " backFaces]" : "noBackFaces]");

        return result.toString();
    }
}
