/*
 Copyright (c) 2018-2024 Stephen Gold
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

import com.jme3.system.NullContext;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods that relate to display settings.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class DsUtils {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DsUtils.class.getName());
    /**
     * pattern for matching a display dimensions
     */
    final private static Pattern dimensionsPattern
            = Pattern.compile("^\\s*(\\d+)\\s*[x,]\\s*(\\d+)\\s*");

    final private static boolean hasLwjglVersion2;
    final private static boolean hasLwjglVersion3;
    final private static Field lwjglListenerField;
    final private static Field nullListenerField;

    final private static Method get;
    final private static Method getBitsPerPixel;
    final private static Method getBlueBits;
    final private static Method getFrequency;
    final private static Method getGreenBits;
    final private static Method getMode;
    final private static Method getModeHeight;
    final private static Method getModeWidth;
    final private static Method getModes;
    final private static Method getPrimaryMonitor;
    final private static Method getRedBits;
    final private static Method hasRemaining;

    static {
        boolean foundVersion2;
        try {
            Class.forName("org.lwjgl.opengl.Display");
            foundVersion2 = true;
        } catch (ClassNotFoundException exception) {
            foundVersion2 = false;
        }
        hasLwjglVersion2 = foundVersion2;

        boolean foundVersion3;
        try {
            Class.forName("com.jme3.system.lwjgl.LwjglWindow");
            foundVersion3 = true;
        } catch (ClassNotFoundException exception) {
            foundVersion3 = false;
        }
        hasLwjglVersion3 = foundVersion3;

        try {
            Class<?> contextClass
                    = Class.forName("com.jme3.system.lwjgl.LwjglContext");
            lwjglListenerField = contextClass.getDeclaredField("listener");
            lwjglListenerField.setAccessible(true);

            nullListenerField = NullContext.class.getDeclaredField("listener");
            nullListenerField.setAccessible(true);

            if (foundVersion2) {
                get = null;
                getBlueBits = null;
                getGreenBits = null;
                getPrimaryMonitor = null;
                getRedBits = null;
                hasRemaining = null;

                Class<?> displayClass
                        = Class.forName("org.lwjgl.opengl.Display");
                getMode = displayClass.getDeclaredMethod(
                        "getDesktopDisplayMode");
                getModes = displayClass.getDeclaredMethod(
                        "getAvailableDisplayModes");

                Class<?> displayModeClass = Class.forName(
                        "org.lwjgl.opengl.DisplayMode");
                getBitsPerPixel
                        = displayModeClass.getDeclaredMethod("getBitsPerPixel");
                getFrequency
                        = displayModeClass.getDeclaredMethod("getFrequency");
                getModeHeight = displayModeClass.getDeclaredMethod("getHeight");
                getModeWidth = displayModeClass.getDeclaredMethod("getWidth");

            } else if (foundVersion3) {
                getBitsPerPixel = null;

                Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
                getMode = glfwClass.getDeclaredMethod(
                        "glfwGetVideoMode", long.class);
                getModes = glfwClass.getDeclaredMethod(
                        "glfwGetVideoModes", long.class);
                getPrimaryMonitor = glfwClass.getDeclaredMethod(
                        "glfwGetPrimaryMonitor");

                Class<?> vidModeClass
                        = Class.forName("org.lwjgl.glfw.GLFWVidMode");
                getBlueBits = vidModeClass.getDeclaredMethod("blueBits");
                getFrequency = vidModeClass.getDeclaredMethod("refreshRate");
                getGreenBits = vidModeClass.getDeclaredMethod("greenBits");
                getModeHeight = vidModeClass.getDeclaredMethod("height");
                getModeWidth = vidModeClass.getDeclaredMethod("width");
                getRedBits = vidModeClass.getDeclaredMethod("redBits");

                Class<?>[] vmInnerClasses = vidModeClass.getDeclaredClasses();
                assert vmInnerClasses.length == 1 : vmInnerClasses.length;
                Class<?> vmBufferClass = vmInnerClasses[0];
                get = vmBufferClass.getMethod("get");
                hasRemaining = vmBufferClass.getMethod("hasRemaining");

            } else { // LWJGL not found
                get = null;
                getBitsPerPixel = null;
                getBlueBits = null;
                getFrequency = null;
                getGreenBits = null;
                getMode = null;
                getModeHeight = null;
                getModeWidth = null;
                getModes = null;
                getPrimaryMonitor = null;
                getRedBits = null;
                hasRemaining = null;
            }
        } catch (ClassNotFoundException | NoSuchFieldException
                | NoSuchMethodException | SecurityException exception) {
            throw new RuntimeException(exception);
        }
    }
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private DsUtils() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Describe the specified display dimensions.
     *
     * @param width width in pixels (&gt;0)
     * @param height height in pixels (&gt;0)
     * @return a textual description (not null, not empty)
     */
    public static String describeDimensions(int width, int height) {
        Validate.positive(width, "width");
        Validate.positive(height, "height");

        String result = String.format("%d x %d", width, height);
        return result;
    }

    /**
     * Describe an MSAA sampling factor.
     *
     * @param factor samples per pixel
     * @return a textual description (not null, not empty)
     */
    public static String describeMsaaFactor(int factor) {
        String description;
        if (factor <= 1) {
            description = "disabled";
        } else {
            description = String.format("%dx", factor);
        }

        return description;
    }

    /**
     * Return the default monitor's current display mode.
     *
     * @return a new instance (not null)
     */
    public static DisplayMode displayMode() {
        DisplayMode result;

        if (hasLwjglVersion2) {
            result = displayMode2();

        } else if (hasLwjglVersion3) {
            result = displayMode3();

        } else { // use AWT
            GraphicsEnvironment environment
                    = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = environment.getDefaultScreenDevice();
            result = device.getDisplayMode();
        }

        return result;
    }

    /**
     * Test whether jme3-lwjgl3 is in the classpath.
     *
     * @return true if present, otherwise false
     */
    public static boolean hasLwjglVersion3() {
        return hasLwjglVersion3;
    }

    /**
     * Enumerate the default monitor's available display modes.
     *
     * @return a new list of modes (not null)
     */
    public static List<DisplayMode> listDisplayModes() {
        List<DisplayMode> result;
        if (hasLwjglVersion2) {
            result = listDisplayModes2();
        } else if (hasLwjglVersion3) {
            result = listDisplayModes3();
        } else {
            // LWJGL v2 and v3 are unavailable, so use AWT instead.
            GraphicsEnvironment environment
                    = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = environment.getDefaultScreenDevice();
            DisplayMode[] modeArray = device.getDisplayModes();
            result = Arrays.asList(modeArray);
        }

        return result;
    }

    /**
     * Parse the specified text to obtain display dimensions.
     *
     * @param description the text to parse (not null, not empty)
     * @return a new array containing the width and height, or null for a syntax
     * error
     */
    public static int[] parseDimensions(String description) {
        Validate.nonEmpty(description, "text");

        String lcText = description.toLowerCase(Locale.ROOT);
        Matcher matcher = dimensionsPattern.matcher(lcText);
        int[] result = null;
        if (matcher.find()) {
            result = new int[2];
            String widthText = matcher.group(1);
            result[0] = Integer.parseInt(widthText);
            String heightText = matcher.group(2);
            result[1] = Integer.parseInt(heightText);
        }

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Return the default monitor's current display mode.
     *
     * @return a new instance (not null)
     */
    private static DisplayMode displayMode2() {
        assert hasLwjglVersion2;

        try {
            // DisplayMode glMode = Display.getDesktopDisplayMode();
            Object glMode = getMode.invoke(null);

            DisplayMode result = makeDisplayMode2(glMode);
            return result;

        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Return the default monitor's current display mode.
     *
     * @return a new instance (not null)
     */
    private static DisplayMode displayMode3() {
        assert hasLwjglVersion3;

        try {
            // long monitorId = GLFW.glfwGetPrimaryMonitor();
            Object monitorId = getPrimaryMonitor.invoke(null);

            // GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitorId);
            Object vidMode = getMode.invoke(null, monitorId);

            DisplayMode result = makeDisplayMode3(vidMode);
            return result;

        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Enumerate the default monitor's available display modes.
     *
     * @return a new list of modes (not null)
     */
    private static List<DisplayMode> listDisplayModes2() {
        assert hasLwjglVersion2;

        try {
            // DisplayMode[] glModes = Display.getAvailableDisplayModes();
            Object[] glModes = (Object[]) getModes.invoke(null);

            List<DisplayMode> result = new ArrayList<>(glModes.length);
            for (Object glMode : glModes) {
                DisplayMode mode = makeDisplayMode2(glMode);
                result.add(mode);
            }
            return result;

        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Enumerate the default monitor's available display modes.
     *
     * @return a new list of modes (not null)
     */
    private static List<DisplayMode> listDisplayModes3() {
        assert hasLwjglVersion3;

        try {
            // long monitorId = GLFW.glfwGetPrimaryMonitor();
            Object monitorId = getPrimaryMonitor.invoke(null);

            if (monitorId == null || 0L == (Long) monitorId) {
                return new ArrayList<>(1);
            }

            // GLFWVidMode.Buffer buf = GLFW.glfwGetVideoModes(monitorId);
            Object buf = getModes.invoke(null, monitorId);

            List<DisplayMode> result = new ArrayList<>(32);

            // while (buf.hasRemaining()) {
            while ((Boolean) hasRemaining.invoke(buf)) {

                // GLFWVidMode vidMode = buf.get();
                Object vidMode = get.invoke(buf);

                DisplayMode mode = makeDisplayMode3(vidMode);
                result.add(mode);
            }
            return result;

        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Convert an org.lwjgl.opengl.DisplayMode to a java.awt.DisplayMode via
     * reflection of LWJGL v2.
     *
     * @param glMode the AWT display mode (not null, unaffected)
     * @return a new instance
     *
     * @throws IllegalAccessException ?
     * @throws InvocationTargetException ?
     */
    private static DisplayMode makeDisplayMode2(Object glMode)
            throws IllegalAccessException, InvocationTargetException {
        assert hasLwjglVersion2;

        // int width = glMode.getWidth();
        int width = (Integer) getModeWidth.invoke(glMode);

        // int height = glMode.getHeight();
        int height = (Integer) getModeHeight.invoke(glMode);

        // int bitDepth = glMode.getBitsPerPixel();
        int bitDepth = (Integer) getBitsPerPixel.invoke(glMode);

        // int rate = glMode.getFrequency();
        int rate = (Integer) getFrequency.invoke(glMode);

        DisplayMode result = new DisplayMode(width, height, bitDepth, rate);

        return result;
    }

    /**
     * Convert a GLFWVidMode to a DisplayMode via reflection of LWJGL v3.
     *
     * @param glfwVidMode (not null, unaffected)
     * @return a new instance
     *
     * @throws IllegalAccessException ?
     * @throws InvocationTargetException ?
     */
    private static DisplayMode makeDisplayMode3(Object glfwVidMode)
            throws IllegalAccessException, InvocationTargetException {
        assert hasLwjglVersion3;

        // int width = glfwVidMode.width();
        int width = (Integer) getModeWidth.invoke(glfwVidMode);

        // int height = glfwVidMode.height();
        int height = (Integer) getModeHeight.invoke(glfwVidMode);

        // int redBits = glfwVidMode.redBits();
        int redBits = (Integer) getRedBits.invoke(glfwVidMode);

        // int greenBits = glfwVidMode.greenBits();
        int greenBits = (Integer) getGreenBits.invoke(glfwVidMode);

        // int blueBits = glfwVidMode.blueBits();
        int blueBits = (Integer) getBlueBits.invoke(glfwVidMode);

        // int rate = glfwVidMode.refreshRate();
        int rate = (Integer) getFrequency.invoke(glfwVidMode);

        int bitDepth = redBits + greenBits + blueBits;
        DisplayMode result = new DisplayMode(width, height, bitDepth, rate);

        return result;
    }
}
