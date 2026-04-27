package com.yousseflabs.iamgeflowconverter.ui.components

import com.intellij.openapi.ui.ComboBox
import com.intellij.util.ui.JBUI
import com.yousseflabs.iamgeflowconverter.detection.DrawableDirectory
import com.yousseflabs.iamgeflowconverter.model.conversion.ConversionMode
import com.yousseflabs.iamgeflowconverter.model.conversion.ConversionOptions
import com.yousseflabs.iamgeflowconverter.model.conversion.DensityVariant
import com.yousseflabs.iamgeflowconverter.model.conversion.OutputFormat
import com.yousseflabs.iamgeflowconverter.model.conversion.ResizeMode
import java.awt.*
import javax.swing.*

class OptionsFormPanel(
    private val drawableDirs: List<DrawableDirectory>
) : JPanel(GridBagLayout()) {

    val formatCombo = ComboBox(OutputFormat.entries.map { it.displayName }.toTypedArray())
    val modeCombo = ComboBox(arrayOf("Lossy", "Lossless"))
    val qualitySlider = JSlider(0, 100, 80)
    val qualityLabel = JLabel("80")

    val resizeCheck = JCheckBox("Resize images")
    val scaleRadio = JRadioButton("Scale %", true)
    val exactRadio = JRadioButton("Width × Height")
    val scaleSpinner = JSpinner(SpinnerNumberModel(100, 1, 1000, 5))
    val widthSpinner = JSpinner(SpinnerNumberModel(512, 1, 99999, 1))
    val heightSpinner = JSpinner(SpinnerNumberModel(512, 1, 99999, 1))
    val keepAspectCheck = JCheckBox("Keep aspect ratio", true)

    val densityCheck = JCheckBox("Generate density variants")
    val densityBoxes = DensityVariant.entries.associateWith { v ->
        JCheckBox(v.suffix, v == DensityVariant.XHDPI)
    }

    val deleteOriginalsCheck = JCheckBox("Delete originals after conversion")
    val outputDirCombo = ComboBox<String>().apply {
        addItem("Same folder as source")
        drawableDirs.forEach { addItem("[${it.moduleName}] ${it.dir.path}") }
    }

    init {
        border = JBUI.Borders.empty(8)
        buildForm()
        syncFormatControls()
        syncExactSpinners()
    }

    private fun buildForm() {
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(3)
        }
        var r = 0

        fun header(text: String) {
            gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 2
            gbc.weightx = 1.0; gbc.insets = JBUI.insets(10, 3, 2, 3)
            add(JLabel(text).apply { font = font.deriveFont(Font.BOLD, 11f) }, gbc)
            gbc.insets = JBUI.insets(3)
        }

        fun row(label: String, comp: JComponent) {
            gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.0; gbc.gridwidth = 1
            add(JLabel("$label:"), gbc)
            gbc.gridx = 1; gbc.weightx = 1.0
            add(comp, gbc)
            r++
        }

        fun wide(comp: JComponent) {
            gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 2; gbc.weightx = 1.0
            add(comp, gbc)
        }

        // ── Output Format ──────────────────────────────────────────────────────
        header("Output Format")
        row("Format", formatCombo)
        row("Mode", modeCombo)
        row("Quality", JPanel(BorderLayout(4, 0)).apply {
            add(qualitySlider, BorderLayout.CENTER)
            add(qualityLabel, BorderLayout.EAST)
        })

        qualitySlider.addChangeListener { qualityLabel.text = qualitySlider.value.toString() }
        modeCombo.addActionListener {
            qualitySlider.isEnabled = modeCombo.selectedIndex == 0
            qualityLabel.isEnabled = modeCombo.selectedIndex == 0
        }
        formatCombo.addActionListener { syncFormatControls() }

        // ── Resize ─────────────────────────────────────────────────────────────
        header("Resize")
        wide(resizeCheck)

        ButtonGroup().apply { add(scaleRadio); add(exactRadio) }

        val resizeOptionsPanel = JPanel(GridLayout(2, 1, 0, 2)).apply {
            add(JPanel(FlowLayout(FlowLayout.LEFT, 4, 0)).apply {
                add(scaleRadio)
                add(scaleSpinner.apply { preferredSize = Dimension(70, preferredSize.height) })
                add(JLabel("%"))
            })
            add(JPanel(FlowLayout(FlowLayout.LEFT, 4, 0)).apply {
                add(exactRadio)
                add(widthSpinner.apply { preferredSize = Dimension(75, preferredSize.height) })
                add(JLabel("×"))
                add(heightSpinner.apply { preferredSize = Dimension(75, preferredSize.height) })
                add(JLabel("px"))
                add(keepAspectCheck)
            })
            isVisible = false
            border = JBUI.Borders.emptyLeft(20)
        }
        wide(resizeOptionsPanel)

        resizeCheck.addActionListener {
            resizeOptionsPanel.isVisible = resizeCheck.isSelected
            syncExactSpinners()
        }
        scaleRadio.addActionListener { syncExactSpinners() }
        exactRadio.addActionListener { syncExactSpinners() }
        keepAspectCheck.addActionListener { syncExactSpinners() }

        // ── Density Variants ───────────────────────────────────────────────────
        header("Density Variants")
        wide(densityCheck)

        val densityPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2)).apply {
            densityBoxes.values.forEach { add(it) }
            isVisible = false
            border = JBUI.Borders.emptyLeft(20)
        }
        wide(densityPanel)
        densityCheck.addActionListener { densityPanel.isVisible = densityCheck.isSelected }

        // ── Output ─────────────────────────────────────────────────────────────
        header("Output")
        row("Save to", outputDirCombo)
        wide(deleteOriginalsCheck)

        // Spacer
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2
        gbc.weightx = 1.0; gbc.weighty = 1.0
        add(JPanel(), gbc)
    }

    fun syncFormatControls() {
        val isPng = OutputFormat.entries[formatCombo.selectedIndex] == OutputFormat.PNG
        modeCombo.isEnabled = !isPng
        qualitySlider.isEnabled = !isPng && modeCombo.selectedIndex == 0
        qualityLabel.isEnabled = qualitySlider.isEnabled
    }

    fun syncExactSpinners() {
        val exactActive = resizeCheck.isSelected && exactRadio.isSelected
        heightSpinner.isEnabled = exactActive && !keepAspectCheck.isSelected
        widthSpinner.isEnabled = exactActive
        scaleSpinner.isEnabled = resizeCheck.isSelected && scaleRadio.isSelected
    }

    fun buildOptions(): ConversionOptions {
        val format = OutputFormat.entries[formatCombo.selectedIndex]
        val resizeMode = when {
            !resizeCheck.isSelected -> ResizeMode.NONE
            scaleRadio.isSelected -> ResizeMode.SCALE_PERCENT
            else -> ResizeMode.EXACT_DIMENSIONS
        }
        val targetW =
            if (resizeMode == ResizeMode.EXACT_DIMENSIONS) widthSpinner.value as Int else null
        val targetH = if (resizeMode == ResizeMode.EXACT_DIMENSIONS && !keepAspectCheck.isSelected)
            heightSpinner.value as Int else null
        val variants = densityBoxes
            .filter { (_, cb) -> cb.isSelected && densityCheck.isSelected }
            .keys.toSet()

        return ConversionOptions(
            outputFormat = format,
            mode = if (modeCombo.selectedIndex == 0) ConversionMode.LOSSY else ConversionMode.LOSSLESS,
            quality = qualitySlider.value,
            resizeMode = resizeMode,
            scalePercent = scaleSpinner.value as Int,
            targetWidth = targetW,
            targetHeight = targetH,
            deleteOriginals = deleteOriginalsCheck.isSelected,
            generateDensityVariants = densityCheck.isSelected,
            densityVariants = variants,
            outputDirectory = if (outputDirCombo.selectedIndex == 0) null
            else drawableDirs[outputDirCombo.selectedIndex - 1].dir.absolutePath
        )
    }
}