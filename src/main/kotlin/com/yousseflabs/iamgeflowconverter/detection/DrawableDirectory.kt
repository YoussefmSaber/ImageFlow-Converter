package com.yousseflabs.iamgeflowconverter.detection

import java.io.File


data class DrawableDirectory(
    val dir: File,
    val type: DrawableDirectoryType,
    val moduleName: String,
    val qualifier: String? = null
)