package com.yousseflabs.iamgeflowconverter.model.image

import java.io.File

data class ImageFile(
    val file: File,
    val extension: ImageExtension,
    val relativePath: String   // relative to project root, for display
)