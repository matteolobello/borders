package com.matteolobello.borders.algorithm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.widget.Toast
import com.matteolobello.borders.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PhotoFrameAlgorithm private constructor() {

    companion object {
        const val INITIAL_BORDER_PX = 180
        const val MAX_BORDER_PX = 300

        val instance: PhotoFrameAlgorithm by lazy { Holder.INSTANCE }
    }

    private object Holder {
        val INSTANCE = PhotoFrameAlgorithm()
    }

    var currentBitmap: Bitmap? = null

    fun generateAsync(foregroundBitmap: Bitmap, borderInPixels: Int, backgroundColor: Int,
                      aspectRatio: AspectRatio, callback: (generatedBitmap: Bitmap) -> Unit) {
        Thread {
            callback(generate(foregroundBitmap, borderInPixels, backgroundColor, aspectRatio))
        }.start()
    }

    fun generate(foregroundBitmap: Bitmap, borderInPixels: Int,
                 backgroundColor: Int, aspectRatio: AspectRatio): Bitmap {
        val widthHeight = aspectRatio.toWidthHeightPair()

        val backgroundBitmap = createMonoColourImageBitmap(
                width = widthHeight.first,
                height = widthHeight.second,
                color = backgroundColor)
        var bitmapToDrawInTheCenter = resizeKeepingAspectRatio(
                foregroundBitmap,
                maxWidth = widthHeight.first,
                maxHeight = widthHeight.second)

        bitmapToDrawInTheCenter = resizeKeepingAspectRatio(bitmapToDrawInTheCenter,
                bitmapToDrawInTheCenter.width - borderInPixels,
                bitmapToDrawInTheCenter.height - borderInPixels)

        currentBitmap = centerBitmapInBackground(backgroundBitmap, bitmapToDrawInTheCenter)
        return currentBitmap as Bitmap
    }

    fun createMonoColourImageBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    fun exportToFileAsync(context: Context, bitmap: Bitmap, fileName: String, callback: (path: String) -> Unit) {
        val rootPath = context.getExternalFilesDir(null)
        if (rootPath == null) {
            Toast.makeText(context, R.string.cannot_export_file, Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            with(File("${rootPath}/${context.getString(R.string.app_name)}")) {
                if (!exists()) {
                    mkdir()
                }
            }
            val outputFile = File("${rootPath}/${context.getString(R.string.app_name)}/$fileName")
            outputFile.createNewFile()

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream)
            val bitmapData = byteArrayOutputStream.toByteArray()

            val fileOutputStream = FileOutputStream(outputFile)
            fileOutputStream.write(bitmapData)
            fileOutputStream.flush()
            fileOutputStream.close()

            callback(outputFile.path)
        }.start()
    }

    fun rotateBitmapAsync(source: Bitmap, angle: Float, callback: (bitmap: Bitmap) -> Unit) {
        Thread {
            val matrix = Matrix()
            matrix.postRotate(angle)
            val bitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            callback(bitmap)
        }.start()
    }

    private fun resizeKeepingAspectRatio(inputBitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image = inputBitmap
        if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            return image
        } else {
            return image
        }
    }

    private fun centerBitmapInBackground(backgroundBitmap: Bitmap, bitmapToDrawInTheCenter: Bitmap): Bitmap {
        val bmOverlay = Bitmap.createBitmap(backgroundBitmap.width, backgroundBitmap.height, backgroundBitmap.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(backgroundBitmap, Matrix(), null)
        canvas.drawBitmap(bitmapToDrawInTheCenter,
                ((canvas.width - bitmapToDrawInTheCenter.width) / 2).toFloat(),
                ((canvas.height - bitmapToDrawInTheCenter.height) / 2).toFloat(), null)
        return bmOverlay
    }
}