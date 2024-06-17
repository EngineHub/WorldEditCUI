plugins {
    alias(libs.plugins.architecturyPlugin)
    alias(libs.plugins.loom)
    alias(libs.plugins.shadow)
}

architectury {
    fabric()
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER-PROTOCOL"))
}

configurations {
    val common = dependencyScope("common")
    compileClasspath { extendsFrom(common.get()) }
    runtimeClasspath { extendsFrom(common.get()) }
    "developmentFabric" { extendsFrom(common.get()) }
}

val shadowBundle = configurations.dependencyScope("shadowBundle")
val shadowBundleClasspath = configurations.resolvable("shadowBundleClasspath") {
    extendsFrom(shadowBundle.get())
}

dependencies {
    "common"(project(":worldeditcui-protocol-common", configuration = "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":worldeditcui-protocol-common", configuration = "transformProductionFabric"))
    modImplementation(libs.fabric.loader)
    modImplementation(platform(libs.fabric.api.bom))
    modImplementation(libs.fabric.api.networking)
}

tasks {
    shadowJar {
        configurations = listOf(shadowBundleClasspath.get())
        archiveClassifier = "dev-shadow"
    }

    remapJar {
        inputFile = shadowJar.flatMap { it.archiveFile }
    }
}
