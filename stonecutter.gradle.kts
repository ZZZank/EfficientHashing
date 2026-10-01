plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.20.1-fabric"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    // Makes version- and loader-specific properties apply from `stonecutter.properties.toml`
    properties {
        tags(version, loader)
    }

    // Adds constants to Stonecutter comments (i.e. for `//? if fabric { ... }`)
    constants {
        match(loader, "fabric", "forge", "neoforge")
    }

    swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
}

// Runs the client/server of whichever version is currently active
for (type in listOf("Client", "Server")) {
    tasks.register("runActive$type") {
        group = "project"
        dependsOn(":${stonecutter.current!!.project}:run$type")
    }
}
