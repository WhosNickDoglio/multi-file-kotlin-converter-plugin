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
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.MultiLineLabelUI
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.util.SystemInfo
import com.pandora.plugin.options.IntComparison
import com.pandora.plugin.options.PanelOption
import com.pandora.plugin.options.SearchOptions
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants
import org.jetbrains.annotations.Nls

@Suppress("Deprecation")
internal class FileSearchDialog(
    project: Project,
    private val message: String,
    @Nls(capitalization = Nls.Capitalization.Title) title: String,
    val checkboxText: String,
    val checked: Boolean,
    val checkboxEnabled: Boolean,
) : DialogWrapper(project) {
    private lateinit var commitCheckBox: JCheckBox
    private lateinit var regexCheckBox: JCheckBox
    private lateinit var regexInput: JTextField
    private lateinit var lineCountCheckBox: JCheckBox
    private lateinit var lineCountCombo: ComboBox<IntComparison>
    private lateinit var lineCountInput: ComboBox<Int>

    init {
        this.title = title
        if (!SystemInfo.isMac) {
            setButtonsAlignment(SwingConstants.CENTER)
        }

        init()
    }

    private val commit
        get() = commitCheckBox.isSelected

    private val regexText
        get() = if (regexCheckBox.isSelected) regexInput.text else null

    private val lineCount
        get() =
            (if (lineCountCheckBox.isSelected) lineCountInput.selectedItem as Int? else null) ?: -1

    private val lineCountFunction: (Int, Int) -> Boolean
        get() =
            if (lineCountCheckBox.isSelected) {
                (lineCountCombo.selectedItem as IntComparison).function
            } else {
                { _, _ -> true }
            }

    @Suppress("MagicNumber") // Bottom padding is just a nudge
    private fun createTextComponent(str: String): JComponent {
        val textLabel = JLabel(str)
        textLabel.setUI(MultiLineLabelUI())
        textLabel.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)
        return textLabel
    }

    override fun createCenterPanel(): JComponent? {
        val messagePanel =
            JPanel(
                VerticalFlowLayout(VerticalFlowLayout.TOP or VerticalFlowLayout.LEFT, true, false)
            )

        messagePanel.add(createTextComponent(message))
        val regexPanel = JPanel(GridBagLayout())
        regexCheckBox = JCheckBox()
        regexCheckBox.text = "Regex (extensions allowed, not full filepath)"
        regexCheckBox.isSelected = lastRegexUsed
        regexCheckBox.isEnabled = true
        regexPanel.add(regexCheckBox, PanelOption.WRAP.constraints)

        regexInput = JTextField()
        regexInput.text = lastRegex
        regexPanel.add(regexInput, PanelOption.FILL.constraints)
        messagePanel.add(regexPanel)

        val lineCountPanel = JPanel(GridBagLayout())
        lineCountCheckBox = JCheckBox()
        lineCountCheckBox.text = "Line Count"
        lineCountCheckBox.isSelected = lastSizeUsed
        lineCountCheckBox.isEnabled = true
        lineCountPanel.add(lineCountCheckBox, PanelOption.WRAP.constraints)

        lineCountCombo = ComboBox(IntComparison.values())
        lineCountCombo.selectedItem = lastSizeType
        lineCountPanel.add(lineCountCombo, PanelOption.WRAP.constraints)

        lineCountInput = ComboBox((0..MAX_LINES).toList().toTypedArray())
        lineCountInput.selectedItem = lastSize
        lineCountPanel.add(lineCountInput, PanelOption.FILL.constraints)

        messagePanel.add(lineCountPanel)

        commitCheckBox = JCheckBox()
        commitCheckBox.text = checkboxText
        commitCheckBox.isSelected = checked
        commitCheckBox.isEnabled = checkboxEnabled

        messagePanel.add(commitCheckBox)

        return messagePanel
    }

    companion object {
        private const val MAX_LINES = 10_000
        private var lastRegexUsed: Boolean = false
        private var lastRegex: String = ""
        private var lastSizeUsed: Boolean = false
        private var lastSize: Int = 0
        private var lastSizeType: IntComparison = IntComparison.LESS_THAN

        fun showSearchDialog(project: Project, searchDialog: SearchDialog): SearchOptions? {
            val dialog =
                FileSearchDialog(
                    project = project,
                    message = searchDialog.message,
                    title = searchDialog.title,
                    checkboxText = searchDialog.checkboxText,
                    checked = searchDialog.checked,
                    checkboxEnabled = searchDialog.checkboxEnabled,
                )
            dialog.show()
            lastRegexUsed = dialog.regexCheckBox.isSelected
            lastRegex = dialog.regexInput.text
            lastSizeUsed = dialog.lineCountCheckBox.isSelected
            lastSize = dialog.lineCountInput.selectedItem as Int? ?: -1
            lastSizeType = dialog.lineCountCombo.selectedItem as IntComparison

            return if (dialog.isOK) {
                SearchOptions(
                    dialog.commit,
                    dialog.regexText,
                    dialog.lineCount,
                    dialog.lineCountFunction,
                )
            } else {
                null
            }
        }
    }
}
