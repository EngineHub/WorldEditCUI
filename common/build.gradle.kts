import dev.architectury.plugin.ArchitectPluginExtension

extensions.configure<ArchitectPluginExtension> {
    common(rootProject.properties["enabledPlatforms"].toString().split(','))
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation(rootProject.libs.fabric.loader)

    // Architectury API. This is optional, and you can comment it out if you don't need it.
    modImplementation(rootProject.libs.architectury)

    modImplementation(rootProject.libs.cuiProtocol.common)
}

loom {
    accessWidenerPath = file("src/main/resources/worldeditcui.accesswidener")
}