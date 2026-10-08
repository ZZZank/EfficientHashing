import java.util.function.BiConsumer

plugins {
    id("net.neoforged.moddev") version "2.0.148"
    id("neoforge-mutex")
    id("com.hypherionmc.modutils.modpublisher") version "2.2.3"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-neoforge"

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

// The shared `src/` of the project is linked in by Stonecutter automatically;
// loader-specific sources and metadata are layered on top from `neoforge/src`.
sourceSets {
    main {
        java.srcDir(rootProject.file("neoforge/src/main/java"))
        resources.srcDir(rootProject.file("neoforge/src/main/resources"))
    }
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    // NeoForge itself comes from the `neoForge {}` block below
}

neoForge {
    version = property("deps.neo_loader") as String

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = file("../../run/")
            client()
        }

        register("server") {
            gameDirectory = file("../../run/")
            server()
        }
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("authors", sc.properties["mod.authors"])
            register("minecraft", sc.properties["mod.mc_dep_forgelike"])
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("fabric.mod.json")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

// see: https://github.com/firstdarkdev/modpublisher
publisher {
    validatedProp("publish.modrinth", "MODRINTH_TOKEN") { id, key ->
        modrinthID.set(id)
        apiKeys { modrinth(key) }
    }

    validatedProp("publish.curseforge", "CURSE_TOKEN") { id, key ->
        curseID.set(id)
        apiKeys { curseforge(key) }
    }

    validatedProp("publish.github", "GITHUB_TOKEN") { id, key ->
        githubRepo.set(id)
        apiKeys { github(key) }
    }

    val publishType = if (project.hasProperty("publish.type")) {
        project.property("publish.type") // for supporting `-Pxxx=yyy` in command
    } else {
        sc.properties["publish.type"]
    } as String
    if (publishType == "debug") {
        // Enable Debug mode. When enabled, no files will actually be uploaded
        debug.set(true)
    } else {
        versionType.set(publishType)
    }

    val modVersion = sc.properties["mod.version"] as String
    val mcVersionTitle = sc.properties["mod.mc_title"] as String
    val platform = "neoforge"

    changelog.set(rootProject.file("CHANGELOG.md"))
    projectVersion.set(modVersion)
    // Example: 1.2.3 for 1.20.1 forge
    displayName.set("$modVersion for $mcVersionTitle $platform")
    gameVersions.set((sc.properties["mod.mc_targets"] as String).split(" "))
    loaders.set(listOf(platform))
    artifact.set(tasks.jar)
}

fun validatedProp(prop: String, env: String, action: BiConsumer<String, String>) {
    val projectID = if (project.hasProperty(prop)) { project.property(prop) as String } else { null }
    val apiKey = System.getenv(env)
    if (projectID != null && !projectID.startsWith('[') && apiKey != null && apiKey.isNotEmpty()) {
        action.accept(projectID, apiKey)
    }
}