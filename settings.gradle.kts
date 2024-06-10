rootProject.name = "WorldEditCUI"

pluginManagement {
    repositories {
        // mirrors:
        // - https://maven.architectury.dev/
        // - https://maven.fabricmc.net/
        // - gradlePluginPortal
        maven(url = "https://repo.stellardrift.ca/repository/stable/") {
            name = "stellardriftReleases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.stellardrift.ca/repository/snapshots/") {
            name = "stellardriftSnapshots"
            mavenContent { snapshotsOnly() }
        }
        // maven("https://maven.fabricmc.net/")
        // gradlePluginPortal()
    }
}
