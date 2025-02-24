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
package com.pandora.plugin.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.pandora.plugin.CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID
import com.pandora.plugin.ConversionException
import com.pandora.plugin.logger
import com.pandora.plugin.ui.MultiFileFinderDialog
import com.pandora.plugin.ui.SearchDialog
import com.pandora.plugin.writeCommitHistory

/**
 * Custom action executing the following steps on each selected file(s):
 * 0. Request user to enter list of files for conversion
 * 0. (Optional) Rename step in GIT
 * 0. (Optional) Simple file extension rename for GIT (`.java` to `.kt`)
 * 0. (Optional) Commit to GIT, with editable commit message
 * 0. (Optional) Rename file back to `.java`
 * 0. Use Native `ConvertJavaToKotlin` action to convert requested files.
 *
 * @see Link to 'ConvertJavaToKotlin' official source code
 *   https://github.com/JetBrains/kotlin/blob/master/idea/src/org/jetbrains/kotlin/idea/actions/JavaToKotlinAction.kt
 */
internal class ConvertListOfFilesToKotlinWithHistory : AnAction() {
    // region Plugin implementation

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    @Suppress("UseOrEmpty", "LongMethod")
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectBase = project.guessProjectDir() ?: return

        try {
            val dialogResult =
                MultiFileFinderDialog.showInputDialogWithCheckBox(
                    SearchDialog(
                        "Enter files to convert: (newline separated)",
                        "Files to convert",
                        "Automatically rename files in VCS",
                    ),
                    null,
                    "",
                    null,
                )

            val fileArray = fromFileList(projectBase, dialogResult.first) ?: emptyArray()

            fileArray.forEach { logger.info("Preparing to convert file: $it") }
            // TODO need to figure out how this works if list contains files from multiple modules
            val modules = ModuleManager.getInstance(project).modules.first()
            writeCommitHistory(
                project = project,
                projectBase = projectBase,
                files = fileArray,
                onFinish = { successful ->
                    if (fileArray.isEmpty() || (dialogResult.second && !successful)) {
                        return@writeCommitHistory
                    }

                    val dataContext =
                        SimpleDataContext.builder()
                            .setParent(e.dataContext)
                            .add(PlatformDataKeys.VIRTUAL_FILE_ARRAY, fileArray)
                            .add(PlatformDataKeys.MODULE, modules)
                            .build()

                    val overrideEvent =
                        AnActionEvent(
                            dataContext,
                            e.presentation,
                            e.place,
                            ActionUiKind.NONE,
                            e.inputEvent,
                            e.modifiers,
                            e.actionManager,
                        )

                    val javaConverterAction =
                        ActionManager.getInstance().getAction(CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID)
                    javaConverterAction?.actionPerformed(overrideEvent)
                },
            )
        } catch (e: ConversionException) {
            if (e.isError) {
                logger.error(
                    "Problem running conversion plugin: ${e.message}\n" +
                        "${e.stackTrace.joinToString("\n")}\n" +
                        "----------"
                )
            } else {
                logger.info(e.message, e.cause)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }

    // endregion

    // region Utility function

    private fun fromFileList(projectBase: VirtualFile, files: String): Array<VirtualFile>? {
        if (files.isBlank()) return null
        val fileNames = files.lines()
        fileNames.forEach { logger.debug("Found file: $it") }

        return projectBase.findMatchingChildren { file ->
            fileNames.indexOfFirst { file.canonicalPath?.endsWith(it) == true } >= 0
        }
    }

    // endregion
}
