package com.yousseflabs.iamgeflowconverter.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.yousseflabs.iamgeflowconverter.conversion.ConversionTask
import com.yousseflabs.iamgeflowconverter.model.image.ImageExtension
import com.yousseflabs.iamgeflowconverter.model.image.ImageFile
import com.yousseflabs.iamgeflowconverter.ui.ConversionOptionsDialog
import java.io.File

class ConvertContextMenuAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: emptyArray()
        e.presentation.isEnabledAndVisible = files.any { it.isImageOrFolder() }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val vFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        val images = vFiles.flatMap { it.collectImages() }
        if (images.isEmpty()) return

        val dialog = ConversionOptionsDialog(project, images)
        if (!dialog.showAndGet()) return

        val options = dialog.buildOptions()
        ConversionTask(project, images, options) {
            VirtualFileManager.getInstance().asyncRefresh(null)
        }.queue()
    }

    private fun VirtualFile.isImageOrFolder(): Boolean =
        isDirectory || ImageExtension.from(File(path)) != null

    private fun VirtualFile.collectImages(): List<ImageFile> {
        if (!isDirectory) {
            val ext = ImageExtension.from(File(path)) ?: return emptyList()
            return listOf(ImageFile(File(path), ext, name))
        }
        return children.flatMap { it.collectImages() }
    }
}