/*
 Copyright (c) 2013-2026 Stephen Gold
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

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.export.FormatVersion;
import com.jme3.export.JmeExporter;
import com.jme3.export.Savable;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.util.clone.Cloner;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import jme3utilities.math.IntPair;
import jme3utilities.math.RectangularSolid;
import jme3utilities.math.Vector3i;
import jme3utilities.math.VectorXZ;

/**
 * Miscellaneous utility methods in the Heart Library. All methods should be
 * static.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class Heart {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Heart.class.getName());
    // *************************************************************************
    // fields

    /**
     * handler for logging
     */
    private static FileHandler fileHandler = null;
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Heart() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether assertions are enabled.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     */
    public static boolean areAssertionsEnabled() {
        boolean enabled = false;
        assert enabled = true; // Note: intentional side effect.

        return enabled;
    }

    /**
     * Open the specified web page in a new browser or browser tab.
     *
     * @param startUriString URI of the web page (not {@code null})
     * @return {@code true} if successful, otherwise {@code false}
     */
    public static boolean browseWeb(String startUriString) {
        Validate.nonNull(startUriString, "start uri");

        boolean success = false;
        if (Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                URI startUri = new URI(startUriString);
                Desktop.getDesktop().browse(startUri);
                success = true;
            } catch (IOException | URISyntaxException exception) {
                // do nothing
            }
        }

        return success;
    }

    /**
     * Copy the specified object using deepClone() if it's cloneable. This works
     * around JME issue #879, but still doesn't handle all classes.
     *
     * @param <T> the type of object to be copied
     * @param object the input (unaffected)
     * @return an instance equivalent to the input
     */
    public static <T> T deepCopy(T object) {
        T copy;
        if (object == null) {
            copy = null;

        } else if (object instanceof Cloneable
                || object.getClass().isArray()) {
            copy = Cloner.deepClone(object);

        } else if (object instanceof Boolean
                || object instanceof Byte
                || object instanceof Character
                || object instanceof Double
                || object instanceof Enum
                || object instanceof Float
                || object instanceof Integer
                || object instanceof IntPair
                || object instanceof Long
                || object instanceof RectangularSolid
                || object instanceof Short
                || object instanceof String
                || object instanceof Vector3i
                || object instanceof VectorXZ) {
            copy = object;

        } else {
            copy = Cloner.deepClone(object);
        }

        return copy;
    }

    /**
     * Delete the application's stored settings, if any.
     *
     * @param applicationName the name of the application
     */
    public static void deleteStoredSettings(String applicationName) {
        try {
            if (Preferences.userRoot().nodeExists(applicationName)) {
                Preferences.userRoot().node(applicationName).removeNode();
                logger.log(Level.WARNING,
                        "The stored settings for \"{0}\" were deleted.",
                        applicationName);
            } else {
                logger.log(Level.WARNING,
                        "No stored settings were found for \"{0}\".",
                        applicationName);
            }

        } catch (BackingStoreException exception) {
            logger.log(Level.SEVERE,
                    "The stored settings for \"{0}\" are inaccessible.",
                    applicationName);
        }
    }

    /**
     * Detach all app states which are subclasses of a specified class.
     *
     * @param <T> class to scan for
     * @param stateManager (not {@code null})
     * @param whichClass (not {@code null})
     */
    public static <T extends AppState> void detachAll(
            AppStateManager stateManager, Class<T> whichClass) {
        Validate.nonNull(whichClass, "class");

        AppState state = stateManager.getState(whichClass);
        while (state != null) {
            stateManager.detach(state);
            state = stateManager.getState(whichClass);
        }
    }

    /**
     * Construct a map from drive paths (roots) to file objects.
     *
     * @return a new map from canonical file-system paths to files
     */
    public static Map<String, File> driveMap() {
        Map<String, File> result = new TreeMap<>();
        File[] roots = File.listRoots();
        for (File root : roots) {
            if (root.isDirectory()) {
                String absoluteDirPath = fixedPath(root);
                File oldFile = result.put(absoluteDirPath, root);
                assert oldFile == null : oldFile;
            }
        }

        return result;
    }

    /**
     * Access the first member of a collection.
     *
     * @param <T> the type of the member
     * @param collection the collection to access (not {@code null})
     * @return the pre-existing member, or {@code null} if none
     */
    @SuppressWarnings("unchecked")
    public static <T> T first(Collection<T> collection) {
        T result = null;
        if (!collection.isEmpty()) {
            Object[] members = collection.toArray(new Object[0]);
            result = (T) members[0];
        }

        return result;
    }

    /**
     * Canonicalize a file's path and convert backslashes to slashes.
     *
     * @param inputFile the input file (not {@code null}, not empty)
     * @return the fixed file path (not {@code null}, not empty)
     */
    public static String fixedPath(File inputFile) {
        Validate.nonNull(inputFile, "input file");

        String result;
        try {
            result = inputFile.getCanonicalPath();
        } catch (IOException exception) {
            result = inputFile.getAbsolutePath();
        }
        result = result.replaceAll("\\\\", "/");

        assert result != null;
        assert !result.isEmpty();
        return result;
    }

    /**
     * Canonicalize a file path and convert backslashes to slashes.
     *
     * @param inputPath the file path to fix (not {@code null}, not empty)
     * @return the fixed file path (not {@code null}, not empty)
     */
    public static String fixPath(String inputPath) {
        Validate.nonEmpty(inputPath, "input path");

        File file = new File(inputPath);
        String result = fixedPath(file);

        return result;
    }

    /**
     * Access the pre-existing FileHandler for logging, or if none is found,
     * create one and use that.
     *
     * @return not {@code null}
     */
    public static FileHandler getFileHandler() {
        if (fileHandler == null) {
            Calendar rightNow = Calendar.getInstance();
            int hours = rightNow.get(Calendar.HOUR_OF_DAY);
            int minutes = rightNow.get(Calendar.MINUTE);
            int seconds = rightNow.get(Calendar.SECOND);
            String hhmmss
                    = String.format("%02d%02d%02d", hours, minutes, seconds);
            String fileName = hhmmss + ".txt";

            try {
                fileHandler = new FileHandler(fileName);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            // New file handlers default to XML format.
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            File file = new File(fileName);
            String filePath = fixedPath(file);
            System.out.println("logging to file " + MyString.quote(filePath));
        }

        return fileHandler;
    }

    /**
     * Access the pre-existing FilterPostProcessor of the specified view port,
     * or if it has none, add a new FPP and use that.
     *
     * @param viewPort which view port (not {@code null})
     * @param assetManager (not {@code null})
     * @param numSamples number of samples for anti-aliasing (&ge;1, &le;16) or
     * 0 for the FPP default
     * @return not {@code null}
     */
    public static FilterPostProcessor getFpp(
            ViewPort viewPort, AssetManager assetManager, int numSamples) {
        Validate.nonNull(viewPort, "viewport");
        Validate.nonNull(assetManager, "asset manager");
        Validate.inRange(numSamples, "number of samples", 0, 16);

        for (SceneProcessor processor : viewPort.getProcessors()) {
            if (processor instanceof FilterPostProcessor) {
                return (FilterPostProcessor) processor;
            }
        }

        // Add a new filter post-processor.
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        if (numSamples > 0) {
            fpp.setNumSamples(numSamples);
        }
        viewPort.addProcessor(fpp);

        return fpp;
    }

    /**
     * Access the keyboard interface of the specified InputManager.
     *
     * @param inputManager the InputManager to access (not {@code null},
     * unaffected)
     * @return the pre-existing instance
     */
    public static KeyInput getKeyInput(InputManager inputManager) {
        // Use reflection to access the "keys" field of the input manager.
        Field keyInputField;
        try {
            keyInputField = InputManager.class.getDeclaredField("keys");
        } catch (NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
        keyInputField.setAccessible(true);

        KeyInput result;
        try {
            result = (KeyInput) keyInputField.get(inputManager);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }

        return result;
    }

    /**
     * Test whether the named application has stored settings.
     *
     * @param applicationName the name of the application
     * @return {@code true} if it has stored settings, otherwise {@code false}
     */
    public static boolean hasStoredSettings(String applicationName) {
        try {
            if (Preferences.userRoot().nodeExists(applicationName)) {
                return true;
            }
        } catch (BackingStoreException exception) {
            // do nothing
        }

        return false;
    }

    /**
     * Generate a canonical filesystem path to the named file in the user's home
     * directory.
     *
     * @param fileName file name to use (not {@code null}, not empty)
     * @return the file-system path (not {@code null}, not empty)
     */
    public static String homePath(String fileName) {
        Validate.nonEmpty(fileName, "file name");

        String homePath = System.getProperty("user.home");
        File file = new File(homePath, fileName);
        String result = fixedPath(file);

        return result;
    }

    /**
     * Enumerate all entries (in the specified JAR or ZIP) whose names begin
     * with the specified prefix.
     *
     * @param zipPath filesystem path to the JAR or ZIP (not {@code null}, not
     * empty)
     * @param namePrefix (not {@code null})
     * @return a new list of entry names
     */
    public static List<String> listZipEntries(
            String zipPath, String namePrefix) {
        Validate.nonEmpty(zipPath, "zip path");
        Validate.nonNull(namePrefix, "name prefix");

        List<String> result = new ArrayList<>(90);
        try (FileInputStream fileIn = new FileInputStream(zipPath);
                ZipInputStream zipIn = new ZipInputStream(fileIn)) {
            for (ZipEntry entry = zipIn.getNextEntry();
                    entry != null;
                    entry = zipIn.getNextEntry()) {
                String entryName = "/" + entry.getName();
                if (entryName.startsWith(namePrefix)) {
                    result.add(entryName);
                }
            }
        } catch (IOException exception) {
            // quit reading entries
        }

        return result;
    }

    /**
     * Load UTF-8 text from the named resource.
     *
     * @param resourceName the name of the classpath resource to load (not
     * {@code null})
     * @return the text (possibly multiple lines)
     */
    public static String loadResourceAsString(String resourceName) {
        // Open the resource as a stream.
        InputStream stream = Heart.class.getResourceAsStream(resourceName);
        if (stream == null) {
            String quotedName = MyString.quote(resourceName);
            throw new RuntimeException("resource not found:  " + quotedName);
        }

        // Parse the stream's data into one long text string.
        String charsetName = StandardCharsets.UTF_8.name();
        String result;
        try (Scanner scanner = new Scanner(stream, charsetName)) {
            scanner.useDelimiter("\\Z");
            result = scanner.next();
        }

        return result;
    }

    /**
     * Parse some conventional command-line arguments and apply them to the
     * specified SimpleApplication.
     *
     * @param application the application to modify (not {@code null})
     * @param arguments array of command-line arguments (not {@code null})
     */
    public static void parseAppArgs(
            SimpleApplication application, String... arguments) {
        boolean showSettingsDialog = false;
        Level loggingLevel = Level.WARNING;

        // Process any command-line arguments.
        for (String arg : arguments) {
            switch (arg) {
                case "-s":
                case "--showSettingsDialog":
                    showSettingsDialog = true;
                    break;

                case "-v":
                case "--verbose":
                    loggingLevel = Level.INFO;
                    break;

                default:
                    logger.log(Level.INFO,
                            "Unknown command-line argument {0} skipped",
                            MyString.quote(arg));
            }
        }

        setLoggingLevels(loggingLevel);
        application.setShowSettings(showSettingsDialog);
    }

    /**
     * Set the specified pixel to the specified brightness and opacity.
     *
     * @param graphics rendering context of the pixel (not {@code null})
     * @param x pixel's first coordinate (&lt;width, &ge;0)
     * @param y pixel's 2nd coordinate (&lt;height, &ge;0)
     * @param brightness (&le;1, &ge;0, 0 &rarr; black, 1 &rarr; white)
     * @param opacity (&le;1, &ge;0, 0 &rarr; transparent, 1 &rarr; opaque)
     */
    public static void setGrayPixel(Graphics2D graphics, int x, int y,
            float brightness, float opacity) {
        GraphicsConfiguration configuration = graphics.getDeviceConfiguration();
        Rectangle bounds = configuration.getBounds();
        Validate.inRange(x, "X coordinate", 0, bounds.width - 1);
        Validate.inRange(y, "Y coordinate", 0, bounds.height - 1);
        Validate.fraction(brightness, "brightness");
        Validate.fraction(opacity, "opacity");

        Color color = new Color(brightness, brightness, brightness, opacity);
        graphics.setColor(color);
        graphics.fillRect(x, y, 1, 1);
    }

    /**
     * Alter the logging levels of all known loggers.
     *
     * @param newLevel the desired level (not {@code null})
     * @see java.util.logging.Level
     */
    public static void setLoggingLevels(Level newLevel) {
        Validate.nonNull(newLevel, "level");
        Logger.getLogger("").setLevel(newLevel);
    }

    /**
     * Read the verbose version string for this library.
     *
     * @return project name, library name, branch, and revision
     */
    public static String version() {
        return "Heart Heart master $Rev: 9.3.1-SNAPSHOT $";
    }

    /**
     * Return the terse version string for this library.
     *
     * @return the branch name and revision string (not {@code null}, not empty)
     */
    public static String versionShort() {
        String verbose = version();
        String[] words = verbose.split("\\s+");
        assert words.length == 6 : words.length;
        String result = String.format("%s %s", words[2], words[4]);

        assert !result.isEmpty();
        return result;
    }

    /**
     * Write the specified image to the specified file, attempting to overwrite
     * any pre-existing file.
     *
     * @param filePath the path to the output file (not {@code null}, not empty)
     * @param image the image to be written (not {@code null})
     * @throws IOException if the file cannot be written
     */
    public static void writeImage(String filePath, RenderedImage image)
            throws IOException {
        Validate.nonEmpty(filePath, "path");
        Validate.nonNull(image, "image");
        /*
         * Determine the output format based on the filename
         * or else default to PNG.
         */
        String formatName = "png";
        String lowerCase = filePath.toLowerCase();
        if (lowerCase.endsWith(".bmp")) {
            formatName = "bmp";
        } else if (lowerCase.endsWith(".gif")) {
            formatName = "gif";
        } else if (lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")) {
            formatName = "jpeg";
        }
        // TODO write Microsoft's DDS file format as well
        /*
         * ImageIO fails silently when asked to write alpha to a BMP.
         * It throws an IOException when asked to write alpha to a JPEG.
         */
        boolean hasAlpha = image.getColorModel().hasAlpha();
        if (hasAlpha
                && (formatName.equals("bmp") || formatName.equals("jpeg"))) {
            logger.log(Level.SEVERE, "unable to write alpha channel to a {0}",
                    formatName.toUpperCase());
        }

        String quotedPath = MyString.quote(filePath);
        File textureFile = new File(filePath);
        try {
            // If a parent directory/folder is needed, create it.
            File parentDirectory = textureFile.getParentFile();
            if (parentDirectory != null && !parentDirectory.exists()) {
                boolean success = parentDirectory.mkdirs();
                if (!success) {
                    String parentPath = parentDirectory.toString();
                    quotedPath = MyString.quote(parentPath);
                    throw new IOException("Unable to create " + quotedPath);
                }
            }

            boolean success = ImageIO.write(image, formatName, textureFile);
            if (!success) {
                logger.log(Level.SEVERE,
                        "write to {0} failed; no writer for {1} format",
                        new Object[]{
                            quotedPath, MyString.quote(formatName)
                        });
            }
            if (logger.isLoggable(Level.INFO)) {
                int height = image.getHeight();
                int width = image.getWidth();
                logger.log(Level.INFO, "wrote {0}-by-{1} texture to {2}",
                        new Object[]{width, height, quotedPath});
            }

        } catch (IOException exception) {
            logger.log(Level.SEVERE, "write to {0} failed", quotedPath);
            boolean success = textureFile.delete();
            if (success) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "deleted file {0}", quotedPath);
                }
            } else {
                logger.log(Level.SEVERE, "deletion of {0} failed", quotedPath);
            }
            throw exception;
        }
    }

    /**
     * Write the specified Savable to the specified J3O file.
     *
     * @param filePath (not {@code null}, not empty, should end in ".j3o")
     * @param savable (not {@code null}, unaffected)
     */
    public static void writeJ3O(String filePath, Savable savable) {
        Validate.nonEmpty(filePath, "file path");
        Validate.nonNull(savable, "savable");

        JmeExporter exporter = BinaryExporter.getInstance();
        File file = new File(filePath);
        try {
            exporter.save(savable, file);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "write to {0} failed",
                    MyString.quote(filePath));
            throw new RuntimeException(exception);
        }
        if (logger.isLoggable(Level.INFO)) {
            int version = FormatVersion.VERSION;
            logger.log(Level.INFO, "wrote version-{0} binary to {1}",
                    new Object[]{version, MyString.quote(filePath)});
        }
    }
}
