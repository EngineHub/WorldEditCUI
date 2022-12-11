plugins {
    java
    id("fabric-loom") version "1.0.+"
    id("io.github.juuxel.loom-quiltflower") version "1.8.0"
    id("com.github.ben-manes.versions") version "0.44.0"
    id("de.jjohannes.missing-metadata-guava") version "31.1.1"
}

val minecraftVersion = "1.19.3"
val fabricLoaderVersion = "0.14.11"
val fabricApiVersion = "0.68.1+1.19.3"
val modmenuVersion = "5.0.1"
val multiconnectVersion = "1.5.10"

group = "org.enginehub.worldeditcui"
version = "$minecraftVersion+01-SNAPSHOT"

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
    quiltflowerVersion.set("1.9.0")
    addToRuntimeClasspath.set(true)
    preferences["win"] = 0
}

val targetVersion = 17
java {
    sourceCompatibility = JavaVersion.toVersion(targetVersion)
    targetCompatibility = sourceCompatibility
    if (JavaVersion.current() < JavaVersion.toVersion(targetVersion)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetVersion))
    }
}

tasks.withType(JavaCompile::class) {
    options.release.set(targetVersion)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
}

val fabricApiConfiguration: Configuration = configurations.create("fabricApi")

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings {
            nameSyntheticMembers = false
        }
        parchment("org.parchmentmc.data:parchment-1.19.2:2022.11.27@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("com.terraformersmc:modmenu:$modmenuVersion")
    modImplementation("net.earthcomputer.multiconnect:multiconnect-api:$multiconnectVersion") {
        isTransitive = false
    }

    // [1] declare fabric-api dependency...
    "fabricApi"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

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
    val fabricApiDependencies = fabricApiConfiguration.incoming.resolutionResult.allDependencies
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
    /*modRuntimeOnly("com.sk89q.worldedit:worldedit-fabric-mc1.19.2:7.2.12") {
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
        exclude("com.google.code.gson", "gson")
        exclude("it.unimi.dsi", "fastutil")
        exclude("org.apache.logging.log4j", "log4j-api")
    }*/
}

tasks {
    register("generateStandaloneRun") {
        description = "Generate a script that will run WorldEdit CUI, for graphics debugging"
        val scriptDest = project.layout.buildDirectory.file(if (System.getProperty("os.name").contains("windows", ignoreCase = true)) { "run-dev.bat" } else { "run-dev" })
        val argsDest = project.layout.buildDirectory.file("run-dev-args.txt")
        val taskClasspath = project.files(jar.map { it.outputs }, configurations.runtimeClasspath)
        val toolchain = project.javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(targetVersion)) }
        inputs.files(taskClasspath)
                .ignoreEmptyDirectories()
                .withPropertyName("runClasspath")
        // inputs.property("javaLauncher", toolchain)
        outputs.file(scriptDest)
        outputs.file(argsDest)
        doLast {
            val clientRun = loom.runConfigs.getByName("client")
            //-Dfabric.dli.config=${minecraft.devLauncherConfig.absolutePath} TODO
            argsDest.get().asFile.writeText("""    
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
        // Mixin debug options
        jvmArgs(
                // "-Dmixin.debug.verbose=true",
                // "-Dmixin.debug.export=true",
                // "-Dmixin.debug.export.decompile.async=false", // to get decompiled sources when mixins straight up fail to apply
                "-Dmixin.dumpTargetOnFailure=true",
                "-Dmixin.checks.interfaces=true",
                "-Dwecui.debug.mixinaudit=true",
                "-Doptifabric.extract=true"
        )

        // Configure mixin agent
        jvmArgumentProviders += CommandLineArgumentProvider {
            // Resolve the Mixin configuration
            // Java agent: the jar file for mixin
            val mixinJar = configurations.runtimeClasspath.get().resolvedConfiguration
                    .getFiles { it.name == "sponge-mixin" && it.group == "net.fabricmc" }
                    .firstOrNull()

            if (mixinJar != null) {
                listOf("-javaagent:$mixinJar")
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
