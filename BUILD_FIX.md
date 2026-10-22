# Build fix

The original project was being evaluated with Gradle 9.0-milestone-1 and failed while resolving
`:app:debugRuntimeClasspathCopy` with:

`Cannot select root node 'debugRuntimeClasspathCopy' as a variant.`

This version pins the build to Gradle 8.11.1 via `gradle/wrapper/gradle-wrapper.properties`.
Use the included `gradlew`/`gradlew.bat` or import the project into Android Studio and let it
use the Gradle wrapper.

If Android Studio asks to use the Gradle wrapper, choose that option rather than a system Gradle 9
installation.
