package com.yousseflabs.iamgeflowconverter.conversion

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.yousseflabs.iamgeflowconverter.model.conversion.ConversionOptions
import com.yousseflabs.iamgeflowconverter.model.conversion.ConversionResult
import com.yousseflabs.iamgeflowconverter.model.image.ImageFile

class ConversionTask(
    project: Project,
    private val images: List<ImageFile>,
    private val options: ConversionOptions,
    private val onFinished: (List<ConversionResult>) -> Unit
) : Task.Backgroundable(project, "ImageFlow: Converting images…", true) {

    override fun run(indicator: ProgressIndicator) {
        val results = mutableListOf<ConversionResult>()

        images.forEachIndexed { index, imageFile ->
            if (indicator.isCanceled) return

            indicator.text    = "Converting ${imageFile.file.name} " +
                    "→ ${options.outputFormat.displayName}…"
            indicator.fraction = index.toDouble() / images.size

            results += ImageConverter.convert(imageFile.file, options)
        }

        VirtualFileManager.getInstance().asyncRefresh(null)

        onFinished(results)
    }
}