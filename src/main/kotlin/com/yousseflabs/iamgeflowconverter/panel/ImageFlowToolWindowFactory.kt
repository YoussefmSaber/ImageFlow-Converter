package com.yousseflabs.iamgeflowconverter.panel

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.yousseflabs.iamgeflowconverter.ui.ImageFlowPanel

class ImageFlowToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun isApplicable(project: Project): Boolean = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel  = ImageFlowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}