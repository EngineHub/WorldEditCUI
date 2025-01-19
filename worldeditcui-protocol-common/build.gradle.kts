import dev.architectury.plugin.TransformingTask

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

tasks.withType(TransformingTask::class).configureEach {
    taskActions.removeIf { it.displayName == "Execute copy" } // why do i have to do this
}

dependencies {
    modImplementation(libs.fabric.loader)
}