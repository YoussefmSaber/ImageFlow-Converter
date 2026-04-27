package com.yousseflabs.iamgeflowconverter.model

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

enum class OutputFormat(
    val displayName: String,
    val extension: String,
    val mimeType: String
) {
    WEBP("WebP",  "webp", "webp"),
    PNG ("PNG",   "png",  "png"),
    JPEG("JPEG",  "jpg",  "jpeg")
}

enum class ConversionMode { LOSSY, LOSSLESS }

enum class DensityVariant(val suffix: String, val scale: Double) {
    MDPI   ("mdpi",    1.0),
    HDPI   ("hdpi",    1.5),
    XHDPI  ("xhdpi",  2.0),
    XXHDPI ("xxhdpi", 3.0),
    XXXHDPI("xxxhdpi",4.0)
}

enum class ResizeMode {
    NONE,
    SCALE_PERCENT,
    EXACT_DIMENSIONS
}