import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    base
    java
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.architectury.plugin)
    alias(libs.plugins.shadow) apply false
    // TODO: Reinsert the spotless plugin.
    //alias(libs.plugins.spotless)
    //alias(libs.plugins.indra.spotlessLicenser) apply false
}

subprojects {
    apply(plugin = "base")
    apply(plugin = "java")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    group = "org.enginehub.worldeditcui"
    version = "${rootProject.libs.versions.minecraft.get()}+02+SNAPSHOT"
    base.archivesName = "${rootProject.name}-${project.name}"

    repositories {
        // The EngineHub repository mirror the following repository.
        // maven { setUrl("https://maven.parchmentmc.org") }
        maven { setUrl("https://maven.enginehub.org/repo/") }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

        withSourcesJar()
    }

    tasks.withType(JavaCompile::class).configureEach {
        options.release = 21
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
    }

    architectury {
        minecraft = rootProject.libs.versions.minecraft.get()
    }

    val loom = extensions.getByType(LoomGradleExtensionAPI::class)
    loom.run {
        silentMojangMappingsLicense()
    }

    dependencies {
        "minecraft"(rootProject.libs.minecraft)
        "mappings"(loom.layered() {
            officialMojangMappings()
            parchment(variantOf(rootProject.libs.parchment) { artifactType("zip") })
        })
    }

    extensions.configure(PublishingExtension::class) {
        publications {
            register("maven", MavenPublication::class) {
                from(components.getByName("java"))
            }
        }
    }
}