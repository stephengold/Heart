/*
 Copyright (c) 2014-2024 Stephen Gold
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

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.AnimTrack;
import com.jme3.anim.Joint;
import com.jme3.anim.MorphTrack;
import com.jme3.anim.TransformTrack;
import com.jme3.anim.util.HasLocalTransform;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.AudioTrack;
import com.jme3.animation.Bone;
import com.jme3.animation.BoneTrack;
import com.jme3.animation.EffectTrack;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SpatialTrack;
import com.jme3.animation.Track;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.util.Arrays;
import java.util.logging.Logger;
import jme3utilities.math.MyArray;

/**
 * Utility methods for manipulating animations, clips, and tracks. All methods
 * should be static.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class MyAnimation {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MyAnimation.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private MyAnimation() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Copy the keyframe rotations for the specified bone/spatial track.
     *
     * @param track which Track (not null, unaffected)
     * @return a new array or null
     */
    public static Quaternion[] copyRotations(Track track) {
        Quaternion[] result;
        if (track instanceof BoneTrack) {
            BoneTrack boneTrack = (BoneTrack) track;
            result = boneTrack.getRotations();

        } else if (track instanceof SpatialTrack) {
            SpatialTrack spatialTrack = (SpatialTrack) track;
            result = spatialTrack.getRotations();

        } else {
            String className = track.getClass().getSimpleName();
            throw new IllegalArgumentException(className);
        }

        return result;
    }

    /**
     * Copy the keyframe scales for the specified bone/spatial track.
     *
     * @param track which Track (not null, unaffected)
     * @return a new array or null
     */
    public static Vector3f[] copyScales(Track track) {
        Vector3f[] result;
        if (track instanceof BoneTrack) {
            BoneTrack boneTrack = (BoneTrack) track;
            result = boneTrack.getScales();

        } else if (track instanceof SpatialTrack) {
            SpatialTrack spatialTrack = (SpatialTrack) track;
            result = spatialTrack.getScales();

        } else {
            String className = track.getClass().getSimpleName();
            throw new IllegalArgumentException(className);
        }

        return result;
    }

    /**
     * Copy the translations for the specified bone/spatial track.
     *
     * @param track which Track (not null, unaffected)
     * @return a new array or null
     */
    public static Vector3f[] copyTranslations(Track track) {
        Vector3f[] result;
        if (track instanceof BoneTrack) {
            BoneTrack boneTrack = (BoneTrack) track;
            result = boneTrack.getTranslations();

        } else if (track instanceof SpatialTrack) {
            SpatialTrack spatialTrack = (SpatialTrack) track;
            result = spatialTrack.getTranslations();

        } else {
            String className = track.getClass().getSimpleName();
            throw new IllegalArgumentException(className);
        }

        return result;
    }

    /**
     * Count all tracks of the specified type in the specified Animation.
     *
     * @param <T> subclass of Track
     * @param animation the Animation to search (may be null, unaffected)
     * @param trackType the subclass of Track to search for
     * @return the number of tracks found (&ge;0)
     */
    public static <T extends Track> int countTracks(
            Animation animation, Class<T> trackType) {
        int result = 0;

        if (animation != null) {
            Track[] tracks = animation.getTracks();
            for (Track track : tracks) {
                if (trackType.isAssignableFrom(track.getClass())) {
                    ++result;
                }
            }
        }

        assert result >= 0 : result;
        return result;
    }

    /**
     * Describe an Animation.
     *
     * @param animation the Animation to describe (not null, unaffected)
     * @param animControl the Control that contains the Animation (not null,
     * unaffected)
     * @return textual description (not null, not empty)
     */
    public static String describe(
            Animation animation, AnimControl animControl) {
        Validate.nonNull(animControl, "anim control");

        String name = animation.getName();
        Track[] tracks = animation.getTracks();

        String result;
        int numTracks = tracks.length;
        if (numTracks > 2) {
            result = String.format("%s[%d]", MyString.quote(name), numTracks);
        } else {
            String[] trackDescriptions = new String[numTracks];
            for (int trackIndex = 0; trackIndex < numTracks; ++trackIndex) {
                Track track = tracks[trackIndex];
                trackDescriptions[trackIndex] = describe(track, animControl);
            }
            String joined = MyString.join(trackDescriptions);
            result = String.format("%s(%s)", name, joined);
        }

        return result;
    }

    /**
     * Describe an AnimClip.
     *
     * @param clip the AnimClip to describe (not null, unaffected)
     * @param composer the Control that contains the clip (not null, unaffected)
     * @return textual description (not null, not empty)
     */
    public static String describe(AnimClip clip, AnimComposer composer) {
        Validate.nonNull(composer, "composer");

        String name = clip.getName();
        AnimTrack<?>[] tracks = clip.getTracks();

        String result;
        int numTracks = tracks.length;
        if (numTracks > 3) {
            result = String.format("%s[%d]", MyString.quote(name), numTracks);
        } else {
            String[] trackDescriptions = new String[numTracks];
            for (int trackIndex = 0; trackIndex < numTracks; ++trackIndex) {
                AnimTrack<?> track = tracks[trackIndex];
                trackDescriptions[trackIndex] = describe(track);
            }
            String joined = MyString.join(trackDescriptions);
            result = String.format("%s(%s)", name, joined);
        }

        return result;
    }

    /**
     * Describe an AnimTrack.
     *
     * @param track the AnimTrack to describe (not null, unaffected)
     * @return a textual description (not null, not empty)
     */
    public static String describe(AnimTrack track) {
        Validate.nonNull(track, "track");

        StringBuilder builder = new StringBuilder(32);

        char typeChar = describeTrackType(track);
        builder.append(typeChar);

        if (track instanceof MorphTrack) {
            Geometry target = ((MorphTrack) track).getTarget();
            builder.append(target.getClass().getSimpleName());
            String targetName = target.getName();
            targetName = MyString.quote(targetName);
            builder.append(targetName);

        } else if (track instanceof TransformTrack) {
            TransformTrack transformTrack = (TransformTrack) track;
            HasLocalTransform target = transformTrack.getTarget();
            builder.append(target.getClass().getSimpleName());
            String targetName = getTargetName(target);
            targetName = MyString.quote(targetName);
            builder.append(targetName);

            if (transformTrack.getTranslations() != null) {
                builder.append("T");
            }
            if (transformTrack.getRotations() != null) {
                builder.append("R");
            }
            if (transformTrack.getScales() != null) {
                builder.append("S");
            }
        }

        return builder.toString();
    }

    /**
     * Describe an animation track in the context of its Animation.
     *
     * @param track the Track to describe (not null, unaffected)
     * @param animControl an AnimControl that contains the Track (not null,
     * unaffected)
     * @return a textual description (not null, not empty)
     */
    public static String describe(Track track, AnimControl animControl) {
        Validate.nonNull(track, "track");
        Validate.nonNull(animControl, "anim control");

        StringBuilder builder = new StringBuilder(20);

        char typeChar = describeTrackType(track);
        builder.append(typeChar);

        if (track instanceof BoneTrack || track instanceof SpatialTrack) {
            String targetName = getTargetName(track, animControl);
            targetName = MyString.quote(targetName);
            builder.append(targetName);

            if (copyTranslations(track) != null) {
                builder.append("T");
            }
            if (copyRotations(track) != null) {
                builder.append("R");
            }
            if (copyScales(track) != null) {
                builder.append("S");
            }
        }

        String result = builder.toString();
        return result;
    }

    /**
     * Describe a track's type with a single character.
     *
     * @param track the track to describe (may be null, unaffected)
     * @return a mnemonic character
     */
    public static char describeTrackType(Object track) {
        if (track instanceof AudioTrack) {
            return 'a';
        } else if (track instanceof BoneTrack) {
            return 'b';
        } else if (track instanceof EffectTrack) {
            return 'e';
        } else if (track instanceof MorphTrack) {
            return 'm';
        } else if (track instanceof SpatialTrack) {
            return 's';
        } else if (track instanceof TransformTrack) {
            return 't';
        }
        return '?';
    }

    /**
     * Find a BoneTrack in a specified Animation that targets the indexed Bone.
     *
     * @param animation which Animation (not null, unaffected)
     * @param boneIndex which Bone (&ge;0)
     * @return the pre-existing instance, or null if none found
     */
    public static BoneTrack findBoneTrack(Animation animation, int boneIndex) {
        Validate.nonNegative(boneIndex, "bone index");

        Track[] tracks = animation.getTracks();
        for (Track track : tracks) {
            if (track instanceof BoneTrack) {
                BoneTrack boneTrack = (BoneTrack) track;
                int trackBoneIndex = boneTrack.getTargetBoneIndex();
                if (boneIndex == trackBoneIndex) {
                    return boneTrack;
                }
            }
        }

        return null;
    }

    /**
     * Find a TransformTrack in a specified AnimClip that targets the indexed
     * Joint.
     *
     * @param clip which AnimClip (not null, unaffected)
     * @param jointIndex which Joint (&ge;0)
     * @return the pre-existing instance, or null if none found
     */
    public static TransformTrack findJointTrack(
            AnimClip clip, int jointIndex) {
        Validate.nonNegative(jointIndex, "joint index");

        AnimTrack<?>[] tracks = clip.getTracks();
        for (AnimTrack<?> track : tracks) {
            if (track instanceof TransformTrack) {
                TransformTrack transformTrack = (TransformTrack) track;
                HasLocalTransform target = transformTrack.getTarget();
                if (target instanceof Joint) {
                    Joint joint = (Joint) target;
                    int trackJointIndex = joint.getId();
                    if (jointIndex == trackJointIndex) {
                        return transformTrack;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Find the index of the keyframe (if any) at the specified time in the
     * specified Track.
     *
     * @param track which Track to search (not null, unaffected)
     * @param time the track time (in seconds, &ge;0)
     * @return the keyframe's index (&ge;0) or -1 if no keyframe at that time
     */
    public static int findKeyframeIndex(Track track, float time) {
        Validate.nonNegative(time, "time");

        float[] times = track.getKeyFrameTimes(); // alias
        int result = Arrays.binarySearch(times, time);
        if (result < 0) {
            result = -1;
        }

        return result;
    }

    /**
     * Find the index of the keyframe (if any) at the specified time in the
     * specified TransformTrack.
     *
     * @param track the TransformTrack to search (not null, unaffected)
     * @param time the track time (in seconds, &ge;0)
     * @return the keyframe's index (&ge;0) or -1 if no keyframe at that time
     */
    public static int findKeyframeIndex(TransformTrack track, float time) {
        Validate.nonNegative(time, "time");

        float[] times = track.getTimes(); // alias
        int result = Arrays.binarySearch(times, time);
        if (result < 0) {
            result = -1;
        }

        return result;
    }

    /**
     * Find the time of the keyframe in the specified Animation with the latest
     * time.
     *
     * @param animation the input (not null, unaffected)
     * @return the track time (in seconds, &ge;0)
     */
    public static float findLastKeyframe(Animation animation) {
        float maxTime = 0f;
        Track[] loadedTracks = animation.getTracks();
        for (Track track : loadedTracks) {
            float[] frameTimes = track.getKeyFrameTimes(); // alias
            for (float time : frameTimes) {
                if (time > maxTime) {
                    maxTime = time;
                }
            }
        }

        return maxTime;
    }

    /**
     * Find the index of the last keyframe at or before the specified time in
     * the specified Track.
     *
     * @param track the Track to search (not null, unaffected)
     * @param time the track time (in seconds, &ge;0)
     * @return the keyframe's index (&ge;0)
     */
    public static int findPreviousKeyframeIndex(Track track, float time) {
        Validate.nonNegative(time, "time");

        float[] times = track.getKeyFrameTimes(); // alias
        int result = MyArray.findPreviousIndex(time, times);

        assert result >= 0 : result;
        return result;
    }

    /**
     * Find the index of the last keyframe at or before the specified time in
     * the specified TransformTrack.
     *
     * @param track the TransformTrack to search (not null, unaffected)
     * @param time the track time (in seconds, &ge;0)
     * @return the keyframe's index (&ge;0)
     */
    public static int findPreviousKeyframeIndex(
            TransformTrack track, float time) {
        Validate.nonNegative(time, "time");

        float[] times = track.getTimes(); // alias
        int result = MyArray.findPreviousIndex(time, times);

        assert result >= 0 : result;
        return result;
    }

    /**
     * Find a SpatialTrack in the specified animation for the specified spatial.
     *
     * @param animControl the AnimControl containing the Animation (not null,
     * unaffected)
     * @param animation which Animation to search (not null, unaffected)
     * @param spatial which Spatial to find (unaffected)
     * @return the pre-existing instance, or null if not found
     */
    public static SpatialTrack findSpatialTrack(
            AnimControl animControl, Animation animation, Spatial spatial) {
        Track[] tracks = animation.getTracks();
        for (Track track : tracks) {
            if (track instanceof SpatialTrack) {
                SpatialTrack spatialTrack = (SpatialTrack) track;
                Spatial target = spatialTrack.getTrackSpatial();
                if (target == null) {
                    target = animControl.getSpatial();
                }
                if (target == spatial) {
                    return spatialTrack;
                }
            }
        }

        return null;
    }

    /**
     * Find the specified Track in the specified Animation.
     *
     * @param animation the Animation containing the Track (not null,
     * unaffected)
     * @param track which Track to find (unaffected)
     * @return the track index (&ge;0) or -1 if not found
     */
    public static int findTrackIndex(Animation animation, Track track) {
        int result = -1;
        Track[] tracks = animation.getTracks();
        int numTracks = tracks.length;
        for (int index = 0; index < numTracks; ++index) {
            if (track == tracks[index]) {
                result = index;
                break;
            }
        }

        return result;
    }

    /**
     * Find a TransformTrack in a specified AnimClip for the indexed Joint.
     *
     * @param clip which AnimClip (not null, unaffected)
     * @param jointIndex which Joint (&ge;0)
     * @return the pre-existing instance, or null if not found
     */
    public static TransformTrack findTransformTrack(
            AnimClip clip, int jointIndex) {
        TransformTrack result = null;

        AnimTrack<?>[] animTracks = clip.getTracks();
        for (AnimTrack<?> animTrack : animTracks) {
            if (animTrack instanceof TransformTrack) {
                TransformTrack track = (TransformTrack) animTrack;
                HasLocalTransform target = track.getTarget();
                if (target instanceof Joint) {
                    int targetIndex = ((Joint) target).getId();
                    if (targetIndex == jointIndex) {
                        result = track;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Access the time array of the specified track.
     *
     * @param object the input track (a MorphTrack, TransformTrack, or Track)
     * @return the pre-existing array (not null, length&gt;0)
     */
    public static float[] getKeyFrameTimes(Object object) {
        float[] result;
        if (object instanceof MorphTrack) {
            MorphTrack morphTrack = (MorphTrack) object;
            result = morphTrack.getTimes(); // alias
        } else if (object instanceof Track) {
            Track t = (Track) object;
            result = t.getKeyFrameTimes(); // alias
        } else if (object instanceof TransformTrack) {
            TransformTrack transformTrack = (TransformTrack) object;
            result = transformTrack.getTimes(); // alias
        } else {
            String className = object.getClass().getSimpleName();
            throw new IllegalArgumentException(className);
        }

        assert result != null;
        assert result.length > 0 : result.length;
        return result;
    }

    /**
     * Copy the keyframe rotations for the specified bone/spatial track.
     *
     * @param track which Track (not null, unaffected)
     * @return a new array or null
     * @deprecated use {@link #copyRotations(com.jme3.animation.Track)}
     */
    @Deprecated
    public static Quaternion[] getRotations(Track track) {
        Quaternion[] result = copyRotations(track);
        return result;
    }

    /**
     * Copy the keyframe scales for the specified bone/spatial track.
     *
     * @param track which Track (not null, unaffected)
     * @return a new array or null
     * @deprecated use {@link #copyScales(com.jme3.animation.Track)}
     */
    public static Vector3f[] getScales(Track track) {
        Vector3f[] result = copyScales(track);
        return result;
    }

    /**
     * Determine the name of the specified animation target.
     *
     * @param target the target to analyze (not null, unaffected)
     * @return the name of target joint/spatial
     */
    public static String getTargetName(HasLocalTransform target) {
        Validate.nonNull(target, "target");

        String result;
        if (target instanceof Joint) {
            result = ((Joint) target).getName();

        } else if (target instanceof Spatial) {
            result = ((Spatial) target).getName();

        } else {
            String className = target.getClass().getSimpleName();
            throw new IllegalArgumentException("className = " + className);
        }

        return result;
    }

    /**
     * Read the name of the target of the specified bone/spatial track in the
     * specified AnimControl.
     *
     * @param track the bone/spatial track (not null, unaffected)
     * @param animControl the Control that contains the Track (not null,
     * unaffected)
     * @return the name of target bone/spatial
     */
    public static String getTargetName(Track track, AnimControl animControl) {
        Validate.nonNull(track, "track");

        String result;
        if (track instanceof BoneTrack) {
            BoneTrack boneTrack = (BoneTrack) track;
            int boneIndex = boneTrack.getTargetBoneIndex();
            Skeleton skeleton = animControl.getSkeleton();
            Bone bone = skeleton.getBone(boneIndex);
            result = bone.getName();

        } else if (track instanceof SpatialTrack) {
            SpatialTrack spatialTrack = (SpatialTrack) track;
            Spatial spatial = spatialTrack.getTrackSpatial();
            if (spatial == null) {
                spatial = animControl.getSpatial();
            }
            result = spatial.getName();

        } else {
            String className = track.getClass().getSimpleName();
            throw new IllegalArgumentException(className);
        }

        return result;
    }

    /**
     * Copy the translations for the specified bone/spatial track.
     *
     * @param track which Track (not null, unaffected)
     * @return a new array or null
     * @deprecated use {@link #copyTranslations(com.jme3.animation.Track)}
     */
    public static Vector3f[] getTranslations(Track track) {
        Vector3f[] result = copyTranslations(track);
        return result;
    }

    /**
     * Test whether the specified Animation includes a BoneTrack for the indexed
     * Bone.
     *
     * @param animation the Animation to test (not null, unaffected)
     * @param boneIndex which Bone (&ge;0)
     * @return true if a track exists, otherwise false
     */
    public static boolean hasTrackForBone(Animation animation, int boneIndex) {
        Validate.nonNegative(boneIndex, "bone index");

        Track track = findBoneTrack(animation, boneIndex);
        if (track == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the specified AnimTrack targets a Joint.
     *
     * @param track the AnimTrack to test (not null, unaffected)
     * @return true if it targets a Joint, otherwise false
     */
    public static boolean isJointTrack(AnimTrack<?> track) {
        boolean result = false;

        if (track instanceof TransformTrack) {
            TransformTrack transformTrack = (TransformTrack) track;
            HasLocalTransform target = transformTrack.getTarget();
            if (target instanceof Joint) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Test whether the specified AnimTrack targets a Spatial.
     *
     * @param track the AnimTrack to test (may be null, unaffected)
     * @return true if it targets a Spatial, otherwise false
     */
    public static boolean isSpatialTrack(AnimTrack<?> track) {
        boolean result = false;

        if (track instanceof TransformTrack) {
            TransformTrack transformTrack = (TransformTrack) track;
            HasLocalTransform target = transformTrack.getTarget();
            if (target instanceof Spatial) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Create a BoneTrack consisting of a single keyframe at t=0.
     *
     * @param boneIndex which Bone (&ge;0)
     * @param translation relative to bind pose (not null, unaffected)
     * @param rotation relative to bind pose (not null, unaffected)
     * @param scale relative to bind pose (not null, unaffected)
     * @return a new bone track
     */
    public static BoneTrack newBoneTrack(int boneIndex, Vector3f translation,
            Quaternion rotation, Vector3f scale) {
        Validate.nonNegative(boneIndex, "bone index");

        Vector3f copyTranslation = translation.clone();
        Quaternion copyRotation = rotation.clone();
        Vector3f copyScale = scale.clone();

        float[] times = {0f};
        Vector3f[] translations = {copyTranslation};
        Quaternion[] rotations = {copyRotation};
        Vector3f[] scales = {copyScale};
        BoneTrack result = newBoneTrack(
                boneIndex, times, translations, rotations, scales);

        return result;
    }

    /**
     * Create a new BoneTrack, with or without scales.
     *
     * @param boneIndex (&ge;0)
     * @param times (not null, alias created)
     * @param translations (not null, same length as times)
     * @param rotations (not null, same length as times)
     * @param scales (either null or same length as times)
     * @return a new bone track
     */
    public static BoneTrack newBoneTrack(int boneIndex, float[] times,
            Vector3f[] translations, Quaternion[] rotations,
            Vector3f[] scales) {
        Validate.nonNull(times, "times");
        Validate.nonNull(translations, "translations");
        Validate.nonNull(rotations, "rotations");
        int numKeyframes = times.length;
        assert translations.length == numKeyframes;
        assert rotations.length == numKeyframes;
        assert scales == null || scales.length == numKeyframes;

        BoneTrack result;
        if (scales == null) {
            result = new BoneTrack(boneIndex, times, translations, rotations);
        } else {
            result = new BoneTrack(
                    boneIndex, times, translations, rotations, scales);
        }

        return result;
    }

    /**
     * Create a BoneTrack in which all keyframes have the same Transform.
     *
     * @param boneIndex which bone (&ge;0)
     * @param frameTimes (not null, unaffected)
     * @param transform the desired Transform (not null, unaffected)
     * @return a new BoneTrack
     */
    public static BoneTrack newBoneTrack(
            int boneIndex, float[] frameTimes, Transform transform) {
        Validate.nonNegative(boneIndex, "bone index");

        int numFrames = frameTimes.length;
        float[] times = new float[numFrames];
        Vector3f[] translations = new Vector3f[numFrames];
        Quaternion[] rotations = new Quaternion[numFrames];
        Vector3f[] scales = new Vector3f[numFrames];
        Transform clonedTransform = transform.clone();

        for (int frameIndex = 0; frameIndex < numFrames; ++frameIndex) {
            times[frameIndex] = frameTimes[frameIndex];
            translations[frameIndex]
                    = clonedTransform.getTranslation(); // alias
            rotations[frameIndex] = clonedTransform.getRotation(); // alias
            scales[frameIndex] = clonedTransform.getScale(); // alias
        }
        BoneTrack result = newBoneTrack(
                boneIndex, times, translations, rotations, scales);

        return result;
    }
}
