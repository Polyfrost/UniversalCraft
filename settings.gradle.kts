pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://repo.polyfrost.org/releases")
    }
    plugins {
        val egtVersion = "0.6.8"
        id("org.polyfrost.multi-version.root") version egtVersion
        id("org.polyfrost.multi-version.api-validation") version egtVersion
    }
}

rootProject.name = "UniversalCraft"
rootProject.buildFileName = "root.gradle.kts"

include(":standalone")
include(":standalone:example")

listOf(
    "1.8.9-forge",
    "1.12.2-forge",
    "1.8.9-fabric",
    "1.12.2-fabric",
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}