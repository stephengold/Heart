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
package jme3utilities;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.AnimLayer;
import com.jme3.anim.MorphControl;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.action.Action;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.light.Light;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.control.LightControl;
import com.jme3.util.SafeArrayList;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Utility methods that operate on jME3 scene-graph controls in general. If
 * physics controls might be present, use the corresponding methods in Minie's
 * {@code MyControlP} class instead.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class MyControl {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MyControl.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private MyControl() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Check whether a scene-graph control implements isEnabled() and
     * setEnabled().
     *
     * @param sgc control to test (may be null, unaffected)
     * @return true if it's implemented, otherwise false
     */
    public static boolean canDisable(Control sgc) {
        boolean result = sgc instanceof AbstractControl;
        if (!result) {
            /*
             * Handle ChaseCamera, MotionEvent, ParticleEmitterControl,
             * StatsView, and custom controls.
             */
            Class<? extends Control> sgcClass = sgc.getClass();
            try {
                sgcClass.getMethod("isEnabled");
                result = true;
            } catch (NoSuchMethodException exception) {
                String message = sgcClass.getName();
                throw new IllegalArgumentException(message);
            }
        }

        return result;
    }

    /**
     * Generate a textual description of the specified scene-graph control.
     *
     * @param control the instance to describe (not null, unaffected)
     * @return a description (not null, not empty)
     */
    public static String describe(Control control) {
        StringBuilder result = new StringBuilder(80);
        String typeString = describeType(control);
        result.append(typeString);

        if (control instanceof AnimComposer) {
            AnimComposer composer = (AnimComposer) control;

            result.append("[clips=");
            Collection<String> nameCollection = composer.getAnimClipsNames();
            int numClips = nameCollection.size();
            result.append(numClips);

            float gSpeed = composer.getGlobalSpeed();
            if (gSpeed != 1f) {
                result.append(" gSpeed=");
                result.append(MyString.describe(gSpeed));
            }

            result.append(" layers");
            nameCollection = composer.getLayerNames();
            int numLayers = nameCollection.size();
            if (numLayers == 1) {
                String name = Heart.first(nameCollection);
                AnimLayer layer = composer.getLayer(name);
                result.append("[t=");
                double animTime = layer.getTime();
                result.append(MyString.describeFraction((float) animTime));

                result.append(" action=");
                Action action = layer.getCurrentAction();
                if (action == null) {
                    result.append("null");
                } else {
                    result.append(action.getClass().getSimpleName());
                    result.append(MyString.quote(action.toString()));
                }
                result.append(']');
            } else {
                result.append('=');
                result.append(numLayers);
            }
            result.append(']');

        } else if (control instanceof AnimControl) {
            AnimControl animControl = (AnimControl) control;
            result.append('[');
            Collection<String> nameCollection = animControl.getAnimationNames();
            int numAnimations = nameCollection.size();
            if (numAnimations > 2) {
                result.append(numAnimations);
            } else {
                boolean isFirst = true;
                for (String animationName : nameCollection) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        result.append(',');
                    }
                    Animation animation = animControl.getAnim(animationName);
                    String desc = MyAnimation.describe(animation, animControl);
                    result.append(desc);
                }
            }
            result.append(']');

        } else if (control instanceof CameraControl) {
            CameraControl cameraControl = (CameraControl) control;
            CameraControl.ControlDirection direction
                    = cameraControl.getControlDir();
            Camera camera = cameraControl.getCamera();
            String name = (camera == null) ? null : camera.getName();
            String quotedName = MyString.quoteName(name);

            result.append('[');
            result.append(direction);
            result.append(" camera");
            result.append(quotedName);
            result.append(']');

        } else if (control instanceof LightControl) {
            LightControl lightControl = (LightControl) control;
            LightControl.ControlDirection direction
                    = lightControl.getControlDir();

            Light light = lightControl.getLight();
            String typeName;
            if (light == null) {
                typeName = "null";
            } else {
                String quotedName = MyString.quoteName(light.getName());
                typeName = MyLight.describeType(light) + quotedName;
            }

            result.append('[');
            result.append(direction);
            result.append(' ');
            result.append(typeName);
            result.append(']');

        } else if (control instanceof MorphControl) {
            MorphControl morph = (MorphControl) control;
            boolean approx = morph.isApproximateTangents();
            result.append(approx ? "[approx]" : "[vbuf]");

        } else if (control instanceof SkeletonControl) {
            SkeletonControl skeletonControl = (SkeletonControl) control;
            result.append('[');
            int boneCount = skeletonControl.getSkeleton().getBoneCount();
            result.append(boneCount);
            boolean useHw = skeletonControl.isHardwareSkinningUsed();
            result.append(useHw ? " hw]" : " sw]");

        } else if (control instanceof SkinningControl) {
            SkinningControl skinningControl = (SkinningControl) control;
            result.append('[');
            int jointCount = skinningControl.getArmature().getJointCount();
            result.append(jointCount);
            boolean useHw = skinningControl.isHardwareSkinningUsed();
            result.append(useHw ? " hw]" : " sw]");
        }

        return result.toString();
    }

    /**
     * Describe the type of a scene-graph control.
     *
     * @param control instance to describe (not null, unaffected)
     * @return description (not null)
     */
    public static String describeType(Control control) {
        String description = control.getClass().getSimpleName();
        if (description.endsWith("Control")) {
            description = MyString.removeSuffix(description, "Control");
        }

        return description;
    }

    /**
     * Find the index of the specified scene-graph control in the specified
     * spatial.
     *
     * @param sgc scene-graph control to find (not null, unaffected)
     * @param spatial where the control was added (not null, unaffected)
     * @return index (&ge;0) or -1 if not found
     */
    public static int findIndex(Control sgc, Spatial spatial) {
        Validate.nonNull(sgc, "control");

        int result = -1;
        int numControls = spatial.getNumControls();
        for (int index = 0; index < numControls; ++index) {
            Control control = spatial.getControl(index);
            if (control == sgc) {
                result = index;
            }
        }

        return result;
    }

    /**
     * Access the skeleton (if any) in the specified scene-graph control.
     *
     * @param sgc which scene-graph control (may be null, unaffected)
     * @return the pre-existing instance, or null if none found
     */
    public static Skeleton findSkeleton(Control sgc) {
        Skeleton result = null;
        if (sgc instanceof AnimControl) {
            AnimControl animControl = (AnimControl) sgc;
            result = animControl.getSkeleton();

        } else if (sgc instanceof SkeletonControl) {
            SkeletonControl skeletonControl = (SkeletonControl) sgc;
            result = skeletonControl.getSkeleton();
        }

        return result;
    }

    /**
     * Add the specified Control at the specified position in the
     * {@code controls} list of the specified Spatial.
     *
     * @param spatial the Spatial to add to (not null, modified)
     * @param index the index at which to add the Control (&ge;0)
     * @param sgc the Control to add (not null)
     * @deprecated use {@link com.jme3.scene.Spatial#addControlAt(int,
     * com.jme3.scene.control.Control)}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static void insertAt(Spatial spatial, int index, Control sgc) {
        int numSgcs = spatial.getNumControls();
        Validate.inRange(index, "index", 0, numSgcs);
        Validate.nonNull(sgc, "control");

        Field controlsField;
        try {
            controlsField = Spatial.class.getDeclaredField("controls");
        } catch (NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
        controlsField.setAccessible(true);
        SafeArrayList<Control> controlsList;
        try {
            controlsList = (SafeArrayList<Control>) controlsField.get(spatial);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }

        spatial.addControl(sgc); // performs all the necessary bookkeeping

        if (index != numSgcs) { // re-arrange the list directly
            boolean success = controlsList.remove(sgc);
            assert success;
            controlsList.add(index, sgc);
        }
    }

    /**
     * Test whether the specified scene-graph control is enabled.
     *
     * @param sgc the control to test (not null, unaffected)
     * @return true if the control is enabled, otherwise false
     */
    public static boolean isEnabled(Control sgc) {
        Validate.nonNull(sgc, "control");

        boolean result;
        if (sgc instanceof AbstractControl) {
            AbstractControl abstractControl = (AbstractControl) sgc;
            result = abstractControl.isEnabled();

        } else {
            /*
             * Handle ChaseCamera, MotionEvent, ParticleEmitterControl,
             * StatsView, and custom controls.
             */
            Class<? extends Control> sgcClass = sgc.getClass();
            try {
                Method isEnabled = sgcClass.getMethod("isEnabled");
                result = (boolean) isEnabled.invoke(sgc);
            } catch (IllegalAccessException
                    | InvocationTargetException
                    | NoSuchMethodException exception) {
                String message = sgcClass.getName();
                throw new IllegalArgumentException(message);
            }
        }

        return result;
    }

    /**
     * Alter the enabled state of a scene-graph control.
     *
     * @param sgc control to alter (not null)
     * @param newState true means enable the control, false means disable it
     */
    public static void setEnabled(Control sgc, boolean newState) {
        if (sgc instanceof AbstractControl) {
            AbstractControl abstractControl = (AbstractControl) sgc;
            abstractControl.setEnabled(newState);

        } else {
            /*
             * Handle ChaseCamera, MotionEvent, ParticleEmitterControl,
             * StatsView, and custom controls.
             */
            Class<? extends Control> sgcClass = sgc.getClass();
            try {
                Method setEnabled = sgcClass.getMethod("setEnabled");
                setEnabled.invoke(sgc, newState);
            } catch (IllegalAccessException
                    | InvocationTargetException
                    | NoSuchMethodException exception) {
                String message = sgcClass.getName();
                throw new IllegalArgumentException(message);
            }
        }
    }
}
