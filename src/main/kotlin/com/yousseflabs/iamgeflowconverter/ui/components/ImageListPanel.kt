package com.yousseflabs.iamgeflowconverter.ui.components

import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.yousseflabs.iamgeflowconverter.model.image.ImageFile
import com.yousseflabs.iamgeflowconverter.ui.ImageCheckboxRenderer
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.UIManager

class ImageListPanel(
    private val onSelectionChanged: (Set<ImageFile>) -> Unit
) : JPanel(BorderLayout()) {

    val checkedItems: MutableSet<ImageFile> = mutableSetOf()

    private val listModel = DefaultListModel<ImageFile>()
    private val imageList = JBList(listModel).apply {
        cellRenderer = ImageCheckboxRenderer(checkedItems)
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        fixedCellHeight = 28
        background = UIManager.getColor("List.background") ?: JBColor.WHITE

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val index = locationToIndex(e.point)
                if (index < 0) return
                val item = listModel.getElementAt(index)
                if (checkedItems.contains(item)) checkedItems.remove(item) else checkedItems.add(
                    item
                )
                repaint()
                onSelectionChanged(checkedItems)
            }
        })
    }

    private val statusLabel = JLabel("", SwingConstants.CENTER).apply {
        foreground = JBColor.GRAY
        font = font.deriveFont(Font.ITALIC, 12f)
        border = JBUI.Borders.empty(16)
    }

    private val cardPanel = JPanel(CardLayout())
    private val cardLayout = cardPanel.layout as CardLayout

    private var currentImages: List<ImageFile> = emptyList()

    init {
        cardPanel.add(statusLabel, "status")
        cardPanel.add(ScrollPaneFactory.createScrollPane(imageList), "list")

        val selectAllBtn = JButton("Select All").apply {
            addActionListener {
                checkedItems.clear()
                checkedItems.addAll(currentImages)
                imageList.repaint()
                onSelectionChanged(checkedItems)
            }
        }
        val deselectBtn = JButton("Deselect All").apply {
            addActionListener {
                checkedItems.clear()
                imageList.repaint()
                onSelectionChanged(checkedItems)
            }
        }

        val btnRow = JPanel(FlowLayout(FlowLayout.LEFT, 4, 4)).apply {
            add(selectAllBtn)
            add(deselectBtn)
        }

        add(btnRow, BorderLayout.NORTH)
        add(cardPanel, BorderLayout.CENTER)
    }

    fun populate(images: List<ImageFile>, allEmpty: Boolean = false) {
        currentImages = images
        checkedItems.retainAll(images.toSet())

        listModel.clear()
        images.forEach { listModel.addElement(it) }

        if (images.isEmpty()) {
            showStatus(
                if (allEmpty) "No images found in project"
                else "No images match the selected filters"
            )
        } else {
            cardLayout.show(cardPanel, "list")
        }

        onSelectionChanged(checkedItems)
    }

    fun showStatus(message: String) {
        statusLabel.text = message
        cardLayout.show(cardPanel, "status")
    }

    fun clearChecked() {
        checkedItems.clear()
        imageList.repaint()
        onSelectionChanged(checkedItems)
    }
}
