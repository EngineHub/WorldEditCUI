plugins {
    alias(libs.plugins.architecturyPlugin)
    alias(libs.plugins.loom)
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER-PROTOCOL"))
}

val enabledPlatforms: String by project
architectury {
    common(enabledPlatforms.split(','))
}

dependencies {
    modImplementation(libs.fabric.loader)
}