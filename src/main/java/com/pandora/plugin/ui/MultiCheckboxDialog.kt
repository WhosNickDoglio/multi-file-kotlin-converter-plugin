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
package com.pandora.plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.ScrollPaneFactory
import com.pandora.plugin.DIALOG_SIZE
import java.awt.Dimension
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants
import org.jetbrains.annotations.Nls

@Suppress("Deprecation")
internal class MultiCheckboxDialog(
    items: Array<out Any>,
    project: Project,
    @Nls(capitalization = Nls.Capitalization.Title) title: String,
    private val itemFormatter: (Any) -> String,
) : DialogWrapper(project) {
    private val checkBoxes = HashMap<Any, JCheckBox>()

    private val allItems = items

    val selectedItems: List<Any>
        get() = checkBoxes.mapNotNull { if (it.value.isSelected) it.key else null }

    init {
        this.title = title
        if (!SystemInfo.isMac) {
            setButtonsAlignment(SwingConstants.CENTER)
        }
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val messagePanel =
            JPanel(
                VerticalFlowLayout(VerticalFlowLayout.TOP or VerticalFlowLayout.LEFT, true, false)
            )
        messagePanel.maximumSize = Dimension(DIALOG_SIZE, DIALOG_SIZE)

        if (allItems.count() > MAX_FILES) {
            messagePanel.add(
                JTextField(
                    "WARNING: Converting more than $MAX_FILES can cause unpredictable results"
                )
            )
        }

        var i = 0
        allItems.forEach {
            JCheckBox().apply {
                isSelected = i < MAX_FILES
                isEnabled = true
                text = itemFormatter.invoke(it)
                checkBoxes[it] = this
                messagePanel.add(this, i++)
            }
        }
        return ScrollPaneFactory.createScrollPane(messagePanel)
    }

    companion object {
        private const val MAX_FILES = 15 // Above this the IntelliJ convert plugin gets flaky.

        fun showMultiCheckboxDialog(
            items: Array<out Any>,
            project: Project,
            @Nls(capitalization = Nls.Capitalization.Title) title: String,
            itemFormatter: (Any) -> String = { "$it" },
        ): List<Any> {
            val dialog =
                MultiCheckboxDialog(
                    items,
                    project = project,
                    title = title,
                    itemFormatter = itemFormatter,
                )
            dialog.show()

            return if (dialog.isOK) dialog.selectedItems else emptyList()
        }
    }
}
