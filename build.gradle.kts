import net.darkhax.curseforgegradle.Constants
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.fabricmc.loom.LoomGradleExtension

plugins {
    java
    alias(libs.plugins.loom)
    alias(libs.plugins.loomQuiltflower)
    alias(libs.plugins.versions)
    alias(libs.plugins.javaEcosystemCapabilities)
    alias(libs.plugins.curseForgeGradle)
    id("org.enginehub.worldeditcui.ghrelease")
}

group = "org.enginehub.worldeditcui"
version = "${libs.versions.minecraft.get()}+01-SNAPSHOT"

repositories {
    // mirrors:
    // - https://maven.enginehub.org/repo/
    // - https://maven.terraformersmc.com/releases/
    // - https://maven.minecraftforge.net/
    // - https://maven.parchmentmc.org/
    maven(url = "https://repo.stellardrift.ca/repository/stable/") {
        name = "stellardriftReleases"
        mavenContent { releasesOnly() }
    }
    maven(url = "https://repo.stellardrift.ca/repository/snapshots/") {
        name = "stellardriftSnapshots"
        mavenContent { snapshotsOnly() }
    }
}

quiltflower {
    quiltflowerVersion = libs.versions.quiltflower.get()
    addToRuntimeClasspath = true
    preferences["win"] = 0
}

val targetVersion = 17
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
}

// Ugly hack for easy genSourcening
afterEvaluate {
    tasks.matching { it.name == "genSources" }.configureEach {
        setDependsOn(setOf("genSourcesWithQuiltflower"))
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
    modImplementation(libs.fabric.loader)
    modImplementation(libs.modmenu)
    modImplementation(libs.multiconnect.api) {
        isTransitive = false
    }

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
        val scriptDest = project.layout.buildDirectory.file(if (System.getProperty("os.name").contains("windows", ignoreCase = true)) { "run-dev.bat" } else { "run-dev" })
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

    val publishToCurseForge by registering(TaskPublishCurseForge::class) {
        val cfApiToken: String by project
        val cfProjectId: String by project
        val changelogFile = project.findProperty("changelog")
        val version = project.provider { project.version }

        onlyIf { indraGit.headTag() != null }

        doFirst {
            if (version.get().toString().contains("SNAPSHOT")) {
                throw InvalidUserDataException("SNAPSHOT versions of WorldEditCUI cannot be published to CurseForge")
            }
            if (changelogFile == null || !file(changelogFile).isFile) {
                throw InvalidUserDataException("A file with changelog text must be provided using the 'changelog' Gradle property")
            }
        }

        apiToken = cfApiToken

        with(upload(cfProjectId, remapJar)) {
            releaseType = Constants.RELEASE_TYPE_RELEASE
            changelog = changelogFile?.let(::file)
            // Rendering plugins
            addOptional("canvas-renderer", "sodium", "irisshaders")
            // Config screens, version compatibility
            addOptional("modmenu", "multiconnect", "worldedit")
            addJavaVersion("Java 17")
            addGameVersion(libs.versions.minecraft.get())
        }
    }

    register("publishRelease") {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(publishToCurseForge, publishToGitHub)
    }

    publishToGitHub {
        onlyIf { indraGit.headTag() != null }
    }
}

githubRelease {
    val changelogFile = project.findProperty("changelog")
    apiToken = providers.gradleProperty("githubToken")
            .orElse(providers.environmentVariable("GITHUB_TOKEN"))

    // tag is inferred from the head tag
    repository = "EngineHub/WorldEditCUI"
    releaseName = "WorldEditCUI v$version"
    releaseBody = project.provider { changelogFile?.let(::file)?.readText(Charsets.UTF_8) }
    artifacts.from(tasks.remapJar)
}
