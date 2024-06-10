import net.darkhax.curseforgegradle.Constants
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.fabricmc.loom.LoomGradleExtension

plugins {
    java
    alias(libs.plugins.indra.git)
    alias(libs.plugins.indra.spotlessLicenser)
    alias(libs.plugins.loom)
    alias(libs.plugins.versions)
    alias(libs.plugins.javaEcosystemCapabilities)
    alias(libs.plugins.curseForgeGradle)
    alias(libs.plugins.minotaur)
    alias(libs.plugins.publishGithubRelease)
    alias(libs.plugins.spotless)
}

group = "org.enginehub.worldeditcui"
version = "${libs.versions.minecraft.get()}+01-SNAPSHOT"

repositories {
    // mirrors:
    // - https://maven.enginehub.org/repo/
    // - https://maven.terraformersmc.com/releases/
    // - https://maven.minecraftforge.net/
    // - https://maven.parchmentmc.org/
    // - https://repo.viaversion.com/
    maven(url = "https://repo.stellardrift.ca/repository/stable/") {
        name = "stellardriftReleases"
        mavenContent { releasesOnly() }
    }
    maven(url = "https://repo.stellardrift.ca/repository/snapshots/") {
        name = "stellardriftSnapshots"
        mavenContent { snapshotsOnly() }
    }
}

val targetVersion = 21
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

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER"))
}

loom {
    runs {
        named("client") {
            // Mixin debug options
            vmArgs(
                    // "-Dmixin.debug.verbose=true",
                    // "-Dmixin.debug.export=true",
                    // "-Dmixin.debug.export.decompile.async=false", // to get decompiled sources when mixins straight up fail to apply
                    "-Dmixin.dumpTargetOnFailure=true",
                    "-Dmixin.checks.interfaces=true",
                    "-Dwecui.debug.mixinaudit=true",
                    "-Doptifabric.extract=true"
            )
        }
    }
    decompilerOptions.named("vineflower") {
        options.put("win", "0")
    }
    silentMojangMappingsLicense()
}

// Ugly hack for easy genSourcening
afterEvaluate {
    tasks.matching { it.name == "genSources" }.configureEach {
        setDependsOn(setOf("genSourcesWithVineflower"))
    }
}

val fabricApi by configurations.creating
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings {
            nameSyntheticMembers = false
        }
        parchment(variantOf(libs.parchment) { artifactType("zip") })
    })
    vineflowerDecompilerClasspath(libs.vineflower)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.modmenu)
    modCompileOnly(libs.viafabricplus.api) {
        isTransitive = false
    }
    modCompileOnly(libs.viaversion)

    // [1] declare fabric-api dependency...
    fabricApi(libs.fabric.api)

    // [2] Load the API dependencies from the fabric mod json...
    @Suppress("UNCHECKED_CAST")
    val fabricModJson = file("src/main/resources/fabric.mod.json").bufferedReader().use {
        groovy.json.JsonSlurper().parse(it) as Map<String, Map<String, *>>
    }
    val wantedDependencies = (fabricModJson["depends"] ?: error("no depends in fabric.mod.json")).keys
        .filter { it == "fabric-api-base" || it.contains(Regex("v\\d$")) }
        .map { "net.fabricmc.fabric-api:$it" }
        .toSet()
    logger.lifecycle("Looking for these dependencies:")
    for (wantedDependency in wantedDependencies) {
        logger.lifecycle(wantedDependency)
    }
    // [3] and now we resolve it to pick out what we want :D
    val fabricApiDependencies = fabricApi.incoming.resolutionResult.allDependencies
        .onEach {
            if (it is UnresolvedDependencyResult) {
                throw kotlin.IllegalStateException("Failed to resolve Fabric API", it.failure)
            }
        }
        .filterIsInstance<ResolvedDependencyResult>()
        // pick out transitive dependencies
        .flatMap {
            it.selected.dependencies
        }
        // grab the requested versions
        .map { it.requested }
        .filterIsInstance<ModuleComponentSelector>()
        // map to standard notation
        .associateByTo(
            mutableMapOf(),
            keySelector = { "${it.group}:${it.module}" },
            valueTransform = { "${it.group}:${it.module}:${it.version}" }
        )
    fabricApiDependencies.keys.retainAll(wantedDependencies)
    // sanity check
    for (wantedDep in wantedDependencies) {
        check(wantedDep in fabricApiDependencies) { "Fabric API library $wantedDep is missing!" }
    }

    fabricApiDependencies.values.forEach {
        "include"(it)
        "modImplementation"(it)
    }

    // for development
    /*modLocalRuntime(libs.worldedit) {
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
        exclude("com.google.code.gson", "gson")
        exclude("it.unimi.dsi", "fastutil")
        exclude("org.apache.logging.log4j", "log4j-api")
    }*/
}

configurations.modLocalRuntime {
    shouldResolveConsistentlyWith(configurations.modImplementation.get())
}

tasks {
    register("generateStandaloneRun") {
        description = "Generate a script that will run WorldEdit CUI, for graphics debugging"
        val scriptDest = project.layout.buildDirectory.file(if (System.getProperty("os.name").contains("windows", ignoreCase = true)) {
            "run-dev.bat"
        } else {
            "run-dev"
        })
        val argsDest = project.layout.buildDirectory.file("run-dev-args.txt")
        val taskClasspath = project.files(jar.map { it.outputs }, configurations.runtimeClasspath)
        val toolchain = project.javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(targetVersion) }
        inputs.files(taskClasspath)
                .ignoreEmptyDirectories()
                .withPropertyName("runClasspath")
        // inputs.property("javaLauncher", toolchain)
        outputs.file(scriptDest)
        outputs.file(argsDest)
        doLast {
            val clientRun = loom.runConfigs.getByName("client")

            // technically uses internal API, but it's non-essential
            argsDest.get().asFile.writeText("""    
                -Dfabric.dli.config=${(loom as LoomGradleExtension).files.devLauncherConfig.absolutePath}
                -Dfabric.dli.env=client
                -Dfabric.dli.main=${clientRun.defaultMainClass}
                ${clientRun.vmArgs.joinToString(System.lineSeparator())}
                -cp ${taskClasspath.asPath}
                net.fabricmc.devlaunchinjector.Main
                ${clientRun.programArgs.joinToString(System.lineSeparator())}
            """.trimIndent(), Charsets.UTF_8)
            scriptDest.get().asFile.writeText("""
                ${toolchain.get().executablePath.asFile.absolutePath} "@${argsDest.get().asFile.absolutePath}"
            """.trimIndent(), Charsets.UTF_8)
        }
    }

    withType(net.fabricmc.loom.task.AbstractRunTask::class).configureEach {
        // Configure mixin agent
        // Resolve the Mixin configuration
        val mixinSource = configurations.runtimeClasspath.get().incoming
                .artifactView { componentFilter { it is ModuleComponentIdentifier && it.module == "sponge-mixin" && it.group == "net.fabricmc" } }
                .files
        inputs.files(mixinSource).withPropertyName("mixinAgent")

        jvmArgumentProviders += CommandLineArgumentProvider {
            // Java agent: the jar file for mixin
            if (!mixinSource.isEmpty) {
                listOf("-javaagent:${mixinSource.singleFile}")
            } else {
                emptyList()
            }
        }
    }

    processResources.configure {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}

// Releasing
val changelogContents = objects.property(String::class)
changelogContents.set(providers.gradleProperty("changelog")
        .map { file(it) }
        .filter { it.exists() }
        .map { it.readText(Charsets.UTF_8) })
changelogContents.finalizeValueOnRead()
val versionName = project.provider { project.version }

val cfApiToken = providers.gradleProperty("cfApiToken")
val modrinthToken = providers.gradleProperty("modrinthToken")
        .orElse(providers.environmentVariable("MODRINTH_TOKEN"))
val githubToken = providers.gradleProperty("githubToken")
        .orElse(providers.environmentVariable("GITHUB_TOKEN"))

tasks {
    val validateRelease by registering {
        inputs.property("changelog", changelogContents).optional(true)
        inputs.property("version", versionName)

        doLast {
            val problems = mutableListOf<String>()
            // General release-readiness
            if (indraGit.headTag() == null) {
                problems.add("Tried to perform a release without being checked out to a tag")
            }
            if (versionName.get().toString().contains("SNAPSHOT")) {
                problems.add("SNAPSHOT versions of WorldEditCUI cannot be published")
            }
            if (!changelogContents.isPresent) {
                problems.add("A file with changelog text must be provided using the 'changelog' Gradle property")
            }

            // CF
            if (!cfApiToken.isPresent) {
                problems.add("No CurseForge API token was set with the 'cfApiToken' Gradle property")
            }

            // MR
            if (!modrinthToken.isPresent) {
                problems.add("No Modrinth access token was set with either the 'modrinthToken' Gradle property, or 'MODRINTH_TOKEN' environment variable")
            }

            // GH
            if (!githubToken.isPresent) {
                problems.add("No GitHub access token was set with either the 'githubToken' Gradle property, or 'GITHUB_TOKEN' environment variable")
            }

            when (problems.size) {
                0 -> return@doLast
                1 -> {
                    throw InvalidUserDataException(problems[0])
                }
                else -> throw InvalidUserDataException(
                    "WorldEditCUI detected the following problems when trying to perform a release:\n\n"
                        + problems.joinToString("\n - ", prefix = " - ")
                )
            }
        }
    }

    val publishToCurseForge by registering(TaskPublishCurseForge::class) {
        val cfProjectId = providers.gradleProperty("cfProjectId")

        apiToken = cfApiToken.get()

        with(upload(cfProjectId.get(), remapJar)) {
            displayName = project.version
            releaseType = Constants.RELEASE_TYPE_RELEASE
            changelog = changelogContents.getOrElse("")
            // Rendering plugins
            addOptional("canvas-renderer", "sodium", "irisshaders")
            // Config screens, version compatibility
            addOptional("modmenu", "viafabricplus", "worldedit")
            addJavaVersion("Java $targetVersion")
            addGameVersion(libs.versions.minecraft.get())
        }
    }

    val releaseTasks = listOf(publishToCurseForge, publishToGitHub, modrinth)
    releaseTasks.forEach {
        it.configure { dependsOn(validateRelease) }
    }

    register("publishRelease") {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(releaseTasks)
    }
}

modrinth {
    token = modrinthToken
    projectId = "worldedit-cui"
    syncBodyFrom = providers.provider { file("README.md").readText(Charsets.UTF_8) }
    uploadFile.set(tasks.remapJar)
    gameVersions.add(libs.versions.minecraft.get())
    changelog = changelogContents
    dependencies {
        optional.project("canvas")
        optional.project("sodium")
        optional.project("iris")
        // Config screens, version compatibility
        optional.project("modmenu")
        optional.project("viafabricplus")
        optional.project("worldedit")
    }
}

githubRelease {
    apiToken = githubToken
    tagName = project.provider {
        indraGit.headTag()?.run { org.eclipse.jgit.lib.Repository.shortenRefName(name) }
    }
    repository = "EngineHub/WorldEditCUI"
    releaseName = "WorldEditCUI v$version"
    releaseBody = changelogContents
    artifacts.from(tasks.remapJar)
}
