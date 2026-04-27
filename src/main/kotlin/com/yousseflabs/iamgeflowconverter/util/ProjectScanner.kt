package com.yousseflabs.iamgeflowconverter.util

import com.intellij.openapi.project.Project
import com.yousseflabs.iamgeflowconverter.model.image.ImageExtension
import com.yousseflabs.iamgeflowconverter.model.image.ImageFile
import java.io.File

object ProjectScanner {

    private val IGNORED_DIRS = setOf(
        "build", ".gradle", ".idea", "node_modules", ".git",
        ".cxx", "intermediates", "generated", "tmp"
    )

    fun scan(project: Project): List<ImageFile> {
        val basePath = project.basePath ?: return emptyList()

        val root = File(basePath)
        if (!root.exists()) {
            return emptyList()
        }

        val results = root.walkTopDown()
            .onEnter { dir ->
                val ignored = dir.name in IGNORED_DIRS
                !ignored
            }
            .filter { it.isFile }
            .mapNotNull { file ->
                val ext = ImageExtension.from(file)
                ext?.let {
                    ImageFile(
                        file = file,
                        extension = it,
                        relativePath = file.relativeTo(root).path
                    )
                }
            }
            .sortedBy { it.relativePath }
            .toList()

        return results
    }
}