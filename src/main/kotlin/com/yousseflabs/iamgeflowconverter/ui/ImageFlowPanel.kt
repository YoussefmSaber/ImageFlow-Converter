package com.yousseflabs.iamgeflowconverter.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.yourplugin.imageflow.ui.components.ChipsRowPanel
import com.yousseflabs.iamgeflowconverter.conversion.ConversionTask
import com.yousseflabs.iamgeflowconverter.model.image.ImageFile
import com.yousseflabs.iamgeflowconverter.ui.components.ImageListPanel
import com.yousseflabs.iamgeflowconverter.util.ProjectScanner
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.UIManager

class ImageFlowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private var allImages: List<ImageFile> = emptyList()

    private val listPanel     = ImageListPanel { refreshConvertButton() }
    private val chipsPanel    = ChipsRowPanel  { applyFilter() }
    private val convertButton = JButton("Select images to convert").apply {
        isEnabled = false
        addActionListener { onConvertClicked() }
    }

    init {
        background = UIManager.getColor("ToolWindow.background")
        add(buildTopBar(),     BorderLayout.NORTH)
        add(buildCenterArea(), BorderLayout.CENTER)
        add(buildBottomBar(),  BorderLayout.SOUTH)
        DumbService.getInstance(project).runWhenSmart { scanAndPopulate() }
    }

    private fun buildTopBar(): JPanel {
        val refreshBtn = JButton("↺").apply {
            toolTipText         = "Refresh"
            isBorderPainted     = false
            isContentAreaFilled = false
            addActionListener { scanAndPopulate() }
        }
        return JPanel(BorderLayout()).apply {
            background = UIManager.getColor("ToolWindow.background")
            border     = BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border())
            add(chipsPanel, BorderLayout.CENTER)
            add(refreshBtn, BorderLayout.EAST)
        }
    }

    private fun buildCenterArea(): JPanel {
        return JPanel(BorderLayout()).apply {
            background = UIManager.getColor("ToolWindow.background")
            border     = JBUI.Borders.empty(4)
            add(listPanel, BorderLayout.CENTER)
        }
    }

    private fun buildBottomBar(): JPanel {
        return JPanel(FlowLayout(FlowLayout.RIGHT, 8, 6)).apply {
            background = UIManager.getColor("ToolWindow.background")
            border     = BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border())
            add(convertButton)
        }
    }

    private fun scanAndPopulate() {
        listPanel.showStatus("Scanning project…")
        ApplicationManager.getApplication().executeOnPooledThread {
            val images = ProjectScanner.scan(project)
            SwingUtilities.invokeLater {
                allImages = images
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filtered = allImages.filter { it.extension in chipsPanel.activeFilters }
        listPanel.populate(filtered, allEmpty = allImages.isEmpty())
    }

    private fun refreshConvertButton() {
        val count = listPanel.checkedItems.size
        convertButton.isEnabled = count > 0
        convertButton.text = if (count == 0) "Select images to convert" else "Convert $count image(s)"
    }

    private fun onConvertClicked() {
        val toConvert = listPanel.checkedItems.toList()
        val dialog    = ConversionOptionsDialog(project, toConvert)
        if (!dialog.showAndGet()) return

        ConversionTask(
            project    = project,
            images     = toConvert,
            options    = dialog.buildOptions(),
            onFinished = { results ->
                SwingUtilities.invokeLater {
                    val ok   = results.count { it.success }
                    val fail = results.count { !it.success }
                    JOptionPane.showMessageDialog(
                        this,
                        "✅ $ok converted" + if (fail > 0) "   ❌ $fail failed" else "",
                        "ImageFlow — Done",
                        JOptionPane.INFORMATION_MESSAGE
                    )
                    listPanel.clearChecked()
                    scanAndPopulate()
                }
            }
        ).queue()
    }
}