/*
 Copyright (c) 2021-2023, Stephen Gold
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
package jme3utilities.cli;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.export.JmeExporter;
import com.jme3.export.binary.BinaryLoader;
import com.jme3.export.xml.XMLExporter;
import com.jme3.material.Material;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.texture.plugins.AWTLoader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MySpatial;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;

/**
 * A command-line utility to dump J3O assets.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class J3oDump {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(J3oDump.class.getName());
    // *************************************************************************
    // fields

    /**
     * load assets
     */
    private static AssetManager assetManager;
    /**
     * option to generate XML
     */
    private static boolean generateXml = false;
    /**
     * option to list textures
     */
    private static boolean listTextures = false;
    /**
     * dump asset descriptions to {@code System.out}
     */
    final private static Dumper dumper = new Dumper();
    /**
     * filesystem path to the asset root
     */
    private static String assetRoot = ".";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private J3oDump() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the MinieDump application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        // Mute a disruptive logger.
        Logger materialLogger = Logger.getLogger(Material.class.getName());
        materialLogger.setLevel(Level.SEVERE);

        dumper.setDumpUser(false);

        // Process the command-line arguments.
        boolean printHelp = false;
        int numArguments = arguments.length;
        if (numArguments == 0) {
            printHelp = true;
        }
        int lastIndex = numArguments - 1;
        int i = 0;
        while (i < numArguments) {
            String argument = arguments[i];
            if (argument.equals("--help") || argument.equals("-h")
                    || argument.equals("--usage") || argument.equals("-u")) {
                printHelp = true;

            } else if (argument.equals("--root") || argument.equals("-r")) {
                if (i == lastIndex) {
                    System.err.println("Missing argument for " + argument);
                    printHelp();
                    System.exit(1);
                } else {
                    assetRoot = arguments[i + 1];
                }
                ++i;

            } else if (argument.equals("--textures") || argument.equals("-t")) {
                listTextures = true;

            } else if (argument.equals("--verbose") || argument.equals("-v")) {
                dumper.setDumpBounds(false)
                        .setDumpBucket(false)
                        .setDumpCull(false)
                        .setDumpMatParam(true)
                        .setDumpOverride(true)
                        .setDumpShadow(false)
                        .setDumpTransform(true)
                        .setDumpUser(false)
                        .setDumpVertex(false);

            } else if (argument.equals("--veryverbose")
                    || argument.equals("-V")) {
                dumper.setDumpBounds(true)
                        .setDumpBucket(true)
                        .setDumpCull(true)
                        .setDumpMatParam(true)
                        .setDumpOverride(true)
                        .setDumpShadow(true)
                        .setDumpTransform(true)
                        .setDumpUser(true)
                        .setDumpVertex(true);

            } else if (argument.equals("--xml") || argument.equals("-x")) {
                generateXml = true;

            } else if (argument.endsWith(".j3o")) {
                dumpAsset(argument);

            } else {
                String quotedArg = MyString.quote(argument);
                System.err.println("Unrecognized argument:  " + quotedArg);
                printHelp();
                System.exit(1);
            }

            ++i;
        }

        if (printHelp) {
            printHelp();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Dump the asset at the specified path.
     *
     * @param assetPath a path to the asset (not null, not empty)
     */
    private static void dumpAsset(String assetPath) {
        newAssetManager();

        System.out.print(MyString.quote(assetPath));
        System.out.flush();

        ModelKey modelKey = new ModelKey(assetPath);
        Spatial loadedAsset = assetManager.loadModel(modelKey);

        System.out.print(" contains a model or scene");
        if (listTextures) {
            System.out.print(" with ");
            List<Texture> textures = MySpatial.listTextures(loadedAsset, null);
            int numTextures = textures.size();
            if (numTextures == 0) {
                System.out.println("no textures.");
            } else if (numTextures == 1) {
                System.out.println("one texture:");
            } else {
                System.out.printf("%d textures:", numTextures);
            }
            for (Texture texture : textures) {
                System.out.println("  " + texture);
            }
        } else {
            System.out.println(":");
        }
        dumper.dump(loadedAsset, "  ");
        System.out.println();

        if (generateXml) {
            String xmlPath = assetPath.replace(".j3o", ".xml");
            File xmlFile = new File(assetRoot, xmlPath);
            JmeExporter exporter = XMLExporter.getInstance();
            try {
                exporter.save(loadedAsset, xmlFile);
            } catch (IOException exception) {
                logger.log(Level.SEVERE, exception.getMessage(), exception);
            }
        }
    }

    /**
     * Print the help/usage hints.
     */
    private static void printHelp() {
        System.err.flush();
        String workingDirectory = System.getProperty("user.dir");
        System.out.printf("NAME:%n"
                + "  j3odump - dump J3O files to standard output.%n%n"
                + "USAGE:%n"
                + "  j3odump [ARGUMENTS...]%n%n"
                + "ARGUMENTS:%n%n"
                + "  --help, --usage, -h, -u  print this help message%n"
                + "  --root path, -r path     set path to the asset root (%s)%n"
                + "  --textures, -t           also list textures%n"
                + "  --verbose, -v            set verbose mode%n"
                + "  --veryverbose, -V        set very verbose mode%n"
                + "  --xml, -x                also generate an XML file%n"
                + "  asset/path.j3o           an asset to dump%n%n",
                "default = " + MyString.quote(workingDirectory));
    }

    /**
     * Create an AssetManager for the current asset root.
     */
    private static void newAssetManager() {
        File dir = new File(assetRoot);
        if (!dir.exists()) {
            logger.log(Level.SEVERE, "No such file:  {0}",
                    MyString.quote(assetRoot));
            System.exit(1);
        }
        assetManager = new DesktopAssetManager();

        // Register loaders.
        assetManager.registerLoader(AWTLoader.class, "jpg", "png");
        assetManager.registerLoader(BinaryLoader.class, "j3o");
        assetManager.registerLoader(J3MLoader.class, "j3m", "j3md");

        // Register locators.
        assetManager.registerLocator(assetRoot, FileLocator.class);
        assetManager.registerLocator(null, ClasspathLocator.class);
    }
}
