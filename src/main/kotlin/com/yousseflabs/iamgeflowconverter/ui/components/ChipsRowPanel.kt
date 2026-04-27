package com.yourplugin.imageflow.ui.components

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.yousseflabs.iamgeflowconverter.model.image.ImageExtension
import java.awt.Color
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JToggleButton

class ChipsRowPanel(
    private val onFilterChanged: (Set<ImageExtension>) -> Unit
) : JPanel(FlowLayout(FlowLayout.LEFT, 6, 6)) {

    var activeFilters: Set<ImageExtension> = ImageExtension.entries.toHashSet()
        private set

    private val chipButtons = mutableMapOf<ImageExtension, JToggleButton>()
    private lateinit var allChip: JToggleButton

    init { buildChips() }

    private fun buildChips() {
        allChip = makeChip("All", selected = true).apply {
            addActionListener {
                isSelected    = true
                activeFilters = ImageExtension.entries.toHashSet()
                chipButtons.values.forEach { it.isSelected = true }
                refreshChipColors()
                onFilterChanged(activeFilters)
            }
        }
        add(allChip)

        ImageExtension.entries.forEach { ext ->
            val chip = makeChip(ext.displayName, selected = true).apply {
                addActionListener {
                    activeFilters = if (isSelected) activeFilters + ext else activeFilters - ext
                    allChip.isSelected = activeFilters.size == ImageExtension.entries.size
                    refreshChipColors()
                    onFilterChanged(activeFilters)
                }
            }
            chipButtons[ext] = chip
            add(chip)
        }
        refreshChipColors()
    }

    private fun makeChip(label: String, selected: Boolean) =
        JToggleButton(label, selected).apply {
            isFocusPainted      = false
            isContentAreaFilled = false
            font   = font.deriveFont(Font.PLAIN, 11f)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        }

    fun refreshChipColors() {
        val selBg     = JBColor(Color(0x4B8BF5), Color(0x4B8BF5))
        val selFg     = JBColor.WHITE
        val unselBg   = JBColor(Color(0xE8E8E8), Color(0x3C3F41))
        val unselFg   = JBColor(Color(0x444444), Color(0xBBBBBB))
        val selBorder = JBColor(Color(0x3A7BD5), Color(0x3A7BD5))
        val unBorder  = JBColor.border()

        fun style(btn: JToggleButton) {
            if (btn.isSelected) {
                btn.background = selBg
                btn.foreground = selFg
                btn.border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selBorder, 1, true),
                    JBUI.Borders.empty(3, 10)
                )
            } else {
                btn.background = unselBg
                btn.foreground = unselFg
                btn.border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(unBorder, 1, true),
                    JBUI.Borders.empty(3, 10)
                )
            }
            btn.isOpaque = btn.isSelected
            btn.repaint()
        }

        style(allChip)
        chipButtons.values.forEach { style(it) }
    }

    fun resetToAll() {
        activeFilters = ImageExtension.entries.toHashSet()
        allChip.isSelected = true
        chipButtons.values.forEach { it.isSelected = true }
        refreshChipColors()
    }
}