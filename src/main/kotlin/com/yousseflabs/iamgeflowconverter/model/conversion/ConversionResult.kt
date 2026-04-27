package com.yousseflabs.iamgeflowconverter.model.conversion

import java.io.File

data class ConversionResult(
    val sourceFile: File,
    val outputFiles: List<File>,
    val success: Boolean,
    val errorMessage: String? = null
)