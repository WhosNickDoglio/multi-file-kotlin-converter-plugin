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
package com.pandora.plugin

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal interface WriteCommitService {
    fun commitChanges(
        message: String,
        projectBase: VirtualFile,
        files: Array<VirtualFile>,
        onCommit: (Boolean) -> Unit,
    )
}

@Service(Service.Level.PROJECT)
internal class WriteCommitServiceImpl(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) : WriteCommitService {
    override fun commitChanges(
        message: String,
        projectBase: VirtualFile,
        files: Array<VirtualFile>,
        onCommit: (Boolean) -> Unit,
    ) {
        if (message.isBlank()) {
            throw ConversionException("Commit Message cannot be empty")
        }

        coroutineScope.launch {
            val finalVcs =
                VcsUtil.getVcsFor(project, projectBase)
                    ?: throw ConversionException(
                        "Unable to find Version Control for selected project"
                    )
            val changes =
                files
                    .mapNotNull {
                        logger.info("File $it has extension: ${it.extension}")
                        if (it.extension != JAVA_EXTENSION) return@mapNotNull null
                        val before = it.contentRevision()
                        logger.info("Found file ${before.file}")
                        renameFile(project, it, "${it.nameWithoutExtension}.$KOTLIN_EXTENSION")
                        val after = it.contentRevision()
                        logger.info("Renamed file ${before.file} -> ${after.file}")
                        Change(before, after)
                    }
                    .toList()
            if (changes.isNotEmpty()) {
                finalVcs.checkinEnvironment?.commit(changes, message)
                files
                    .filter { it.extension == KOTLIN_EXTENSION }
                    .forEach {
                        renameFile(project, it, "${it.nameWithoutExtension}.$JAVA_EXTENSION")
                    }

                withContext(Dispatchers.EDT) { onCommit(true) }
            } else {
                withContext(Dispatchers.EDT) { onCommit(false) }
            }
        }
    }
}
