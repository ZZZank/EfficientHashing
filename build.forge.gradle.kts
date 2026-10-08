import java.util.function.BiConsumer

plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
    id("neoforge-mutex")
    id("com.hypherionmc.modutils.modpublisher") version "2.2.3"
}

fun prop(key: String): Any? {
    return if (project.hasProperty(key)) {
        project.property(key)
    } else {
        sc.properties[key]
    }
}

// DO NOT set group = ...!
version = "${prop("mod.version")}+${sc.current.version}"
base.archivesName = "${prop("mod.id") as String}-forge"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

val modId = property("mod.id") as String
val forgeVersion = property("deps.loader") as String // e.g. 47.3.0, from stonecutter.properties.toml

// The shared `src/` of the project is linked in by Stonecutter automatically;
// loader-specific sources and metadata are layered on top from `forge/src`.
sourceSets {
    main {
        java.srcDir(rootProject.file("forge/src/main/java"))
        resources.srcDir(rootProject.file("forge/src/main/resources"))
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
    // Generates the mixin refmap mapping official names to SRG names used at runtime.
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

legacyForge {
    // The versions can be found at https://files.minecraftforge.net/
    version = "${sc.current.version}-$forgeVersion"

    runs {
        register("client") {
            client()
            gameDirectory = file("../../run/") // Shares the run directory between versions
        }

        register("server") {
            server()
            programArgument("--nogui")
            gameDirectory = file("../../run/")
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
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

// Legacy Forge runs on SRG names, so mixin members declared with official names
// need a refmap, and the config must be listed in the jar manifest. See "Mixins"
// in LEGACY.md.
mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
    config("$modId.mixins.json")
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", modId)
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("authors", sc.properties["mod.authors"])
            register("license", sc.properties["mod.license"])
            register("description", sc.properties["mod.description"])
            register("minecraft", sc.properties["mod.mc_dep_forgelike"])
            register("forge_range", "[$forgeVersion,)")
            register("loader_range", "[${forgeVersion.substringBefore('.')},)")
        }

        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("fabric.mod.json")
    }

    jar {
        manifest.attributes(mapOf("MixinConfigs" to "$modId.mixins.json"))
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", prop("mod.version"))

        dependsOn("reobfJar")
        from(
            named<AbstractArchiveTask>("reobfJar").flatMap { it.archiveFile },
            named<Jar>("sourcesJar").flatMap { it.archiveFile },
        )
        into(rootProject.layout.buildDirectory.file("libs/${prop("mod.version")}"))
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

    val publishType = prop("publish.type") as String
    if (publishType == "debug") {
        // Enable Debug mode. When enabled, no files will actually be uploaded
        debug.set(true)
    } else {
        versionType.set(publishType)
    }

    val modVersion = sc.properties["mod.version"] as String
    val mcVersionTitle = sc.properties["mod.mc_title"] as String
    val platform = "forge"

    changelog.set(rootProject.file("CHANGELOG.md"))
    projectVersion.set(modVersion)
    // Example: 1.2.3 for 1.20.1 forge
    displayName.set("$modVersion for $mcVersionTitle $platform")
    gameVersions.set((sc.properties["mod.mc_targets"] as String).split(" "))
    loaders.set(listOf(platform))
    artifact.set(tasks.named("reobfJar"))
}

fun validatedProp(prop: String, env: String, action: BiConsumer<String, String>) {
    val projectID = prop(prop) as String?
    val apiKey = System.getenv(env)
    if (projectID != null && !projectID.startsWith('[') && apiKey != null && apiKey.isNotEmpty()) {
        action.accept(projectID, apiKey)
    }
}