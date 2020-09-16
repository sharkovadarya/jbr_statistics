import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "ru.hse.spb.sharkova"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.eclipse.jgit", "org.eclipse.jgit", "5.9.0.202009080501-r")
    implementation("org.kohsuke", "github-api", "1.116")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")


    testImplementation("junit", "junit", "4.12")
}

tasks.withType<ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "ru.hse.spb.sharkova.datainfr.MainKt"
    }
}