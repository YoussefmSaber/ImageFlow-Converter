package com.yousseflabs.iamgeflowconverter.model.conversion

enum class DensityVariant(val suffix: String, val scale: Double) {
    MDPI   ("mdpi",    1.0),
    HDPI   ("hdpi",    1.5),
    XHDPI  ("xhdpi",  2.0),
    XXHDPI ("xxhdpi", 3.0),
    XXXHDPI("xxxhdpi",4.0)
}