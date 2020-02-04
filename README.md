<img height="150" src="https://i.imgur.com/O7MMp7A.png">

The [Heart Project][heart] provides a general-purpose add-on library for the
[jMonkeyEngine Game Engine][jme].

It contains 3 sub-projects:

 1. HeartLibrary: the Heart runtime library and its automated tests (in Java)
 2. HeartExamples: demos, examples, and non-automated test software (in Java)
 3. HeartAssets: generate assets included in HeartLibrary (in Java)

Java source code is provided under
[a BSD license](https://github.com/stephengold/Heart/blob/master/license.txt).

Both [Minie][] and [Wes][] depend on the Heart Library.

<a name="toc"/>

## Contents of this document

 + [Important features](#features)
 + [Downloads](#downloads)
 + [Conventions](#conventions)
 + [History](#history)
 + [How to build Heart from source](#build)
 + [How to add Heart to an existing project](#add)
 + [Acknowledgments](#acks)

<a name="features"/>

## Important features

 + debugging aids:
   + `Dumper` to concisely dump a scene graph or a subtree thereof, or to
      dump appstates or viewports
   + `Validate` to validate arguments passed to a library method
   + `AxesVisualizer` to visualize the coordinate axes of a `Node`
   + `BoundsVisualizer` to visualize the world bounds of a `Spatial`
   + `PointVisualizer` to visualize a particular location in the world
   + `SkeletonVisualizer` to visualize the bones/joints of an animated model
   + `MyMesh.boneWeightMaterial()` to visualize bone weights in a `Mesh`
 + `Mesh` subclasses:
   + `Dodecahedron`, `Icosahedron`, `Octahedron`, and `Tetrahedron`
     to generate meshes for Platonic solids
   + `Cone`, `DomeMesh`, `Icosphere`, `Prism` to generate familiar 3-D shapes
   + `RectangleMesh` to generate custom quads
   + `RoundedRectangle` to generate eye-pleasing backgrounds
     for user-interface text
 + `MyMesh` utilities to analyze and manipulate JMonkeyEngine meshes:
   + compress a `Mesh` by introducing an index buffer
   + expand a `Mesh` to ensure no vertex data are re-used
   + generate normals for an outward-facing sphere, a faceted mesh,
     or a smooth mesh
   + flip mesh normals and/or triangle windings
   + enumerate all vertices in a scene-graph subtree
   + convert mesh triangles to lines
 + JME-oriented math aids:
   + generate pseudo-random quaternions and vectors
   + generate 2-D Perlin noise
   + operations on scalars, arrays, and buffers
 + loaders for `Properties` and `String` assets
 + `ContrastAdjustmentFilter`
 + an `AppState` to manage `ViewPort` updating
 + and much, much more!

[Jump to table of contents](#toc)

<a name="downloads"/>

## Downloads

Newer releases (since v4.5.0) can be downloaded from
[GitHub](https://github.com/stephengold/Heart/releases).

Older releases (v0.9.5 through v4.4.0) can be downloaded from
[the Jme3-utilities Project](https://github.com/stephengold/jme3-utilities/releases).

Newer Maven artifacts (since v5.0.0) are available from
[JCenter](https://bintray.com/stephengold/com.github.stephengold/Heart).

Older Maven artifacts (v0.9.5 through v4.5.0) are available from
[JFrog Bintray](https://bintray.com/stephengold/jme3utilities/jme3-utilities-heart).

[Jump to table of contents](#toc)

<a name="conventions"/>

## Conventions

Package names begin with `jme3utilities.`

Both the source code and the pre-built libraries are compatible with JDK 7.

[Jump to table of contents](#toc)

<a name="history"/>

## History

The oldest parts of the Heart Library were originally included in SkyControl.

From May 2017 to February 2020, the Heart Library was a sub-project of
[the Jme3-utilities Project][utilities].

Since February 2020, the Heart Project has been a separate project at
[GitHub][heart].

The evolution of the project is chronicled in
[its release notes](https://github.com/stephengold/Heart/blob/master/HeartLibrary/release-notes.md).

[Jump to table of contents](#toc)

<a name="build"/>

## How to build Heart from source

Heart currently targets Version 3.2.4 of jMonkeyEngine.
You are welcome to use the Engine without installing
its Integrated Development Environment (IDE),
but I use the IDE, so I tend to assume you will too.

### IDE setup

If you already have the IDE installed, skip to step 6.

The hardware and software requirements of the IDE are documented at
[the JME wiki](https://jmonkeyengine.github.io/wiki/jme3/requirements.html).

 1. Download a jMonkeyEngine 3.2 Software Development Kit (SDK) from
    [GitHub](https://github.com/jMonkeyEngine/sdk/releases).
 2. Install the SDK, which includes:
    + the engine itself,
    + an IDE based on [NetBeans][],
    + various IDE plugins, and
    + the [Blender 3D][blender] application.
 3. Open the IDE.
 4. The first time you open the IDE, it prompts you to
    specify a folder for storing projects:
    + Fill in the "Folder name" text box.
    + Click on the "Set Project Folder" button.
 5. The first time you open the IDE, you should update
    all the pre-installed plugins:
    + Menu bar -> "Tools" -> "Plugins" to open the "Plugins" dialog.
    + Click on the "Update" button to open the "Plugin Installer" wizard.
    + Click on the "Next >" button.
    + After the plugins have downloaded, click "Finish".
    + The IDE will restart.
 6. In order to open the Heart Project in the IDE (or NetBeans),
    you will need to install the `Gradle Support` plugin:
    + Menu bar -> "Tools" -> "Plugins" to open the "Plugins" dialog.
    + Click on the "Available Plugins" tab.
    + Check the box next to "Gradle Support" in the "Gradle" category.
     If this plugin isn't shown in the IDE's "Plugins" tool,
     you can download it from
     [GitHub](https://github.com/kelemen/netbeans-gradle-project/releases).
    + Click on the "Install" button to open the "Plugin Installer" wizard.
    + Click on the "Next >" button.
    + Check the box next to
     "I accept the terms in all the license agreements."
    + Click on the "Install" button.
    + When the "Verify Certificate" dialog appears,
     click on the "Continue" button.
    + Click on the "Finish" button.
    + The IDE will restart.

### Source files

Clone the Heart repository using Git:

 1. Open the "Clone Repository" wizard in the IDE:
     + Menu bar -> "Team" -> "Git" -> "Clone..." or
     + Menu bar -> "Team" -> "Remote" -> "Clone..."
 2. For "Repository URL:" specify
    `https://github.com/stephengold/Heart.git`
 3. Clear the "User:" and "Password:" text boxes.
 4. For "Clone into:" specify a writable folder (on a local filesystem)
    that doesn't already contain "Heart".
 5. Click on the "Next >" button.
 6. Make sure the "master" remote branch is checked.
 7. Click on the "Next >" button again.
 8. Make sure the Checkout Branch is set to "master".
 9. Make sure the "Scan for NetBeans Projects after Clone" box is checked.
10. Click on the "Finish" button.
11. When the "Clone Completed" dialog appears, click on the "Open Project..."
    button.
12. Expand the root project node to reveal the 3 sub-projects.
13. Select all sub-projects using control-click, then click on the
    "Open" button.

### Build the project

 1. In the "Projects" window of the IDE,
    right-click on the "Heart [root]" project to select it.
 2. Select "Build".

### How to build Heart without an IDE

 1. Install build software:
   + a Java Development Kit and
   + [Gradle]
 2. Download and extract the source code from GitHub:
   + using Git:
     + `git clone https://github.com/stephengold/Heart.git`
     + `cd Heart`
     + `git checkout -b latest 5.0.0for32`
   + using a web browser:
     + browse to [https://github.com/stephengold/Heart/releases/tag/5.0.0for32](https://github.com/stephengold/Heart/releases/tag/5.0.0for32)
     + follow the "Source code (zip)" link
     + save the ZIP file
     + unzip the saved ZIP file
     + `cd` to the extracted directory/folder
 3. Set the `JAVA_HOME` environment variable:
   + using Bash:  `export JAVA_HOME="` *path to your JDK* `"`
   + using Windows Command Prompt:  `set JAVA_HOME="` *path to your JDK* `"`
 4. Run the Gradle wrapper:
   + using Bash:  `./gradlew build`
   + using Windows Command Prompt:  `.\gradlew build`

After a successful build, new jars will be found in `HeartLibrary/build/libs`.

You can also install the library artifact to your local Maven cache:
 + using Bash:  `./gradlew :HeartLibrary:publishToMavenLocal`
 + using Windows Command Prompt:  `.\gradlew :HeartLibrary:publishToMavenLocal`

[Jump to table of contents](#toc)

<a name="add"/>

## How to add Heart to an existing project

Adding the Heart Library to an existing JME3 project should be
a simple matter of adding it to the classpath.

Heart comes pre-built as a single library that depends on
the standard jme3-core library from jMonkeyEngine.

#### For Gradle projects

For projects built using Maven or Gradle, it is sufficient to specify the
dependency on the Heart Library.  The build tools should automatically
resolve the remaining dependencies automatically.

    repositories {
        jcenter()
    }
    dependencies {
        compile 'com.github.stephengold:Heart:5.0.0for32'
    }

#### For Ant projects

For projects built using [Ant][], download the library:

 + https://github.com/stephengold/Heart/releases/tag/5.0.0for32

You'll want the class jar
and probably the `-sources` and `-javadoc` jars as well.

Open the project's properties in the IDE (JME 3.2 SDK or NetBeans 8.2):

 1. Right-click on the project (not its assets) in the "Projects" window.
 2. Select "Properties to open the "Project Properties" dialog.
 3. Under "Categories:" select "Libraries".
 4. Click on the "Compile" tab.
 5. Add the `jme3-utilities-heart` class jar:
    + Click on the "Add JAR/Folder" button.
    + Navigate to the "jme3-utilities" project folder.
    + Open the "heart" sub-project folder.
    + Navigate to the "build/libs" folder.
    + Select the "Heart-5.0.0for32.jar" file.
    + Click on the "Open" button.
 6. (optional) Add jars for javadoc and sources:
    + Click on the "Edit" button.
    + Click on the "Browse..." button to the right of "Javadoc:"
    + Select the "Heart-5.0.0for32-javadoc.jar" file.
    + Click on the "Open" button.
    + Click on the "Browse..." button to the right of "Sources:"
    + Select the "Heart-5.0.0for32-sources.jar" file.
    + Click on the "Open" button again.
    + Click on the "OK" button to close the "Edit Jar Reference" dialog.
 7. Click on the "OK" button to exit the "Project Properties" dialog.

[Jump to table of contents](#toc)

[ant]: https://ant.apache.org "Apache Ant Project"
[blender]: https://docs.blender.org "Blender Project"
[bsd3]: https://opensource.org/licenses/BSD-3-Clause "3-Clause BSD License"
[chrome]: https://www.google.com/chrome "Chrome"
[elements]: http://www.adobe.com/products/photoshop-elements.html "Photoshop Elements"
[findbugs]: http://findbugs.sourceforge.net "FindBugs Project"
[firefox]: https://www.mozilla.org/en-US/firefox "Firefox"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gradle]: https://gradle.org "Gradle Project"
[heart]: https://github.com/stephengold/Heart "Heart Project"
[jfrog]: https://www.jfrog.com "JFrog"
[jme]: http://jmonkeyengine.org  "jMonkeyEngine Project"
[markdown]: https://daringfireball.net/projects/markdown "Markdown Project"
[minie]: https://github.com/stephengold/Minie "Minie Project"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[wes]: https://github.com/stephengold/Wes "Wes Project"
[winmerge]: http://winmerge.org "WinMerge Project"

<a name="acks"/>

## Acknowledgments

Like most projects, the Heart Project builds on the work of many who
have gone before.  I therefore acknowledge the following
software developers:

 + "jayfella", for creating and sharing the original `Icosphere.java`
 + Paul Speed, for helpful insights
 + plus the creators of (and contributors to) the following software:
    + the [Blender][] 3-D animation suite
    + the [FindBugs][] source-code analyzer
    + the [Git][] revision-control system and GitK commit viewer
    + the [Firefox][] and [Google Chrome][chrome] web browsers
    + the [Gradle][] build tool
    + the Java compiler, standard doclet, and runtime environment
    + [jMonkeyEngine][jme] and the jME3 Software Development Kit
    + the [Linux Mint][mint] operating system
    + LWJGL, the Lightweight Java Game Library
    + the [Markdown][] document conversion tool
    + Microsoft Windows
    + the [NetBeans][] integrated development environment
    + the PMD source-code analyzer
    + the [WinMerge][] differencing and merging tool

I am grateful to [JFrog][] and [Github][] for providing free hosting for the
Heart Project and many other open-source projects.

I'm also grateful to my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know so I can
correct the situation: sgold@sonic.net

[Jump to table of contents](#toc)