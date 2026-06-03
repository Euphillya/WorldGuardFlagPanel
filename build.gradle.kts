plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven {
        name = "faststatsReleases"
        url = uri("https://repo.faststats.dev/releases")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.11-SNAPSHOT")

    compileOnly("com.electronwill.night-config:toml:3.8.4")
    compileOnly("com.electronwill.night-config:core:3.8.4")
    compileOnly("dev.faststats.metrics:bukkit:0.24.1") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.8")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
