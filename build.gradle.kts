import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    base
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.javaEcosystemCapabilities) apply false
}

allprojects {
    repositories {
        // mirrors:
        // - https://maven.enginehub.org/repo/
        // - https://maven.terraformersmc.com/releases/
        // - https://maven.minecraftforge.net/
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
    apply(plugin = "org.gradlex.java-ecosystem-capabilities")

    val targetJavaVersion: String by project
    val targetVersion = targetJavaVersion.toInt()
    extensions.configure(JavaPluginExtension::class) {
        sourceCompatibility = JavaVersion.toVersion(targetVersion)
        targetCompatibility = sourceCompatibility
        if (JavaVersion.current() < JavaVersion.toVersion(targetVersion)) {
            toolchain.languageVersion = JavaLanguageVersion.of(targetVersion)
        }
    }

    tasks.withType(JavaCompile::class).configureEach {
        options.release = targetVersion
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
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
}