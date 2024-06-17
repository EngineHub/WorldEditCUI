plugins {
    alias(libs.plugins.architecturyPlugin)
    alias(libs.plugins.loom)
    alias(libs.plugins.shadow)
}

architectury {
    neoForge()
    platformSetupLoomIde()
}

configurations {
    val common = dependencyScope("common")
    compileClasspath { extendsFrom(common.get()) }
    runtimeClasspath { extendsFrom(common.get()) }
    "developmentNeoForge" { extendsFrom(common.get()) }
}

val shadowBundle = configurations.dependencyScope("shadowBundle")
val shadowBundleClasspath = configurations.resolvable("shadowBundleClasspath") {
    extendsFrom(shadowBundle.get())
}

dependencies {
    neoForge(libs.neoforge)

    "common"(project(":worldeditcui-protocol-common", configuration = "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":worldeditcui-protocol-common", configuration = "transformProductionNeoForge"))
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER-PROTOCOL"))
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
