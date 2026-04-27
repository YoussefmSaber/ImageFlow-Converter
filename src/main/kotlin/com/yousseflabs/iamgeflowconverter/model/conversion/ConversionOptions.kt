package com.yousseflabs.iamgeflowconverter.model.conversion

data class ConversionOptions(
    val outputFormat: OutputFormat = OutputFormat.WEBP,
    val mode: ConversionMode = ConversionMode.LOSSY,
    val quality: Int = 80,

    // resize
    val resizeMode: ResizeMode = ResizeMode.NONE,
    val scalePercent: Int = 100,
    val targetWidth: Int? = null,
    val targetHeight: Int? = null,

    // output
    val deleteOriginals: Boolean = false,
    val generateDensityVariants: Boolean = false,
    val densityVariants: Set<DensityVariant> = setOf(DensityVariant.XHDPI),
    val outputDirectory: String? = null
)