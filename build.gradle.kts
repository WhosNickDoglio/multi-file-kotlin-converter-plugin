buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.8.10'
    id 'org.jetbrains.intellij' version '1.17.4'
    id "io.gitlab.arturbosch.detekt" version "1.9.1"
}

group 'com.pandora.plugin'
version '0.4.3'

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}



tasks.patchPluginXml {
    changeNotes = """
    <ul>
      <li>0.1: Initial version of the Multiple File Kotlin Converter plugin for IntelliJ IDEA.</li>
      <li>0.2: Fixing small issues found in testing with cancellation and layout.</li>
      <li>0.3: Save commit message during a single session.</li>
      <li>0.4: Fix Deprecated project root usage.</li>
    </ul>
    """
}

intellij {
    version = "2019.2"
    updateSinceUntilBuild = false
    plugins = ["java"]
    instrumentCode.set(false)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation group: 'junit', name: 'junit', version: '4.12'
}

tasks.runIde.configure {
    jbrArch.set("x64")
}


tasks.publishPlugin {
    token = project.properties['jetbrains.publish.token']
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions.jvmTarget = "11"
}

apply plugin: 'io.gitlab.arturbosch.detekt'

tasks.detekt.jvmTarget = "1.8"

detekt {
    parallel = true
    buildUponDefaultConfig = true
    input = files("$projectDir")
}

dependencies {
    detektPlugins "io.gitlab.arturbosch.detekt:detekt-formatting:1.9.1"
}
