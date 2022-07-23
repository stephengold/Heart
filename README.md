<img height="150" src="https://i.imgur.com/O7MMp7A.png" alt="Heart Project logo">

[The Heart Project][heart] provides a general-purpose add-on library for
[the jMonkeyEngine (JME) game engine][jme].

It contains 3 sub-projects:

1. HeartLibrary: the Heart runtime library and its automated tests
2. HeartExamples: demos, examples, and non-automated test software
3. J3oDump: a command-line utility to dump J3O assets

Complete source code (in Java) is provided under
[a BSD 3-Clause license][license].

Many other libraries depend on the Heart Library, including
[Acorus], [jme3-wireframe], [JmePower], [Minie], [SkyControl], and [Wes].


<a name="toc"></a>

## Contents of this document

+ [Important features](#features)
+ [How to add Heart to an existing project](#add)
+ [How to build Heart from source](#build)
+ [Downloads](#downloads)
+ [Conventions](#conventions)
+ [An overview of the example applications](#examples)
+ [External links](#links)
+ [History](#history)
+ [Acknowledgments](#acks)


<a name="features"></a>

## Important features

+ debugging aids:
  + `Dumper` to concisely dump a scene graph or a subtree thereof, or to
     dump appstates or viewports
  + `Validate` to validate arguments passed to a library method
  + `AxesVisualizer` to visualize the coordinate axes of a `Node`
  + `BoundsVisualizer` to visualize the world bounds of a `Spatial`
  + `PointVisualizer` to visualize a particular location in the world
  + `SkeletonVisualizer` to visualize the bones/joints of an animated model
  + `VectorVisualizer` to visualize a vector
  + `MyAsset.createDebugMaterial()` to visualize mesh normals
     with or without gamma correction
  + `MyMesh.boneWeightMaterial()` to visualize bone weights in a `Mesh`
+ `Mesh` subclasses:
  + `Dodecahedron`, `Icosahedron`, `Octahedron`, and `Tetrahedron`
    to generate meshes for Platonic solids
  + `Cone`, `DomeMesh`, `Icosphere`, `Octasphere`, and `Prism`
    to generate familiar 3-D shapes
  + `RectangleMesh` to generate custom quads
  + `DiscMesh` and `RoundedRectangle` to generate eye-pleasing backgrounds
    for user-interface text
+ `MyMesh` utilities to analyze and manipulate JMonkeyEngine meshes:
  + compress a `Mesh` by introducing an index buffer
  + expand a `Mesh` to ensure no vertex data are re-used
  + generate normals for an outward-facing sphere, a faceted mesh,
    or a smooth mesh
  + translate and uniformly scale all vertices of a `Mesh`
  + merge 2 meshes into one
  + flip mesh normals and/or triangle windings
  + enumerate all vertices in a scene-graph subtree
  + convert mesh triangles to lines
+ JME-oriented math:
  + generate pseudo-random quaternions and vectors
  + interpolate and cardinalize quaternions and vectors
  + other useful operations on scalars, quaternions, vectors,
    arrays, and buffers
  + generate 2-D Perlin noise
+ loaders for `Properties` and `String` assets
+ `ContrastAdjustmentFilter`
+ an `AppState` to manage `ViewPort` updating
+ and much, much more!

[Jump to table of contents](#toc)


<a name="add"></a>

## How to add Heart to an existing project

Heart comes pre-built as a library that depends on
the standard "jme3-core" library from jMonkeyEngine.
Adding Heart to an existing [jMonkeyEngine][jme] project should be
a simple matter of adding this library to the classpath.

For projects built using [Maven] or [Gradle], it is sufficient to add a
dependency on the Heart Library.  The build tools should automatically
resolve the remaining dependencies.

### Gradle-built projects

Add to the project’s "build.gradle" file:

    repositories {
        mavenCentral()
    }
    dependencies {
        implementation 'com.github.stephengold:Heart:8.1.0'
    }

For some older versions of Gradle,
it's necessary to replace `implementation` with `compile`.

### Maven-built projects

Add to the project’s "pom.xml" file:

    <repositories>
      <repository>
        <id>mvnrepository</id>
        <url>https://repo1.maven.org/maven2/</url>
      </repository>
    </repositories>

    <dependency>
      <groupId>com.github.stephengold</groupId>
      <artifactId>Heart</artifactId>
      <version>8.1.0</version>
    </dependency>

### Ant-built projects

For projects built using [Ant], download the library from GitHub:

+ https://github.com/stephengold/Heart/releases/latest

You'll want the class jar
and probably the `-sources` and `-javadoc` jars as well.

Open the project's properties in the IDE (JME 3.2 SDK or NetBeans 8.2):

1. Right-click on the project (not its assets) in the "Projects" window.
2. Select "Properties" to open the "Project Properties" dialog.
3. Under "Categories:" select "Libraries".
4. Click on the "Compile" tab.
5. Add the Heart class jar:
  + Click on the "Add JAR/Folder" button.
  + Navigate to the download folder.
  + Select the "Heart-8.1.0.jar" file.
  + Click on the "Open" button.
6. (optional) Add jars for javadoc and sources:
  + Click on the "Edit" button.
  + Click on the "Browse..." button to the right of "Javadoc:"
  + Select the "Heart-8.1.0-javadoc.jar" file.
  + Click on the "Open" button.
  + Click on the "Browse..." button to the right of "Sources:"
  + Select the "Heart-8.1.0-sources.jar" file.
  + Click on the "Open" button again.
  + Click on the "OK" button to close the "Edit Jar Reference" dialog.
7. Click on the "OK" button to exit the "Project Properties" dialog.

[Jump to table of contents](#toc)


<a name="build"></a>

## How to build Heart from source

1. Install a [Java Development Kit (JDK)][adoptium],
   if you don't already have one.
2. Point the `JAVA_HOME` environment variable to your JDK installation:
   (The path might be something like "C:\Program Files\Java\jre1.8.0_301"
   or "/usr/lib/jvm/java-8-openjdk-amd64/" or
   "/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home" .)
  + using Bash or Zsh: `export JAVA_HOME="` *path to installation* `"`
  + using Windows Command Prompt: `set JAVA_HOME="` *path to installation* `"`
  + using PowerShell: `$env:JAVA_HOME = '` *path to installation* `'`
3. Download and extract the Heart source code from GitHub:
  + using Git:
    + `git clone https://github.com/stephengold/Heart.git`
    + `cd Heart`
    + `git checkout -b latest 8.1.0`
  + using a web browser:
    + browse to [the latest release][latest]
    + follow the "Source code (zip)" link
    + save the ZIP file
    + extract the contents of the saved ZIP file
    + `cd` to the extracted directory/folder
4. Run the [Gradle] wrapper:
  + using Bash or PowerShell or Zsh: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

After a successful build,
Maven artifacts will be found in "HeartLibrary/build/libs".

You can install the artifacts to your local Maven repository:
+ using Bash or PowerShell or Zsh: `./gradlew install`
+ using Windows Command Prompt: `.\gradlew install`

You can restore the project to a pristine state:
+ using Bash or PowerShell or Zsh: `./gradlew clean`
+ using Windows Command Prompt: `.\gradlew clean`

[Jump to table of contents](#toc)


<a name="downloads"></a>

## Downloads

Newer releases (since v4.5.0) can be downloaded from
[GitHub](https://github.com/stephengold/Heart/releases).

Older releases (v0.9.5 through v4.4.0) can be downloaded from
[the Jme3-utilities Project](https://github.com/stephengold/jme3-utilities/releases).

Newer Maven artifacts (since v6.3.0) are available from
[MavenCentral](https://search.maven.org/artifact/com.github.stephengold/Heart).

Old Maven artifacts (v5.0.0 through v6.4.0) are available from JCenter.

[Jump to table of contents](#toc)


<a name="conventions"></a>

## Conventions

Package names begin with `jme3utilities`.

The source code and pre-built libraries are compatible with JDK 8.

[Jump to table of contents](#toc)


<a name="examples"></a>

## An overview of the example applications

(This section is under construction.)

Applications have been created to test and demonstrate
certain features of Heart.
The following apps are found in the HeartExamples sub-project:

### TestBoundsVisualizer

### TestSkeletonVisualizer

### LoopMeshTest

### TestSolidMeshes

### TestMergeMeshes

### TestContrast

### TestWireframe

[Jump to table of contents](#toc)


<a name="links"></a>

## External links

+ [the Heart page](https://store.jmonkeyengine.org/e534fc1e-5b78-46b7-a831-d5d40cbd9dcd)
  at [JmonkeyStore](https://store.jmonkeyengine.org)

[Jump to table of contents](#toc)


<a name="history"></a>

## History

The evolution of this project is chronicled in
[its release log][log].

The oldest parts of the Heart Library were originally included in [SkyControl].

From May 2017 to February 2020, Heart was a sub-project of
[the Jme3-utilities Project][utilities].

Since February 2020, Heart has been a separate project, hosted at
[GitHub][heart].

[Jump to table of contents](#toc)


<a name="acks"></a>

## Acknowledgments

Like most projects, the Heart Project builds on the work of many who
have gone before.  I therefore acknowledge the following
software developers:

 + "jayfella", for creating and sharing the original `Icosphere.java`
 + Paul Speed, for helpful insights
 + "rvandoosselaer", for reporting and fixing issue #2.
 + plus the creators of (and contributors to) the following software:
    + the [Blender] 3-D animation suite
    + the [Checkstyle] tool
    + the [FindBugs] source-code analyzer
    + the [Firefox] and [Chrome] web browsers
    + the [Git] revision-control system and GitK commit viewer
    + the [GitKraken] client
    + the [Gradle] build tool
    + the [IntelliJ IDEA][idea] and [NetBeans] integrated development environments
    + the [Java] compiler, standard doclet, and runtime environment
    + [jMonkeyEngine][jme] and the jME3 Software Development Kit
    + the [Linux Mint][mint] operating system
    + LWJGL, the Lightweight Java Game Library
    + the [Markdown] document-conversion tool
    + the [Meld] visual merge tool
    + Microsoft Windows
    + the PMD source-code analyzer
    + the [WinMerge] differencing and merging tool

I am grateful to [GitHub], [Sonatype], [JFrog], and [Imgur]
for providing free hosting for this project
and many other open-source projects.

I'm also grateful to my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know, so I can
correct the situation: sgold@sonic.net

[Jump to table of contents](#toc)


[acorus]: https://github.com/stephengold/Acorus "Acorus Project"
[adoptium]: https://adoptium.net/releases.html "Adoptium Project"
[ant]: https://ant.apache.org "Apache Ant Project"
[blender]: https://docs.blender.org "Blender Project"
[checkstyle]: https://checkstyle.org "Checkstyle"
[chrome]: https://www.google.com/chrome "Chrome"
[findbugs]: http://findbugs.sourceforge.net "FindBugs Project"
[firefox]: https://www.mozilla.org/en-US/firefox "Firefox"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gitkraken]: https://www.gitkraken.com "GitKraken client"
[gradle]: https://gradle.org "Gradle Project"
[heart]: https://github.com/stephengold/Heart "Heart Project"
[idea]: https://www.jetbrains.com/idea/ "IntelliJ IDEA"
[imgur]: https://imgur.com/ "Imgur"
[java]: https://java.com "Java"
[jfrog]: https://www.jfrog.com "JFrog"
[jme]: https://jmonkeyengine.org  "jMonkeyEngine Project"
[jme3-wireframe]: https://github.com/joliver82/jme3-wireframe "jME3 Wireframe render library"
[jmepower]: https://github.com/stephengold/JmePower "JmePower Project"
[latest]: https://github.com/stephengold/Heart/releases/latest "latest release"
[license]: https://github.com/stephengold/Heart/blob/master/license.txt "Heart license"
[log]: https://github.com/stephengold/Heart/blob/master/HeartLibrary/release-notes.md "release log"
[markdown]: https://daringfireball.net/projects/markdown "Markdown Project"
[maven]: https://maven.apache.org "Maven Project"
[meld]: https://meldmerge.org "Meld Tool"
[minie]: https://github.com/stephengold/Minie "Minie Project"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[skycontrol]: https://github.com/stephengold/SkyControl "SkyControl Project"
[sonatype]: https://www.sonatype.com "Sonatype"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[wes]: https://github.com/stephengold/Wes "Wes Project"
[winmerge]: https://winmerge.org "WinMerge Project"
