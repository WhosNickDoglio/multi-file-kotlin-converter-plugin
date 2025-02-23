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

interface WriteCommitService {
    fun commitChanges(
        message: String,
        projectBase: VirtualFile,
        files: Array<VirtualFile>,
        onCommit: (Boolean) -> Unit,
    )
}

@Service(Service.Level.PROJECT)
class WriteCommitServiceImpl(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) : WriteCommitService {
    override fun commitChanges(
        message: String,
        projectBase: VirtualFile,
        files: Array<VirtualFile>,
        onCommit: (Boolean) -> Unit,
    ) {
        if (message.isNullOrBlank()) {
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
