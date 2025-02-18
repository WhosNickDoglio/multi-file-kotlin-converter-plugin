plugins {
  id("org.jetbrains.kotlin.jvm") version "1.8.10"
  id("org.jetbrains.intellij") version "1.17.4"
  id("io.gitlab.arturbosch.detekt") version "1.9.1"
}

group = "com.pandora.plugin"

version = "0.4.3"

intellij {
  version.set("2019.2")
  updateSinceUntilBuild.set(false)
  plugins.set(listOf("java"))
  instrumentCode.set(false)
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

detekt {
  parallel = true
  buildUponDefaultConfig = true
  input = files("$projectDir")
}

repositories {
  mavenCentral()
}

tasks.patchPluginXml.configure {
  changeNotes.set(
      """
    <ul>
      <li>0.1: Initial version of the Multiple File Kotlin Converter plugin for IntelliJ IDEA.</li>
      <li>0.2: Fixing small issues found in testing with cancellation and layout.</li>
      <li>0.3: Save commit message during a single session.</li>
      <li>0.4: Fix Deprecated project root usage.</li>
    </ul>
    """)
}

tasks.runIde.configure { jbrArch.set("x64") }

tasks.publishPlugin.configure {
  token.set(project.properties["jetbrains.publish.token"] as? String)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
  kotlinOptions.jvmTarget = "11"
}

tasks.detekt.configure { jvmTarget = "1.8" }

dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.9.1")
  testImplementation("junit:junit:4.13.2")
}
