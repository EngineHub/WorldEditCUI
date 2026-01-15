repositories {
    maven { setUrl("https://maven.neoforged.net/releases") }
}

plugins {
    alias(libs.plugins.shadow)
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val common: Configuration by configurations.creating
val shadowBundle: Configuration by configurations.creating
val developmentNeoForge: Configuration by configurations.getting

configurations {
    common.isCanBeResolved = true
    common.isCanBeConsumed = false

    compileClasspath {
        extendsFrom(common)
    }
    runtimeClasspath {
        extendsFrom(common)
    }
    developmentNeoForge.extendsFrom(common)

    // Files in this configuration will be bundled into your mod using the Shadow plugin.
    // Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
    shadowBundle.isCanBeResolved = true
    shadowBundle.isCanBeConsumed = false
}

dependencies {
    neoForge(rootProject.libs.neoforge)

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowBundle(project(":common", "transformProductionNeoForge"))

    modImplementation(rootProject.libs.cuiProtocol.neoforge)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
}

// TODO: Add the releasing build scripts