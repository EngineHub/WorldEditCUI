rootProject.name = "WorldEditCUI"

pluginManagement {
    repositories {
        // The EngineHub repository mirror the following repository.
        // maven { setUrl("https://maven.fabricmc.net/") }
        // maven { setUrl("https://maven.architectury.dev/") }
        // maven { setUrl("https://files.minecraftforge.net/maven/") }
        maven { setUrl("https://maven.enginehub.org/repo/") }
        gradlePluginPortal()
    }
}

include("common")
include("fabric")
include("neoforge")
