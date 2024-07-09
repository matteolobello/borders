package com.matteolobello.borders.ui.activity

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.matteolobello.borders.R
import com.matteolobello.borders.algorithm.PhotoFrameAlgorithm
import java.io.File

class SaveImageActivity : AppCompatActivity() {

    private var imagePath: String? = null

    private val outputImageView by lazy {
        findViewById<ImageView>(R.id.outputImageView)
    }

    private val saveCloseImageView by lazy {
        findViewById<ImageView>(R.id.saveCloseImageView)
    }

    private val downloadWrapper by lazy {
        findViewById<LinearLayout>(R.id.downloadWrapper)
    }

    // private val shareWrapper by lazy {
    //     findViewById<LinearLayout>(R.id.shareWrapper)
    // }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_image)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val bitmap = PhotoFrameAlgorithm.instance.currentBitmap
        if (bitmap == null) {
            startActivity(Intent(this, SelectImageActivity::class.java))
            return
        }

        outputImageView.setImageBitmap(bitmap)

        saveCloseImageView.setOnClickListener {
            supportFinishAfterTransition()
        }

        downloadWrapper.setOnClickListener {
            if (imagePath == null) {
                exportPhotoAsync {
                    runOnUiThread {
                        Toast.makeText(this, R.string.image_saved, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, R.string.image_saved, Toast.LENGTH_SHORT).show()
            }
        }

        // shareWrapper.setOnClickListener {
        //     if (imagePath == null) {
        //         exportPhotoAsync {
        //             shareImage()
        //         }
        //     } else {
        //        shareImage()
        //     }
        // }
    }

    private fun exportPhotoAsync(callback: (path: String) -> Unit) {
        if (PhotoFrameAlgorithm.instance.currentBitmap == null) {
            Toast.makeText(this, R.string.cannot_export_file, Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(applicationContext, R.string.saving_image, Toast.LENGTH_LONG).show()

        PhotoFrameAlgorithm.instance.exportToFileAsync(
            applicationContext,
            PhotoFrameAlgorithm.instance.currentBitmap!!, "${System.currentTimeMillis()}.png"
        ) { path ->
            run {
                imagePath = path

                val imageFile = File(path)
                MediaStore.Images.Media.insertImage(
                    contentResolver, path,
                    "Borders${System.currentTimeMillis()}",
                    "Image generated with Borders"
                )
                sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(imageFile)
                    )
                )
                MediaScannerConnection.scanFile(this, arrayOf(path), null) { _, _ -> }

                callback(path)
            }
        }
    }
}