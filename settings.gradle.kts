plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ManticHooks"

// Contributors with a ManticLib checkout next to this one build against it; everyone else resolves it from JitPack.
if (file("../ManticLib/build.gradle.kts").isFile) includeBuild("../ManticLib")

// Local-only profiling harness and test servers; not part of the published project.
if (file("test-harness/build.gradle.kts").isFile) include("test-harness")
