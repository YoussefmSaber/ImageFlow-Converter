package com.yousseflabs.iamgeflowconverter.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.yousseflabs.iamgeflowconverter.detection.DrawableDirectoryDetector
import com.yousseflabs.iamgeflowconverter.model.conversion.ConversionOptions
import com.yousseflabs.iamgeflowconverter.model.image.ImageFile
import com.yousseflabs.iamgeflowconverter.ui.components.OptionsFormPanel
import javax.swing.JComponent

class ConversionOptionsDialog(
    project: Project,
    images: List<ImageFile>
) : DialogWrapper(project) {

    private val drawableDirs = DrawableDirectoryDetector.detect(project)
    private val form         = OptionsFormPanel(drawableDirs)

    init {
        title = "ImageFlow Conversion Settings (${images.size} image(s))"
        init()
    }

    override fun createCenterPanel(): JComponent = form

    fun buildOptions(): ConversionOptions = form.buildOptions()
}
