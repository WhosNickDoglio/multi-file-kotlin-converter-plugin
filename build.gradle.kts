import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij)
    alias(libs.plugins.detekt)
}

group = "com.pandora.plugin"

version = "0.4.3"

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

intellijPlatform {
    pluginConfiguration {
        version = providers.provider<String> { project.version as String }
        changeNotes =
            """
            <ul>
              <li>0.1: Initial version of the Multiple File Kotlin Converter plugin for IntelliJ IDEA.</li>
              <li>0.2: Fixing small issues found in testing with cancellation and layout.</li>
              <li>0.3: Save commit message during a single session.</li>
              <li>0.4: Fix Deprecated project root usage.</li>
            </ul>
        """
                .trimIndent()
    }
    publishing { token = providers.gradleProperty("jetbrains.publish.token") }
    instrumentCode.set(false)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

detekt {
    autoCorrect = true
    source.setFrom(project.layout.projectDirectory.asFile)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
    compilerOptions.jvmTarget = JvmTarget.fromTarget(libs.versions.jdkTaret.get())
}

tasks.withType(Detekt::class.java).configureEach {
    jvmTarget = libs.versions.jdkTaret.get()
}

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(version = "2024.3.3")
        bundledPlugin("com.intellij.java")

        pluginVerifier()
        zipSigner()
    }

    detektPlugins(libs.detekt.formatting)
    testImplementation(libs.junit)
}
