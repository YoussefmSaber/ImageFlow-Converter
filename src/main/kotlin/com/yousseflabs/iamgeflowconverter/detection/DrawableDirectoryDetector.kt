package com.yousseflabs.iamgeflowconverter.detection

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import java.io.File
import com.intellij.openapi.roots.ModuleRootManager

object DrawableDirectoryDetector {

    fun detect(project: Project): List<DrawableDirectory> {
        val results = mutableListOf<DrawableDirectory>()

        ModuleManager.getInstance(project).modules.forEach { module ->
            val moduleRoots = ModuleRootManager.getInstance(module).contentRoots
                .mapNotNull { root -> root.path.let(::File) }

            moduleRoots.forEach { moduleRoot ->
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
                                    dir = drawableDir,
                                    type = DrawableDirectoryType.ANDROID_RES,
                                    moduleName = module.name,
                                    qualifier = qualifier
                                )
                            }
                    }

                moduleRoot.walkTopDown()
                    .maxDepth(6)
                    .filter { it.isDirectory && it.name == "composeResources" }
                    .forEach { composeRes ->
                        val drawableDir = File(composeRes, "drawable")
                        if (drawableDir.exists() && drawableDir.isDirectory) {
                            results += DrawableDirectory(
                                dir = drawableDir,
                                type = DrawableDirectoryType.KMP_COMPOSE_RESOURCES,
                                moduleName = module.name
                            )
                        }
                    }
            }
        }

        return results.distinctBy { it.dir.absolutePath }
    }
}