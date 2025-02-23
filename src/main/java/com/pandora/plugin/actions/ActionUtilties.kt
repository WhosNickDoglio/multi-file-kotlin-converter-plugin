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

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.pandora.plugin.JAVA_EXTENSION

/** Provides an overridden [DataContext] using the provided [fileArray] */
@Suppress("DEPRECATION")
internal fun AnActionEvent.dataContext(fileArray: Array<VirtualFile>): DataContext =
    DataContext { data ->
        when (data) {
            PlatformDataKeys.VIRTUAL_FILE_ARRAY.name -> fileArray
            else -> dataContext.getData(data)
        }
    }

/**
 * Searches the directory tree of a given [VirtualFile] for .java files that that can be converted
 */
internal fun VirtualFile.findMatchingChildren(
    matcher: (VirtualFile) -> Boolean
): Array<VirtualFile> =
    buildList {
            VfsUtilCore.visitChildrenRecursively(
                this@findMatchingChildren,
                object : VirtualFileVisitor<Unit>() {
                    override fun visitFile(file: VirtualFile): Boolean {
                        if (file.canConvert && matcher(file)) {
                            add(file)
                        }
                        return true
                    }
                },
            )
        }
        .toTypedArray()

private val VirtualFile.canConvert: Boolean
    get() = extension == JAVA_EXTENSION && isWritable && !isDirectory && !path.contains("/build/")
