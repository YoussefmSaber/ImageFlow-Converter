package com.yousseflabs.iamgeflowconverter.ui

import com.intellij.util.ui.JBUI
import com.yousseflabs.iamgeflowconverter.model.image.ImageFile
import java.awt.Component
import javax.swing.JCheckBox
import javax.swing.JList
import javax.swing.ListCellRenderer

class ImageCheckboxRenderer(
    private val checkedItems: Set<ImageFile>
) : ListCellRenderer<ImageFile> {

    private val checkbox = JCheckBox().apply {
        isOpaque = true
        border   = JBUI.Borders.empty(2, 6)
    }

    override fun getListCellRendererComponent(
        list:        JList<out ImageFile>,
        value:       ImageFile,
        index:       Int,
        isSelected:  Boolean,
        cellHasFocus: Boolean
    ): Component {
        checkbox.isSelected  = checkedItems.contains(value)
        checkbox.background  = if (isSelected) list.selectionBackground else list.background
        checkbox.foreground  = if (isSelected) list.selectionForeground else list.foreground

        checkbox.text = "<html><b>${value.file.name}</b>" +
                "  <font color='gray'>${value.relativePath}</font></html>"
        return checkbox
    }
}
