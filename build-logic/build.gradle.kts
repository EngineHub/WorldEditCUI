plugins {
    `java-gradle-plugin`
}

val targetVersion = 11
java {
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

dependencies {
    implementation(libs.githubApi)
    implementation(libs.indra.git)
}

gradlePlugin {
    plugins {
        register("publish-gh-release") {
            id = "org.enginehub.worldeditcui.ghrelease"
            description = "Publish a GitHub release"
            implementationClass = "org.enginehub.build.worldeditcui.GitHubReleaserPlugin"
        }
    }
}
