package com.yousseflabs.iamgeflowconverter.model.image

import java.io.File

enum class ImageExtension(val displayName: String) {
    PNG("PNG"),
    JPG("JPG / JPEG"),
    WEBP("WebP");

    companion object {
        fun from(file: File): ImageExtension? = when (file.extension.lowercase()) {
            "png"        -> PNG
            "jpg",
            "jpeg"       -> JPG
            "webp"       -> WEBP
            else         -> null
        }
    }
}