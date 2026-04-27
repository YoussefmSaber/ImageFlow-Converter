package com.yousseflabs.iamgeflowconverter.model.conversion

enum class OutputFormat(
    val displayName: String,
    val extension: String,
    val mimeType: String
) {
    WEBP("WebP",  "webp", "webp"),
    PNG ("PNG",   "png",  "png"),
    JPEG("JPEG",  "jpg",  "jpeg")
}