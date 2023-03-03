# release log for the Heart Library and related tests

## Version 8.3.1+for36 released on 2 March 2023

+ Deprecated the `insertAt()` method in the `MyControl` class.
+ Updated 8 textures from the Georg project.
+ Targeted jMonkeyEngine version 3.6.0-beta3.
+ Updated Gradle to v8.0.1

## Version 8.3.0+for36 released on 29 January 2023

+ Bugfix:  `MyQuaternion.isRotationIdentity()` accepts `NaN`s
+ Changed `Describer` to describe scene-graph controls in order
  and omit any disabled ones.
+ In `MyMesh`, renamed `generateNormals()` to `generateFacetNormals()`
  and deprecated the old method name.
+ Added 5 new methods:
  + `MyMath.fromAngles()`
  + `MyMesh.hasAnyTangents()`
  + `MyMesh.hasTangents()`
  + `RectangularSolid.centerLocal()`
  + `RectangularSolid.centerWorld()`
+ Added `TestMyVolume` to the automated tests.
+ Targeted jMonkeyEngine version 3.6.0-beta1.
+ Updated Gradle to v7.6

## Version 8.2.0 released on 5 September 2022

+ Bugfix:  `AppChooser` executes itself instead of the selected app
+ Deprecated the `findIndex()` method in the `MySpatial` class.
+ Added methods:
  + `MyControl.insertAt()`
  + `MyLight.clearLocalLights()`
  + `MyMath.transformInverse()` for triangles
+ Added the `root2` constant to the `MyMath` class.
+ Overrode the `toString()` methods of the `VectorSetUsingBuffers`
  and `VectorSetUsingCollection` classes.
+ Updated Gradle to v7.5.1

## Version 8.1.0 released on 22 July 2022

+ Bugfix:  `MyMesh` ignores bind-pose normals
+ Added methods:
  + constructor for `Prism` to produce prisms closed with right triangles
  + `Element.lerp()`
  + `Element.lerp3()`
  + `MyBuffer.listNeighbors()`
  + `MyMath.areWithinTolerance()`
  + `MyMath.lerp3()`
  + `MyMesh.rotate()`
  + `MyMesh.subdivideLines()`
  + `MyMesh.subdivideTriangles()`
  + `MyQuaternion.angleBetween()`
  + `MyVector3f.areWithinTolerance()`
+ De-privatized the `Describer.describeHalfExtents()` method.
+ Added the `TestRotate` and `TestSubdivide` applications.
+ Converted the `TestNameGenerator` and `TestMySpatial` applications
   into JUnit tests.
+ Updated Gradle to v7.5

## Version 8.0.0 released on 22 June 2022

+ Deleted 5 deprecated methods: (API changes)
  + `MyBuffer.getFormat()`
  + `MyBuffer.putRelative()`
  + `MyMath.isFinite()`
  + `MyMesh.readIndex()`
  + `MyString.join8()`
+ Made `MyBuffer.isAllFinite()` a static method. (API change)
+ Redefined 2 `Generator.pick()` methods using generics. (API changes)
+ Tweaked the signature of the `MyVector3f.doCoincide()` method. (API change)
+ Protected the `SubtreeControl` constructor. (API change)
+ Privatized the `Generator.serialVersionUID` field. (API change)

+ Added the `addAll()` method to the `VectorSet` class.
+ Added the `axisIndex()` method to the `Validate` class.
+ Added the `hasStoredSettings()` method to the `Heart` class.
+ Add 4 vertices to `Octasphere`, in order to better cover the U-V space.
+ Began indicating `Action` subclasses in dumps.
+ Added the `AppChooser` application.
+ Added the "checkstyle" plugin to the build.

## Version 7.6.0 released on 29 April 2022

+ Added the `deleteStoredSettings()` method to the `Heart` class.
+ Added the `renderColor()` method to the `MyColor` class.
+ Enhanced `Heart.deepCopy()` to handle a null argument.
+ Improved dumpers to describe shadow renderers
  and morph controls in greater detail.
+ Converted examples to extend `AbstractDemo` and made them resizable.
+ Targeted jMonkeyEngine version 3.5.2-stable.
+ Updated Gradle to v7.4.2

## Version 7.5.0 released on 31 March 2022

+ Added 3 classes:  `ClothGrid`, `DividedLine`, and `RectSizeLimits`.
+ Added a `Validate.nonEmpty()` method for `int` arrays.

## Version 7.4.1 released on 6 March 2022

Bugfix:  binaries built using JDK 11 aren't compatible with Java 8

## Version 7.4.0 released on 5 March 2022

+ Added methods:
  + no-arg constructors for `ContrastAdjustmentFilter` and `UncachedKey`
  + `Heart.parseAppArgs()` (an aid to portability)
  + `MyBuffer.rotateBinormals()`
  + `Describer.describeAudioNode()`
  + `MyArray.countNeSorted()`
  + `NamedAppState.getInfluence()`
+ `Dumper` enhancements:
  + Dump IDs of appstates, even those not based on `AbstractAppState`.
  + Dump the influence lists of named appstates.
  + Added special handling for audio nodes.
+ Improved the examples:
  + Bugfix:  unable to run examples from the command line
  + Added command-line arguments to adjust the logging level
    and bypass the Settings Dialog. (for portability)
  + Disabled OpenGL "compatibility" profile. (for portability)
  + Added LWJGL natives for MacOSX_ARM64. (for portability)
+ Updated Gradle to v7.4

## Version 7.3.0 released on 7 February 2022

+ Bugfix:  `ClassCastException` in `MyMesh.vertexColor()` for certain meshes
+ Bugfix:  `AssertionError` in `SimpleAppState.refreshCachedFields()`
  when no `FlyByCamera` is present
+ Deprecated 3 Java7-compatibility methods:
  + `MyMath.isFinite(double)`
  + `MyMath.isFinite(float)`
  + `MyString.join8()`
+ Added the `InitialState` enum and alternative constructors
  for the `NamedAppState` and `SimpleAppState` classes.
+ Added 4 methods:
  + `Heart.getKeyInput()`
  + `MyArray.isSorted(int[])`
  + `MyBuffer.fill()`
  + `MyVector3f.rejection()`
+ Added vertex colors to mesh descriptions.
+ Changed how an `AnimComposer` is described.
+ Tests:
  + Converted test applications to automated tests.
  + Added the `TestMergeMeshes` test app.
  + Upgraded 2 test apps to use the `AbstractDemo` class from Acorus.

## Version 7.2.0 released on 23 January 2022

+ Bugfix:  can't de-serialize a `SkeletonMesh`
+ Added the `VectorVisualizer` class for debug visualization.
+ Added the `translate()` method to the `MyMesh` class.
+ Added the `velocity()` method to the `MyVector3f` class.
+ Added the `getCollisionTree()` and `transformBuffer()` methods
  to the `MyBuffer` class.
+ More detailed descriptions of controls used in the new animation system.
+ Targeted jMonkeyEngine version 3.5.0-stable and Java 8+.
+ Updated Gradle to v7.3.3

## Version 7.1.0 released on 9 September 2021

+ Bugfix: `maxNumWeights` isn't initialized by `MyMesh.merge()`
+ Bugfix: instanced meshes are double-counted by `MySpatial.countVertices()`
+ Bugfix: `MyVector3f.maxAbs()` doesn't set the Z component
+ Bugfix: a logic error in one of the `RectangleMesh` constructors
+ Disabled mergers of meshes that have levels of detail.
+ Deprecated `MyBuffer.getFormat()` and `MyBuffer.putRelative()`.
+ Extended `MyAnimation.describeTrackType()` to handle
  the new animation system.
+ Added the `DistinctVectorValues`, `MeshNormals`, and `Population` classes.
+ Added a `Comparable` interface to the `IntPair` class.
+ Added an `isAllFinite()` method to the `MyBuffer` class.
+ Added `isFiniteDouble()` and `maxInt()` methods to the `MyMath` class.
+ Added `countNe()` and `mirrorAxis()` methods to the `MyQuaternion` class.
+ Updated Gradle to v7.2

## Version 7.0.0 released on 14 July 2021

+ Changed the return type of `Heart.deepCopy()`. (API change!)
+ Deleted `defaultAnisotropicFilter()` and `isAlphaToCoverage()`
  from the `MyRender` class. (API change!)
+ Privatized 4 fields in the `VectorSetUsingBuffer` class. (API change!)
+ Copied the `readIndex()` method from `MyMesh` to `MyBuffer`
  and deprecated it.
+ Added an `isFinite()` method to the `MyMath` class.
+ Added `centerBuffer()`, `expandedMode()`, `merge()`, `rotateBuffer()`,
  and `rotateTangentBuffer()` methods to the `MyMesh` class.
+ Added a `listTextures()` method to the `MySpatial` class.
+ Added a `join8()` method to the `MyString` class.
+ Upgraded Gradle to v7.1.1

## Version 6.4.4 released on 30 May 2021

+ Targeted jMonkeyEngine version 3.4.0-stable.
+ Replaced HeartTextures with textures from the Georg project.
+ Anti-aliased the "shapes" textures by downsampling them 10x.
+ Upgraded Gradle to v7.0.2

## Version 6.4.3+for34 released on 22 April 2021

+ Bugfix: Locale not specified in the `MyString.describe()`
  and `MyString.describeFraction()` methods
+ Made `MyMath.hypoteneuse()` more reliable by using double-precision
  arithmetic.
+ Targeted jMonkeyEngine version 3.4.0-beta1.
+ Upgraded the JUnit library to v4.13.2
+ Upgraded Gradle to v7.0

## Version 6.4.2 released on 9 February 2021

+ Bugfix: single-precision arithmetic in `MyMath.sumOfSquares()`
+ Published to MavenCentral instead of JCenter.
+ Upgraded Gradle to v6.8.2

## Version 6.4.0 released on 30 January 2021

Added the `Loadable` interface.

## Version 6.3.0 released on 23 January 2021

+ Bugfix: `IllegalArgumentException` while expanding a `Mesh`.
+ Extended `Heart.writeImage()` to write BMPs, GIFs, and JPEGs.
+ Added an option to `Validate` to configure the type of exception
  thrown for a null argument.
+ Modified `PointVisualizer` to configure its render bucket
  and disable shadows.
+ Reduced argument validation when assertions are disabled.
+ Added to the library:
  + `Dumper.dump()` methods for a `Camera`
  + `MyAnimation.findKeyframeIndex()` for a `TransformTrack`
  + `MyCamera.fovX()`
  + `MyCamera.xDegrees()`
  + `MyCamera.xTangent()`
  + `MyMath.clamp()` for ints
  + `MyMath.easeInQuartic()`
  + `MyMath.easeOutQuartic()`
  + `MyString.findIndex()`
  + a `Vector3i` class
+ Publicized many loggers.
+ Upgraded Gradle to v6.8.1

## Version 6.2.0 released on 23 November 2020

+ Added a `SignalTracker` class to the library.
+ Upgraded Gradle to v6.7.1

## Version 6.1.0 released on 15 November 2020

+ Added to the library:
  + `MyAnimation.findPreviousKeyframeIndex()`
  + `MyAnimation.getKeyFrameTimes()`
  + `MySkeleton.getTargetGeometry()`
  + `MySkeleton.listArmatures()`
  + `PointVisualizer.setDepthTest()`
  + the `DiscMesh` class
+ Improved the examples:
  + disabled audio rendering
  + upgraded the jme3-utilities-ui library to v0.9.0
+ Upgraded Gradle to v6.7
+ Upgraded JUnit to v4.13.1

## Version 6.0.0 released on 15 August 2020

+ Added a "gamma" argument to `createDebugMaterial()`
  in the `MyAsset` class. (API change)
+ Removed or privatized the `phi` constants in 3 mesh classes. (API change)
+ Added a `clear()` method to the `VectorSet` interface. (API change)
+ Added a return value to the `validateUnit()` method
  in the `MyQuaternion` class.
+ Added a U-V seam to `Octasphere` so it can display textures without glitches.
+ Added a `listGeometries()` convenience method to the `MySpatial` class.
+ Added an `Octasphere` value to the `SphereMeshes` enum.

## Version 5.5.0 released on 26 May 2020

+ Added an `Octasphere` class.
+ Added public constants `farZ` and `nearZ` to the `MyCamera` class.
+ Added a "Textures/shapes/pin.png" asset.

## Version 5.4.0 released on 16 May 2020

+ Added public constants `AxesVisualizer.widthForSolid` and `MyMath.phi`.
+ Added a `getFileHandler()` method to the `Heart` class.
+ Added a `listAnimationSpatials()` method to the `MySpatial` class.
+ Added a `nextQuaternion()` method with caller-allocated storage
  to the `Generator` class.

## Version 5.3.0 released on 27 April 2020

+ Deprecated the `defaultAnisotropicFilter()` and `isAlphaToCoverage()`
  methods in the `MyRenderer` class.
+ Added an `isPure()` method to the `MyQuaternion` class.
+ Targeted jMonkeyEngine version 3.3.2-stable.

## Version 5.2.1 released on 1 April 2020

Targeted jMonkeyEngine version 3.3.0-stable.

## Version 5.2.0for33 released on 26 March 2020

+ Bugfix: shader crashes on macOS with OpenGL3.2 core profile.
+ Changed the default line widths in `BoundsVisualizer`
  and `SkeletonVisualizer` to 1.0, since that's the only value
  for which OpenGL guarantees support.
+ Added `clear()` methods to `VectorSetUsingBuffer`
  and `VectorSetUsingCollection`.
+ Added 3 test apps: `TestContrast`, `TestSkeletonVisualizer`,
  and `TestMulticolor`.
+ Targeted jMonkeyEngine version 3.3.0-beta2.

## Version 5.1.0for33 released on 5 March 2020

+ Bugfix: logic error in `Validate.positive()` for `Vector3f` argument.
+ Added `getFormat()` and `putRelative()` methods to the `MyBuffer` class.
+ Added an `addLine()` method to the `Dumper` class.

## Version 5.0.0for33 released on 3 February 2020

+ Changed the Maven groupId from `jme3utilities` to `com.github.stephengold`.
+ Changed the Maven artifactId from `jme3-utilities-heart` to `Heart`.
+ Deleted the deprecated `Misc` class. (API change!)
+ Deleted the deprecated `copyLineColor()` method
  from the `BoundsVisualizer` class. (API change!)
+ Deleted the deprecated `isSorted(List<Comparable>)` method
  from the `MyMath` class. (API change!)
+ Added 4 methods to the `VectorSet` interface: (API changes!)
  + `add(float, float, float)`
  + `contains(float, float, float)`
  + `toFloatArray()`
  + `toVectorArray()`
+ Added an option to the `VectorSetUsingBuffer` constructor to use wrapped
  arrays instead of direct buffers. (API change!)
+ Generalized the `hypotenuse()`, `max()`, `min()`, and `sumOfSquares()`
  methods in the `MyMath` class to accept
  variable numbers of arguments. (API changes!)
+ Added a `normalizeLocal()` method to the `MyQuaternion` class.
+ When dumping an `IndexBuffer`, organize the indices into lines or triangles.

## Version 4.5.0for33 released on 1 February 2020

+ Bugfix: `Icosphere` lacked a no-arg constructor.
+ Bugfix: `Tetrahedron` generated an irregular polyhedron.
+ Copied `Misc` class to `Heart`, renamed `Heart.writeMap()`,
  and deprecated the `Misc` class.
+ Added a `require()` method to the `Validate` class.
+ Added `setBoneIndexBuffer()` and `trianglesToLines()` methods
  to the `MyMesh` class.
+ Added an `IntPair` class.
+ Added `isSorted()` methods for floats and Strings and deprecated the
  one for `List<Comparable>`.
+ Added a test `TestCloneMeshes`.
+ Handle `mode=Points` in the `Dodecahedron` constructor.
+ Heart moved from the Jme3-utilities Project to a new GitHub repo.

## Version 4.4.0for33 released on 24 January 2020

+ Bugfix: `Generator.nextQuaternion()` returned unnormalized values.
+ Added `Dodecahedron` and `SphereMeshes` classes.
+ Incorporated the `PropertiesLoader` class from the UI library.
+ Added `getSubject()`, `setSphereType()`, and `sphereType()` methods
  to the `BoundsVisualizer` class.
+ Added `nextFloat()` and `nextInt()` methods for ranges
  to the `Generator` class.
+ Added a radius constructor to the `LoopMesh` class.
+ Added a `writeJ3O()` method to the `Misc` class.
+ Added an `isSorted(Comparable[])` method to the `MyArray` class.
+ Added an `isSorted(List)` method to the `MyMath` class.
+ Added `addSphereNormals()`, `hasTriangles()`, and `smoothNormals()` methods
  to the `MyMesh` class.
+ Added a public constant `vpe` to the `MyMesh` class.
+ Added `TestBoundsVisualizer` and `TestGenerator` applications
  to the tests sub-project.
+ Improved descriptions for `float` values.
+ Added JME spheres and a textured material
  to the `TestSolidMeshes` application.
+ Simplified the `LoopMesh` class, including deletion of its index buffer.

## Version 4.3.0for33 released on 4 January 2020

+ Bugfix: wrong mode in `LoopMesh`.
+ Added `addIndices()`, `areIdentical()`, `expand()`, `generateNormals()`,
  `hasIndices()`, `reverseNormals()`, and `reverseWinding()` methods
   to the `MyMesh` class.
+ Added the `Cone`, `Element`, `Icosahedron`, `Octahedron`, `Prism`, and
  `Tetrahedron` classes.
+ Added `get()`, `normalize()`, `put()`, `rotate()`, and `scale()` methods to
  the `MyBuffer` class.
+ Added a `Dumper` option to dump mesh-vertex data.
+ Improved dumping of meshes, including number of vertices.
+ Changed default list separator to " " in the `Describer` class.
+ Added a `nonNegative()` method for `Vector2f` arguments to the `Validate`
  class.
+ Implemented JME serialization for visualizer classes.
+ Publicized the `vpt` field of the `MyMesh` class.
+ Added caller-provided storage to `BoundsVisualizer.copyLineColor()`.
+ Added `TestSolidMeshes` application to the tests sub-project.
+ Added `TestCloneControls` class to the automated test suite.
+ Targeted the NEW jMonkeyEngine version 3.3.0-beta1.

## Version 4.2.0for33 released on 8 December 2019

+ Added a single-argument `listSpatials()` method to the `MySpatial` class.
+ Targeted jMonkeyEngine version v3.3.0-beta1, which was later deleted!

## Version 4.1.0for33 released on 5 October 2019

+ Added `addAllInfluencers()` and `hideNonInfluencers()` methods for the new
  animation system to the `InfluenceUtil` class.
+ Added `findJointTrack()`, `findTransformTrack()`, and `isJointTrack()`
  methods to the `MyAnimation` class.
+ Added a `positive()` method for `Vector3f` arguments to the `Validate` class.
+ Publicized 3 methods in the `InfluenceUtil` class.

## Version 4.0.0for33 released on 23 September 2019

+ Protected no-arg constructors used only for serialization. (API changes!)
+ Deleted the `traverse()` method from `SubtreeControl`. (API change!)
+ Privatized the `subtree` field of a `SubtreeControl`. (API change!)
+ Based `NamedAppState` on `AbstractAppState`. (API change!)
+ Targeted jMonkeyEngine version 3.3.0-alpha5.
+ Added `Icosphere` and `InfluenceUtil` classes.
+ Added `distinct()`, `ensureCapacity()`, `frequency()`, `toFloatArray()`,
  and `toIntArray()` methods to the `MyBuffer` class.
+ Added a constructor for a `RectangularSolid` that takes an `AbstractBox`.
+ Changed `SubtreeControl` so the subtree can be a `Geometry`.
+ Changed `Dumper` to dump the ID of each `AbstractAppState`.
+ Changed `NamedAppState` so it doesn't require a `SimpleApplication`.

## Version 3.0.0for33 released on 25 August 2019

+ Deleted deprecated methods. (API changes!)
+ Added a 3-argument `sumOfSquares()` method to the `MyMath` class.
+ Added a `dot()` method to the `MyQuaternion` class.
+ Added an `isBetween()` method to the `MyVector3f` class.
+ Added a `lerp()` method to the `MyColor` class.
+ Added a `nextPoisson()` method to the `Generator` class.
+ Added a `createMulticolor2Material()` method to the `MyAsset` class.
+ Added caller-provided storage options to 2 `Generator` methods.
+ Describe the `BlendMode` of a `RenderState`.
+ Strengthened argument validation.

## Version 2.31.0for33 released on 6 August 2019

+ When dumping a parented `Spatial`, flag it if it's not indented.
+ Changed all `Validate` methods to return `true`, so they can be used
  in assertions.
+ Added support for `IntBuffer` in `MyMesh.readIndex()`.
+ Added a `MyBuffer` utility class.
+ Added a `RectangularSolid` constructor that takes a `FloatBuffer`.
+ Added `maxAbs()` methods to the `MyVector3f` and `VectorSet` classes.
+ Added a `nonZero()` method (for quaternions) and a `nonNullArray()` method
  to the `Validate` class.
+ Added a `hasNormals()` method to the `MyMesh` class.
+ Added an `isZero()` method to the `MyQuaternion` class.
+ Added 2 methods using caller-allocated storage to the `ReadXZ` class.
+ Avoided some unnecessary modifications of buffer positions in `MyMesh`.

## Version 2.29.0for33 released on 5 July 2019

+ Removed the `PoseDemo` application.
+ Targeted jMonkeyEngine version 3.3.0-alpha2.
+ Added support for the `com.jme3.anim` animation system to
  `MySkeleton` and `SkeletonVisualizer`.
+ Terminate each `AppState` dump with a newline.
+ Specify `Locale` for portable `toLowerCase()`.
+ Added a `Dumper` flag to describe the world bounds of spatials.
+ Dump the text of a `BitmapText`.
+ Added `Describer` methods for bounding volumes.
+ Added `firstToLower()` and `invert()` methods to the `MyString` class.
+ Added an `aabb()` method to the `MyArray` class.
+ Added names to generated materials.

## Version 2.28.1 released on 7 June 2019

+ Bugfix: NPE while describing a buffer with no format.
+ Made the default build Java 7 compatible.
+ Deprecated 9 unused utility methods:
  + `MyArray.countDistinct()`
  + `MyArray.hasDistinct()`
  + `MyArray.normalize()`
  + `MyMath.localizeRay()`
  + `MyMesh.maxWeight()`
  + `MySkeleton.worldLocation()`
  + `MySpatial.adopt()`
  + `MySpatial.yLevel()`
  + `MyString.areLexOrdered()`

## Version 2.28.0 released on 28 May 2019

+ Added a `quoteName()` method to the `MyString` class.
+ Added a method to `Dumper` to dump a `ViewPort` without specifying
  indentation.
+ Minor improvements to dump formatting.

## Version 2.27.0 released on 29 April 2019

+ Added a `mipmaps` argument to `MyAsset.loadTexture()`.
+ Provided more detailed descriptions of vertex buffers in `Describer`.
+ Tweaked the `SkeletonVisualizer` class to generate less garbage.

## Version 2.26.0 released on 20 April 2019

+ In dumps, sort material parameters by name.
+ Added a `MyRender` class.
+ Improved descriptions of material parameters.
+ Added `Renderer` information when dumping a `RenderManager`.
+ Added a `getRate()` method to the `TimeOfDay` class.

## Version 2.25.0 released on 15 April 2019

+ Added `fixedPath()` and `fixPath()` methods to the `Misc` class.
+ Made several improvements to describe/dump formatting.

## Version 2.24.0 released on 28 March 2019

+ Bugfix: `MyString.trimFloat()` doesn't recognize scientific notation.
+ Added a `repeat()` method to the `MyString` class.
+ Added `mid()` methods to the `MyMath` class.
+ Omit zero components in the `MyVector3f.describe()` method.
+ Initiated unit testing.

## Version 2.23.0 released on 18 March 2019

+ Added an `axisIndex()` method to the `MyString` class
  and deprecate `Describer.describeAxis()`.
+ Added a float[]-based constructor for `RectangularSolid`.
+ Added `covarianceVector3f()` and `meanVector3f()` methods to
  the `MyArray` class.
+ Handled the `TerrainQuad` case in `MySpatial.describeType()`.
+ Tweaked output formatting in `MyCamera.describe()`, `Dumper`,
  and `Describer`.
+ Targeted jMonkeyEngine version 3.2.3-stable

## Version 2.22.0 released on 11 March 2019

Added getters for the fields of a `Dumper`.

## Version 2.21.0 released on 10 March 2019

+ The `jme3-utilities-debug` library merged into the
  `jme3-utilities-heart` library.
  All classes in the `jme3utilities.debug` package except `DebugVersion`
  were transferred to `jme3-utilities-heart`.
  All resources in the `Textures.shapes` package and the
  `Models.indicators.arrow` package were also transferred.
+ Changed `MyVector3f.midpoint()` and `MyVector3f.projection()` to accept
  caller-allocated storage.
+ Added a `first()` method to the `Misc` class.
+ Added a special case for uniform-scaling in `MyVector3f.describe()`.
+ Changed `MyString` to accept `CharSequence` arguments in
  `escape()`, `quote()` and `unescape()`.
+ Made argument validation more friendly in `NameGenerator`.

## Version 2.20.0 released on 18 February 2019

Added `localToWorld(Quaternion)` and
`RectangularSolid(RectangularSolid, Vector3f)` methods to the
`RectangularSolid` class.

## Version 2.19.0 released on 17 February 2019

+ Moved the `countDistinct()` and `countNe()` methods from `MyVector3f` to the
  `MyArray` class.
+ Renamed `MySkeleton.numLeafBones()` to `countLeafBones()`.
+ Renamed `MySkeleton.numRootBones()` to `countRootBones()`.
+ Added a `createDebugMaterial()` method to the `MyAsset` class.
+ Added `SkeletonControl` skinning modes to `MyControl.describe()`.
+ Added a `boneWeightMaterial()` method to the `MyMesh` class.
+ Added a `visualizeBoneWeights()` method to the `MySpatial` class.
+ Added a `listCorners()` method to the `RectangularSolid` class.

## Version 2.18.0 released on 5 January 2019

+ Improved exception handling in the `MyControl` class
+ Targeted jMonkeyEngine version 3.2.2-stable

## Version 2.17.0 released on 28 December 2018

+ Improved output formatting in the `MyCamera.describe()` method
+ Targeted jMonkeyEngine version 3.2.2-beta1

## Version 2.16.0 released on 9 December 2018

+ Added describers for axes, floats, and fractions to the `MyString` class
+ Added describers suitable for locations and directions
  to the `MyVector3f` class
+ Added describers to the `MyColor` and `MyQuaternion` classes
+ Added a `mean()` method to the `MyArray` class
+ Trimmed trailing zeros in the `MyCamera.describe()` and `describeMore()`
  methods

## Version 2.15.0 released on 3 December 2018

+ Fixed bug in the `RectangularSolid` constructor based on `BoundingBox`
+ Added support for `RectangularSolid` in `Misc.deepCopy()`
+ Added a `RectangularSolid` constructor based on half extents

## Version 2.14.0 released on 28 November 2018

+ Added a `RectangularSolid` class to model rotated boxes
+ Added a `covariance()` method to the `MyVector3f` class
+ Added a `nextQuaternion()` method to the `Generator` class
+ Added a `lengthSquared()` method to the `MyQuaternion` class

## Version 2.13.0 released on 19 November 2018

+ Added an `area()` method for `Triangle` to the `MyMath` class
+ Added a `tetrahedronVolume()` method to the `MyVolume` class

## Version 2.12.0 released on 12 November 2018

+ Added a `findIndex()` method for scene-graph controls to
  the `MySpatial` class
+ Added a `mean()` method to the `MyVector3f` class
+ Added `nonEmpty()` methods for Object arrays and collections to
  the `Validate` class
+ Allow for negative bone weights in animated meshes

## Version 2.11.0 released on 19 October 2018

+ Renamed 5 methods in the `MySpatial` class and deprecated the old names
+ Renamed 2 `Validate.isNumber()` methods and deprecated the old name
+ Renamed `MyColor.parseColor()` and deprecated the old name
+ Eviscerated `SubtreeControl.cloneForSpatial()`
+ Added `copyLocalTransform()`, `copyMeshTransform()`, `preOrderBones()`,
  and `setLocalTransform()` methods to the `MySkeleton` class
+ Added a `worldTransform()` method to the `MySpatial` class
+ Added a `slerp()` method for `Transform` to the `MyMath` class
+ Added a `finite()` method for `Vector3f` to the `Validate` class
+ Added an `isAnimated()` method to the `MyMesh` class

## Version 2.10.0 released on 23 September 2018

+ Added `findOverride()`, `listMaterialUsers()`, and `listMeshUsers()` methods
  to the `MySpatial` class
+ Renamed `Misc.deepClone()` and deprecated the old name
+ Deprecated the `Misc.getVersion()` and `Misc.getVersionShort()` methods

## Version 2.9.0 released on 12 September 2018

+ Added `createShadedMaterial()` for solid colors to the `MyAsset` class

## Version 2.8.0 released on 3 September 2018

+ Added an `isScaleUniform()` method to the `MyVector3f` class
+ Added 2 `nonZero()` methods and a `nonNegative()` method to
  the `Validate` class

## Version 2.7.0 released on 22 August 2018

+ Added a `getWorldScale()` method to the `MySpatial` class
+ Added support for `VectorXZ` in `Misc.deepClone()`

## Version 2.6.1 released on 21 August 2018

+ Fixed logic errors in 3 material definitions
+ Added support for `Byte`, `Character`, and `String` in `Misc.deepClone()`

## Version 2.6.0 released on 17 August 2018

+ Added a deepClone() method to the Misc class
+ Added a parseColor() method to the MyColor class
+ Added a listMeshes() method to the MyMesh class
+ Added countUses() and listMaterials() methods to the MySpatial class
+ Added cardinalizeLocal() and parse() methods to the MyVector3f class
+ Verify projection mode in MyCamera.setYTangent()
+ Allow null subtrees in listAnimatedMeshes() and isIgnoringTransforms()
+ Omit duplicates in MySpatial.listAnimatedMeshes()
+ Corrected names in 2 material-definition files

## Version 2.5.0 released on 17 March 2018

+ Set the Wireframe render-state flag in 3 material definitions
+ Added a findTrackIndex() method to the MyAnimation class
+ Added a listSpatials() method to the MySpatial class
+ Extended MyCamera.yTangent() to handle parallel projection

## Version 2.4.0 released on 18 February 2018

+ Added a findNamed() method to the MySpatial class
+ Simplified MyAsset.createStarMapQuads() by using RectangleMesh

## Version 2.3.0 released on 7 February 2018

+ Added PointMesh class
+ Added LoopMeshTest application
+ Extended MyString.findLongestPrefix() and MyString.reduce() to operate
  on collections

## Version 2.2.0 released on 1 February 2018

+ Added vertexNormal() and vertexTangent() methods to the MyMesh class

## Version 2.1.0 released on 27 January 2018

+ Added vertexBoneIndices(), vertexBoneWeights(), vertexColor(), vertexSize(),
  vertexVector2f(), and vertexVector4f methods to the MyMesh class
+ Added MyMath.isBetween(double, double, double) method

## Version 2.0.0 released on 24 January 2018

+ Moved all Bullet dependencies out of the heart library
+ Moved all jme3-effects dependencies out of the heart library
+ Deprecated the old Misc.getFpp() method

## Version 1.1.1 released on 22 January 2018

+ Added MyCamera.isFullWidth() method
+ Target JME v3.2.1

## Version 1.1.0 released on 14 January 2018

+ Reduced the likelihood of MyCamera.listViewPorts() coming up empty
+ Accounted for bottom and left in MyCamera.frustumAspectRatio() calculations
+ Added a version of Misc.getFpp() that sets numSamples
+ Added MyLight class
+ Added MyMath.isBetween(int, int, int) method

## Version 1.0.0for32 released on 5 December 2017

+ 1st release to target JME v3.2
+ Forced MySpatial.findMinMaxCoords(Spatial) to use world coordinates
+ Renamed MySpatial.findObject() to findEnabledRbc()
+ Added isAllPositive() method to MyVector3f class
+ Added axis-index constants to MyVector3f class
+ Added ContrastAdjustmentFilter class

## Version 0.9.27 released on 21 November 2017

+ Modified API of MyVector3f.lineMeetsSphere() to use Line and center
+ Fixed assertion failure during MyMath.localizeRay()
+ Added ViewPortAppState class
+ Added mouseLine() method to MyCamera class
+ Added describeType() method to MyControl class
+ Added countBones() method to MyMesh class
+ Added cardinalizeLocal() method to MyQuaternion class
+ Added countMeshBones() and findControlledSpatial() methods to MySpatial class
+ Added firstAxis, lastAxis, and numAxes public constants to MyVector3f class
+ Added lineMeetsLine(), localizeDirection(), and normalizeLocal()
  methods to MyVector3f class

## Version 0.9.26 released on 27 October 2017

+ Added listAnimatedMeshes() and subtreeContainsAny() to MySpatial class
+ Added {cancel/get/map}Attachments() methods to MySkeleton class
+ Added UncachedKey class

## Version 0.9.25 released on 26 September 2017

+ Added caller-provided storage and used the transform of an animated geometry
  in MySkeleton.worldLocation()
+ Removed modelLocation() and 2 worldOrientation() methods from MySkeleton
+ Added more tests for ignoreTransform
+ Added support for more bones than skinning matrices in
  MyMesh.vertexLocation()
+ Made use of MyMesh.vertexVector3f() in MySpatial.findMinMaxCoords()

## Version 0.9.24 released on 22 September 2017

+ Removed getMapLocation(), moveWorld(), moveChildWorld(), rotateChild(),
  rotateObject(), and rotateY() methods from the MySpatial class
+ Added checks for ignoreTransform
+ Improved handling of non-uniform scaling and disabled RigidBodyControls
+ Added findObject(), isIgnoringTransforms(), and setWorldTransform() methods
  to the MySpatial class
+ Added MyQuaternion.isRotationIdentity() and MyVector3f.isScaleIdentity()
+ Replaced asserts with IllegalArgumentExceptions

## Version 0.9.23 released on 19 September 2017

+ Removed the MyAnimation.listAnimations(), MySkeleton.findBoneIndex(), and
  MyString.getLine() methods
+ Renamed MyAnimation.describe(Track) to describeTrackType
+ Require an anim control to invoke MyAnimation.describe(Animation,...) or
  MyAnimation.describe(Track,...)
+ Require a control to invoke MyControl.describe()
+ Changed the MyString.join() methods to handle items of any type
+ Changed MyString.sharedPrefixLength() to handle char sequences of any type
+ Added methods DomeMesh.getUVScale(), MyBone.descendsFrom(), and
  MyControl.findSkeleton() methods
+ Improved support for multiple skeletons in MySkeleton.findBone()
+ Improved support for SpatialTrack in MyAnimation.describe(Track,...) and
  also in MyAnimation.getTargetName()
+ Improved support for attach nodes in MySkeleton.setName()

## Version 0.9.22 released on 11 September 2017

+ Renamed MyControl.isValid() to canDisable()
+ Changed some asserts to exceptions in MyControl class
+ Added canApplyPhysicsLocal(), isApplyPhysicsLocal(), objectName(), and
  setApplyPhysicsLocal() to MyControl
+ Handle ChaseCamera and MotionEvent in MyControl

## Version 0.9.21 released on 8 September 2017

+ Handle bone-index buffers containing shorts instead of bytes
+ Added MyAnimation.findSpatialTrack()
+ Allow MyAnimation.get{Scales/Translations}() to return null

## Version 0.9.20 released on 7 September 2017

+ Renamed MyAnimation.findTrack() to findBoneTrack()
+ Added MyAnimation.countTracks()
+ Added MyAnimation.get{Rotations/Scales/Translations}()
+ Added class StringLoader
+ Standardized the BSD license texts

## Version 0.9.19 released on 31 August 2017

+ Removed 10 editing methods from MyAnimation
+ Renamed MyAnimation.createTrack()
+ Fixed NPE while applying MyMesh.vertexLocation() to non-animated model
+ Added accumulateScaled() method to MyQuaternion

## Version 0.9.18 released on 30 August 2017

+ Moved the hasUV() method from Misc to MyMesh
+ Fixed NPE during MyAnimation.behead()
+ Added vertex{World}Location() and vertexVector3() methods to MyMesh
+ Added count{Spatial/UserData/Vertices}() methods to MySpatial
+ Added join() method for lists of strings (not needed in Java8)
+ Added more cases to MySpatial.describeType()

## Version 0.9.17 released on 17 August 2017

+ Removed MyAnimation.blendTo() method
+ Split MyMesh utility class off from MySkeleton class
+ Added behead() and zeroFirst() methods to MyAnimation class
+ Added AlphaDiscardThreshold, Color, and UseVertexColor parameters to the
  multicolor2 material definitions

## Version 0.9.16 released on 11 August 2017

+ Added driveMap() and listZipEntries() methods to Misc
+ Added fourthRoot() and lerp() methods to MyMath
+ Added slerp(), squad(), and squadA() methods to MyQuaternion
+ Added listSkeletons() method to MySkeleton
+ Added countControls() and listControls() methods to MySpatial
+ Added addMatchPrefix() method to MyString
+ Added accumulateScaled(), eq(), ne(), and lerp() methods to MyVector3f

## Version 0.9.15 released on 6 August 2017

+ Fixed a serious logic error in Generator.nextVector3f()
+ Added 5 methods to MyQuaternion, 2 methods to MyMath, and 1 to Validate

## Version 0.9.14 released on 31 July 2017

+ Moved snapLocal() from MyMath to new MyQuaternion class
+ Added method MyMath.standardize()
+ Added methods countNe() and standardize() to MyVector3f
+ Added methods ne() and standardize() to MyQuaternion
+ Added methods distinct() and findPreviousIndex() to MyArray
+ Added 5 methods to MyAnimation
+ Added 3 methods to MySpatial

## Version 0.9.13 released on 20 July 2017

+ Fixed NPEs caused by bone tracks without scales
+ Avoid use of == and != on floats, causing semantic changes in some cases
+ Moved 3 methods from MyMath to new MyArray class
+ Privatized protected fields in DomeMesh and LoopMesh
+ Added method MyArray.hasDistinct()
+ Added method MyCamera.listViewPorts()
+ Added method MyMath.isBetween()
+ Added methods aboutEquals(), compareTo(), and equals() to ReadXZ/VectorXZ

## Version 0.9.12 released on 18 July 2017

+ Added material definition multicolor2 to render point shapes
+ Added DomeMesh and RoundedRectangle classes
+ Added another constructor for RectangleMesh
+ Added method MyMath.normalize()
+ Use Mesh.Mode.LineLoop (where feasible) when generating meshes

## Version 0.9.11 released on 14 July 2017

+ Fixed logic error in MyCamera.viewAspectRatio()
+ Moved mesh generators to new jme3utilities.mesh package
+ Rename Misc.getUserPath() to homePath() and generate absolute pathname
+ Added class RectangleOutlineMesh
+ Added method MyString.removeSuffix()
+ Added option to MyAsset.createWireframeMaterial() to set the point size
+ Creative use of mesh modes to save indices

## Version 0.9.10 released on 13 July 2017

+ Renamed Rectangle to RectangleMesh to avoid confusion
+ Removed Misc.isIdentity() for being redundant with MyMath.isIdentity()
+ Replaced MyCamera.aspectRatio() with {display/frustum/view}AspectRatio()
+ Added utility methods to MyCamera and Misc
+ Added LoopMesh class
+ Added simpler constructors for RectangleMesh
+ Distinguish BitmapText in MySpatial.describeType()

## Version 0.9.9 released on 7 July 2017

+ Fixed logic error in MyCamera.aspectRatio()
+ Added Rectangle class to generate meshes
+ Added utility methods to MyCamera, MyMath, MySpatial, and MyVector3f
+ Handle StatsView case in MyControl.isEnabled()

## Version 0.9.8 released on 30 May 2017

+ Changed API of MySkeleton for clarity
+ Better handling of multiple skeletons and missing skeletons in MySkeleton

## Version 0.9.7 released on 24 May 2017

+ Fixed aliasing bugs/hazards in MyAsset
+ Added methods to MyControl, MySpatial and MyString, partly from debug library

## Version 0.9.6 released on 22 May 2017

+ Added wireframe shaders and material definitions, partly from SkyControl lib
+ Added 4 new methods to the MyAnimation class

## Version 0.9.5 released on 20 May 2017

+ Changed semantics of escape(), quote(), and unescape() in MyString
+ Added new methods to MyAnimation, MySkeleton, MyVector3f
+ Removed the Indices class
+ Removed findMinMaxHeights() and getMaxY() from MySpatial
+ Created jme3-utilities-heart library by splitting off packages from the
  SkyControl and jme3-utilities-x libraries
