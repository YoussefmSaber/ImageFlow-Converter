package com.yousseflabs.iamgeflowconverter.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.yourplugin.imageflow.ui.components.ChipsRowPanel
import com.yousseflabs.iamgeflowconverter.conversion.ConversionTask
import com.yousseflabs.iamgeflowconverter.ui.components.ImageListPanel
import com.yousseflabs.iamgeflowconverter.model.conversion.ConversionResult
import com.yousseflabs.iamgeflowconverter.model.image.ImageFile
import com.yousseflabs.iamgeflowconverter.util.ProjectScanner
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities

class ConverterDialog(private val project: Project) : DialogWrapper(project) {

    private var allImages: List<ImageFile> = emptyList()

    private val listPanel  = ImageListPanel { refreshConvertButton() }
    private val chipsPanel = ChipsRowPanel  { applyFilter() }

    private lateinit var convertBtn: JButton

    init {
        title   = "ImageFlow Converter"
        isModal = false
        setSize(820, 580)
        init()
        scanAndPopulate()
    }

    override fun createCenterPanel(): JComponent =
        JPanel(BorderLayout()).apply {
            add(chipsPanel, BorderLayout.NORTH)
            add(listPanel,  BorderLayout.CENTER)
        }

    override fun createSouthPanel(): JComponent {
        convertBtn = JButton("Select images to convert").apply {
            isEnabled = false
            addActionListener { onConvert() }
        }
        val closeBtn = JButton("Close").apply { addActionListener { doCancelAction() } }
        return JPanel(FlowLayout(FlowLayout.RIGHT, 8, 8)).apply {
            border = BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border())
            add(closeBtn)
            add(convertBtn)
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
        convertBtn.isEnabled = count > 0
        convertBtn.text = if (count == 0) "Select images to convert" else "Convert $count image(s)"
    }

    private fun onConvert() {
        val toConvert = listPanel.checkedItems.toList()
        val dialog    = ConversionOptionsDialog(project, toConvert)
        if (!dialog.showAndGet()) return

        ConversionTask(
            project = project,
            images = toConvert,
            options = dialog.buildOptions(),
            onFinished = { results: List<ConversionResult> ->
                SwingUtilities.invokeLater {
                    val ok = results.count { it.success }
                    val fail = results.count { !it.success }
                    JOptionPane.showMessageDialog(
                        contentPanel,
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
