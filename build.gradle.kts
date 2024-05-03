import org.polyfrost.gradle.util.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.polyfrost.multi-version")
    id("org.polyfrost.defaults")
    id("org.polyfrost.defaults.maven-publish")
    `maven-publish`
}

group = "org.polyfrost"

java.withSourcesJar()
tasks.compileKotlin.setJvmDefault(if (platform.mcVersion >= 11400) "all" else "all-compatibility")
loom {
    noServerRunConfigs()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.21")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        languageVersion = "1.6"
        apiVersion = "1.6"
    }
}

tasks.jar {
    manifest {
        attributes(mapOf("FMLModType" to "LIBRARY"))
    }
}

publishing {
    publications {
        create<MavenPublication>("Maven") {
            artifactId = "${rootProject.name.lowercase()}-${project.name}"
            version = rootProject.version.toString()

            artifact(tasks.getByName<Jar>("jar").archiveFile)

            artifact(tasks.getByName<Jar>("sourcesJar").archiveFile) {
                this.classifier = "sources"
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            url = uri("https://repo.polyfrost.org/releases")
            name = "releases"
            credentials(PasswordCredentials::class)
        }
        maven {
            url = uri("https://repo.polyfrost.org/snapshots")
            name = "snapshots"
            credentials(PasswordCredentials::class)
        }
        maven {
            url = uri("https://repo.polyfrost.org/private")
            name = "private"
            credentials(PasswordCredentials::class)
        }
    }
}