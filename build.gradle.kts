import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension

plugins {
    base
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.indra.spotlessLicenser) apply false
}

allprojects {
    group = "org.enginehub.worldeditcui"
    version = "${rootProject.libs.versions.minecraft.get()}+01-SNAPSHOT"

    repositories {
        // mirrors:
        // - https://maven.enginehub.org/repo/
        // - https://maven.terraformersmc.com/releases/
        // - https://maven.minecraftforge.net/
        // - https://maven.neoforged.net/
        // - https://maven.parchmentmc.org/
        // - https://repo.viaversion.com/
        maven(url = "https://repo.stellardrift.ca/repository/stable/") {
            name = "stellardriftReleases"
            mavenContent {
                releasesOnly()
                excludeGroup("org.lwjgl") // workaround for lwjgl-freetype
            }
        }
        maven(url = "https://repo.stellardrift.ca/repository/snapshots/") {
            name = "stellardriftSnapshots"
            mavenContent { snapshotsOnly() }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "net.kyori.indra.licenser.spotless")
    apply(plugin = "maven-publish")

    val targetJavaVersion: String by project
    val targetVersion = targetJavaVersion.toInt()
    extensions.configure(JavaPluginExtension::class) {
        sourceCompatibility = JavaVersion.toVersion(targetVersion)
        targetCompatibility = sourceCompatibility
        if (JavaVersion.current() < JavaVersion.toVersion(targetVersion)) {
            toolchain.languageVersion = JavaLanguageVersion.of(targetVersion)
        }
        withSourcesJar()
    }

    tasks.withType(JavaCompile::class).configureEach {
        options.release = targetVersion
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
    }

    tasks.named("processResources", ProcessResources::class).configure {
        inputs.property("version", project.version)

        sequenceOf("fabric.mod.json", "META-INF/neoforge.mods.toml").forEach {
            filesMatching(it) {
                expand("version" to project.version)
            }
        }
    }

    extensions.configure(IndraSpotlessLicenserExtension::class) {
        licenseHeaderFile(rootProject.file("HEADER"))
    }

    plugins.withId("dev.architectury.loom") {
        val loom = extensions.getByType(LoomGradleExtensionAPI::class)
        loom.run {
            decompilerOptions.named("vineflower") {
                options.put("win", "0")
            }
            silentMojangMappingsLicense()
        }

        // Ugly hack for easy genSourcening
        afterEvaluate {
            tasks.matching { it.name == "genSources" }.configureEach {
                setDependsOn(setOf("genSourcesWithVineflower"))
            }
        }

        dependencies {
            "minecraft"(libs.minecraft)
            "mappings"(loom.layered {
                officialMojangMappings {
                    nameSyntheticMembers = false
                }
                parchment(variantOf(libs.parchment) { artifactType("zip") })
            })
            "vineflowerDecompilerClasspath"(libs.vineflower)
        }

        configurations.named("modLocalRuntime") {
            shouldResolveConsistentlyWith(configurations.getByName("modImplementation"))
        }
    }

    extensions.configure(PublishingExtension::class) {
        publications {
            register("maven", MavenPublication::class) {
                from(components.getByName("java"))
            }
        }
    }
}