/*
 * Copyright 2025 Nicholas Doglio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See accompanying LICENSE file or you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.diffplug.gradle.spotless.KotlinExtension

/*
 * Copyright 2019 Pandora Media, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See accompanying LICENSE file or you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    alias(libs.plugins.convention.kotlin)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij)
    alias(libs.plugins.detekt)
    alias(libs.plugins.lint)
    alias(libs.plugins.spotless)
    alias(libs.plugins.doctor)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.sortDependencies)
    alias(libs.plugins.kover)
}

group = "com.pandora.plugin"

version = "0.4.3"

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

val pathPrefix = "src/main/java/com/pandora/plugin"
val externalFiles =
    arrayOf(
        "$pathPrefix/FileUtils.kt",
        "$pathPrefix/ConversionException.kt",
        "$pathPrefix/ui/SearchDialog.kt",
        "$pathPrefix/ui/MultiFileFinderDialog.kt",
        "$pathPrefix/ui/MultiCheckboxDialog.kt",
        "$pathPrefix/ui/FileSearchDialog.kt",
        "$pathPrefix/options/SearchOptions.kt",
        "$pathPrefix/options/PanelOption.kt",
        "$pathPrefix/options/IntComparison.kt",
        "$pathPrefix/actions/SearchAndConvertFilesToKotlinWithHistory.kt",
        "$pathPrefix/actions/ConvertSelectedFilesToKotlinWithHistory.kt",
        "$pathPrefix/actions/ConvertListOfFilesToKotlinWithHistory.kt",
        "$pathPrefix/actions/ActionUtilities.kt",
    )

spotless {
    kotlin { targetExclude("spotless/*.kt", "**/spotless/*.kt", *externalFiles) }
    // Externally adapted sources that should preserve their license header
    format("kotlinExternal", KotlinExtension::class.java) {
        target(*externalFiles)
        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeaderFile(rootProject.file("spotless/spotless-external.kt"))
    }
}

repositories {
    mavenCentral()
    google()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(version = "2024.3.3")
        bundledPlugin("com.intellij.java")

        pluginVerifier()
        zipSigner()
    }

    testImplementation(libs.junit)
    testImplementation(libs.testParameterInjector)
}
