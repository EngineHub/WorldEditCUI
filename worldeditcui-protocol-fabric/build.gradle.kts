plugins {
    alias(libs.plugins.architecturyPlugin)
    alias(libs.plugins.loom)
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

// Files in this configuration will be bundled into your mod using the Shadow plugin.
// Don't use the `shadow` configuration from the plugin itself as it's meant for excluding
    val shadowBundle = dependencyScope("shadowBundle")
    val shadowBundleClasspath = resolvable("shadowBundleClasspath") {
        extendsFrom(shadowBundle.get())
    }
}

dependencies {
    "common"(project(":worldeditcui-protocol-common", configuration = "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":worldeditcui-protocol-common", configuration = "transformProductionFabric"))
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
}
