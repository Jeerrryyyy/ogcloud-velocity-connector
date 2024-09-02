import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("kapt") version "2.0.0"

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.ogwars"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    implementation("org.kodein.di:kodein-di-jvm:7.22.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.apache.kafka:kafka-clients:3.8.0")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    shadowJar
}