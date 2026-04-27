package com.yousseflabs.iamgeflowconverter.conversion

import com.intellij.conversion.ConversionResult
import com.intellij.util.ui.UIUtil
import com.yousseflabs.iamgeflowconverter.model.ConversionMode
import com.yousseflabs.iamgeflowconverter.model.ConversionOptions
import com.yousseflabs.iamgeflowconverter.model.DensityVariant
import com.yousseflabs.iamgeflowconverter.model.OutputFormat
import com.yousseflabs.iamgeflowconverter.model.ResizeMode
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

object ImageConverter {

    fun convert(source: File, options: ConversionOptions): ConversionResult {
        return try {
            val outputs = if (options.generateDensityVariants && options.densityVariants.isNotEmpty()) {
                options.densityVariants.map { variant ->
                    convertSingle(source, options, variant)
                }
            } else {
                listOf(convertSingle(source, options, null))
            }

            if (options.deleteOriginals) source.delete()

            ConversionResult(source, outputs, success = true)
        } catch (e: Exception) {
            ConversionResult(
                sourceFile   = source,
                outputFiles  = emptyList(),
                success      = false,
                errorMessage = e.message
            )
        }
    }

    private fun convertSingle(
        source:  File,
        options: ConversionOptions,
        variant: DensityVariant?
    ): File {
        val sourceImage: BufferedImage = ImageIO.read(source)
            ?: error("Cannot read image: ${source.path}")

        val resized = applyResize(sourceImage, options, variant)

        val prepared = prepareForFormat(resized, options.outputFormat)
        val outputDir  = resolveOutputDir(source, options, variant)
        outputDir.mkdirs()
        val outputFile = File(outputDir, "${source.nameWithoutExtension}.${options.outputFormat.extension}")

        writeImage(prepared, outputFile, options)

        return outputFile
    }

    private fun applyResize(
        image:   BufferedImage,
        options: ConversionOptions,
        variant: DensityVariant?
    ): BufferedImage {

        val (baseW, baseH) = when (options.resizeMode) {
            ResizeMode.NONE -> image.width to image.height

            ResizeMode.SCALE_PERCENT -> {
                val factor = options.scalePercent / 100.0
                (image.width * factor).toInt() to (image.height * factor).toInt()
            }

            ResizeMode.EXACT_DIMENSIONS -> {
                val tw = options.targetWidth
                val th = options.targetHeight
                when {
                    tw != null && th != null -> tw to th
                    tw != null -> {
                        val ratio = tw.toDouble() / image.width
                        tw to (image.height * ratio).toInt()
                    }
                    th != null -> {
                        val ratio = th.toDouble() / image.height
                        (image.width * ratio).toInt() to th
                    }
                    else -> image.width to image.height
                }
            }
        }

        val finalW: Int
        val finalH: Int
        if (variant != null && variant.scale != 1.0) {
            finalW = (baseW * variant.scale).toInt()
            finalH = (baseH * variant.scale).toInt()
        } else {
            finalW = baseW
            finalH = baseH
        }

        if (finalW == image.width && finalH == image.height) return image

        val w = maxOf(1, finalW)
        val h = maxOf(1, finalH)

        val result = UIUtil.createImage(w, h, image.type.takeIf { it != 0 } ?: BufferedImage.TYPE_INT_ARGB)
        val g = result.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,  RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON)
        g.drawImage(image, 0, 0, w, h, null)
        g.dispose()
        return result
    }

    private fun prepareForFormat(image: BufferedImage, format: OutputFormat): BufferedImage {
        if (format != OutputFormat.JPEG) return image
        if (image.colorModel.hasAlpha()) {
            val rgb = UIUtil.createImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
            val g = rgb.createGraphics()
            g.drawImage(image, 0, 0, null)
            g.dispose()
            return rgb
        }
        return image
    }

    private fun writeImage(image: BufferedImage, output: File, options: ConversionOptions) {
        val formatName = options.outputFormat.mimeType

        val writers = ImageIO.getImageWritersByFormatName(formatName)
        check(writers.hasNext()) {
            "No ImageIO writer found for format '$formatName'. " +
                    "Make sure webp-imageio is on the classpath for WebP output."
        }
        val writer = writers.next()

        val writeParam: ImageWriteParam = writer.defaultWriteParam

        if (writeParam.canWriteCompressed()) {
            writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
            when (options.outputFormat) {
                OutputFormat.WEBP -> {
                    if (options.mode == ConversionMode.LOSSLESS) {
                        runCatching {
                            writeParam.compressionType = "Lossless"
                        }
                    } else {
                        runCatching {
                            writeParam.compressionType  = "Lossy"
                            writeParam.compressionQuality = options.quality / 100f
                        }
                    }
                }
                OutputFormat.JPEG -> {
                    writeParam.compressionQuality = options.quality / 100f
                }
                OutputFormat.PNG -> {}
            }
        }

        val imageOutputStream = ImageIO.createImageOutputStream(output)
            ?: error("Cannot open output stream for: ${output.path}")

        try {
            writer.output = imageOutputStream
            writer.write(null, IIOImage(image, null, null), writeParam)
        } finally {
            imageOutputStream.close()
            writer.dispose()
        }
    }

    private fun resolveOutputDir(
        source:  File,
        options: ConversionOptions,
        variant: DensityVariant?
    ): File {
        val base = if (options.outputDirectory != null)
            File(options.outputDirectory)
        else
            source.parentFile

        return if (variant != null)
            File(base, "drawable-${variant.suffix}")
        else
            base
    }
}