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
package com.pandora.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.actions.VcsContextFactory
import com.intellij.openapi.vcs.changes.CurrentContentRevision
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import java.io.IOException

val logger = Logger.getInstance("Kotlin Converter")

/**
 * Official identifier of the native `ConvertJavaToKotlin` action.
 */
const val CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID = "ConvertJavaToKotlin"

const val SUGGESTED_COMMIT_MESSAGE = "Converting files to Kotlin with safe renaming."

const val KOTLIN_EXTENSION = "kt"

const val JAVA_EXTENSION = "java"

private var lastCommitMessage = SUGGESTED_COMMIT_MESSAGE

internal const val DIALOG_SIZE = 500

internal fun writeCommitHistory(
    project: Project,
    projectBase: VirtualFile,
    files: Array<VirtualFile>,
    onFinish: (Boolean) -> Unit
): Boolean {
    val commitMessage = Messages.showInputDialog(
        project,
        "Commit Message for Conversion:",
        "Enter a commit message",
        null,
        lastCommitMessage,
        null
    )
    if (commitMessage.isNullOrBlank()) {
        throw ConversionException("Commit Message cannot be empty")
    }
    lastCommitMessage = commitMessage

    val writeCommitHistoryService = project.service<WriteCommitService>()

    writeCommitHistoryService.commitChanges(
        message = lastCommitMessage,
        projectBase = projectBase,
        files = files,
        onCommit = { successful ->
            if (!successful) {
                Messages.showDialog("No files found to commit.", "Nothing to commit", emptyArray(), 0, null)
            }
            onFinish(successful)
        }
    )

    return false
}

internal fun renameFile(project: Project, virtualFile: VirtualFile, newName: String) {
    logger.info("Renaming file `${virtualFile.name}` to `$newName`")

    WriteCommandAction.runWriteCommandAction(project) {
        try {
            virtualFile.rename(project, newName)
        } catch (e: IOException) {
            throw ConversionException("Error while renaming file `${virtualFile.name}` to `$newName`", true, e)
        }
    }
}

internal fun VirtualFile.contentRevision(): CurrentContentRevision {
    val contextFactory = VcsContextFactory.SERVICE.getInstance()
    val path = contextFactory.createFilePathOn(this)
    return CurrentContentRevision(path)
}

internal fun AnActionEvent.anyJavaFileSelected(): Boolean = runReadAction {
    val projectRef = project
    val files = getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
    return@runReadAction projectRef != null && files != null && anyJavaFileSelected(projectRef, files)
}

private fun anyJavaFileSelected(project: Project, files: Array<out VirtualFile>): Boolean {
    val manager = PsiManager.getInstance(project)
    return files.any { manager.findFile(it) is PsiJavaFile && it.isWritable } ||
        files.any { it.isDirectory && anyJavaFileSelected(project, it.children) }
}
