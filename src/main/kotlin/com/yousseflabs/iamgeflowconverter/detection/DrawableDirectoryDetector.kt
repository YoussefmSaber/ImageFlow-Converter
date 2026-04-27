package com.yousseflabs.iamgeflowconverter.detection

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import java.io.File

object DrawableDirectoryDetector {

    fun detect(project: Project): List<DrawableDirectory> {
        val results = mutableListOf<DrawableDirectory>()

        ModuleManager.getInstance(project).modules.forEach { module ->
            val moduleRoot = module.moduleFilePath
                .let { File(it).parentFile }
                ?: return@forEach

            // ── Android res/drawable[-qualifier] dirs ─────────────────────────
            listOf(
                File(moduleRoot, "src/main/res"),
                File(moduleRoot, "res")
            ).filter { it.exists() && it.isDirectory }
                .forEach { resDir ->
                    resDir.listFiles()
                        ?.filter { it.isDirectory && it.name.startsWith("drawable") }
                        ?.forEach { drawableDir ->
                            val qualifier = drawableDir.name
                                .removePrefix("drawable")
                                .removePrefix("-")
                                .takeIf { it.isNotEmpty() }
                            results += DrawableDirectory(
                                dir        = drawableDir,
                                type       = DrawableDirectoryType.ANDROID_RES,
                                moduleName = module.name,
                                qualifier  = qualifier
                            )
                        }
                }

            // ── KMP composeResources/drawable dirs ────────────────────────────
            moduleRoot.walkTopDown()
                .maxDepth(6)
                .filter { it.isDirectory && it.name == "composeResources" }
                .forEach { composeRes ->
                    val drawableDir = File(composeRes, "drawable")
                    if (drawableDir.exists() && drawableDir.isDirectory) {
                        results += DrawableDirectory(
                            dir        = drawableDir,
                            type       = DrawableDirectoryType.KMP_COMPOSE_RESOURCES,
                            moduleName = module.name
                        )
                    }
                }
        }

        return results.distinctBy { it.dir.absolutePath }
    }
}
