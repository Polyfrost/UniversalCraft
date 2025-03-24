import org.polyfrost.gradle.util.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.polyfrost.multi-version")
    id("org.polyfrost.defaults")
    id("org.polyfrost.defaults.maven-publish")
}

group = "org.polyfrost"

java.withSourcesJar()
tasks.compileKotlin.setJvmDefault("all")
loom.noServerRunConfigs()

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        languageVersion = "1.6"
        apiVersion = "1.6"
    }
}

preprocess {
    vars.put("STANDALONE", 0)
    vars.put("!STANDALONE", 1)
}
