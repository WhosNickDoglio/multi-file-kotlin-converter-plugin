/*
 * Copyright 2019 Nicholas Doglio
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
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.pandora.plugin.CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID
import com.pandora.plugin.ConversionException
import com.pandora.plugin.anyJavaFileSelected
import com.pandora.plugin.logger
import com.pandora.plugin.writeCommitHistory

internal class ConvertSelectedFileToKotlinWithHistory : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    @Suppress("Deprecation")
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectBase = project.baseDir

        try {
            @Suppress("UseOrEmpty")
            val fileArray = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: emptyArray()
            fileArray.forEach { logger.info("Preparing to convert file: $it") }

            writeCommitHistory(
                project = project,
                projectBase = projectBase,
                files = fileArray,
                onFinish = { successful ->
                    if (fileArray.isEmpty() || !successful) {
                        return@writeCommitHistory
                    }

                    val overrideEvent =
                        AnActionEvent(
                            e.dataContext(fileArray),
                            e.presentation,
                            e.place,
                            ActionUiKind.NONE,
                            e.inputEvent,
                            e.modifiers,
                            e.actionManager,
                        )
                    ActionManager.getInstance()
                        .getAction(CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID)
                        ?.actionPerformed(overrideEvent)
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
        e.presentation.isEnabled = e.anyJavaFileSelected()
    }
}
