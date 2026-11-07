plugins {
    java
}

// Compiling against the oldest supported API with Java 8 bytecode keeps one jar working from 1.8 to the latest release.
val spigotVersion = "1.8.8-R0.1-SNAPSHOT"
val manticLibVersion = "1.0.53.3"
val localManticLib = gradle.includedBuilds.any { it.name == "ManticLib" }

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
    // ManticLib relocates its shaded libraries, so compile against the shaded variant to match runtime.
    val manticLib = if (localManticLib) "me.fullpage:ManticLib:$manticLibVersion" else "com.github.Mantic-Development:ManticLib:$manticLibVersion"
    compileOnly(manticLib) { attributes { attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED)) } }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.11.6") { isTransitive = false }

    // Tests run against the old Gson that spigot-api 1.8.8 brings.
    testImplementation("org.spigotmc:spigot-api:$spigotVersion")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 8
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.release = 8
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching("plugin.yml") { expand(props) }
    }
}
