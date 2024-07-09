package com.matteolobello.borders.ui.activity


import android.Manifest
import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.matteolobello.borders.R
import com.matteolobello.borders.data.bundle.BundleKeys
import com.matteolobello.borders.extensions.setSystemBarsColor
import com.matteolobello.borders.helper.ImageCaptureHelper
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Random


class SelectImageActivity : AppCompatActivity() {

    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 100
        private const val TAKE_PHOTO_REQUEST_CODE = 101

        private val BG_COLORS_RES = arrayOf(
            R.color.bg_1, R.color.bg_2, R.color.bg_3, R.color.bg_4, R.color.bg_5, R.color.bg_6
        )

        private const val BG_ANIM_DURATION = 3000L

        private const val PHOTO_KEY = "borders_tmp"
    }

    private var cameraPhotoFilePath: Uri? = null

    private var currentColorIndex = Random().nextInt(BG_COLORS_RES.size)

    private val rootLayout by lazy {
        findViewById<View>(R.id.rootLayout)
    }

    private val aboutTextView by lazy {
        findViewById<TextView>(R.id.aboutTextView)
    }

    private val takePhotoTextView by lazy {
        findViewById<TextView>(R.id.takePhotoTextView)
    }


    private val pickFromGalleryTextView by lazy {
        findViewById<TextView>(R.id.pickFromGalleryTextView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_image)

        startBackgroundAnimation()

        aboutTextView.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        takePhotoTextView.setOnClickListener {

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile: File? = try {
                createImageFileInAppDir()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }

            photoFile?.also { file ->
                val photoURI: Uri = FileProvider.getUriForFile(
                    this, "com.matteolobello.borders.provider", file
                )
                cameraPhotoFilePath = photoURI
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE)
        }

        pickFromGalleryTextView.setOnClickListener {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI
                ), PICK_IMAGE_REQUEST_CODE
            )
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(">>>>>", "$requestCode $resultCode ${data?.data?.toString()}")

        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (data != null) {
                startActivity(
                    Intent(
                        this, AspectRatioChooserActivity::class.java
                    ).putExtra(BundleKeys.EXTRA_PATH_URI, data.data)
                )
            }
        } else if (requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startActivity(
                Intent(
                    this, AspectRatioChooserActivity::class.java
                ).putExtra(BundleKeys.EXTRA_PATH_URI, cameraPhotoFilePath)
            )
        }
    }

    private fun requestPermissions(onAccepted: () -> Unit, onDenied: () -> Unit) {
        permissionsBuilder(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        ).build().send { result ->
            if (result.allGranted()) {
                onAccepted()
            } else {
                onDenied()
            }
        }
    }

    private fun startBackgroundAnimation() {
        val fromColorRes: Int
        val toColorRes: Int

        if (currentColorIndex == BG_COLORS_RES.size - 1) {
            fromColorRes = BG_COLORS_RES[currentColorIndex]
            toColorRes = BG_COLORS_RES[0]

            currentColorIndex = -1
        } else {
            fromColorRes = BG_COLORS_RES[currentColorIndex]
            toColorRes = BG_COLORS_RES[currentColorIndex + 1]
        }

        val fromColor = ContextCompat.getColor(this, fromColorRes)
        val toColor = ContextCompat.getColor(this, toColorRes)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
        colorAnimation.duration = BG_ANIM_DURATION
        colorAnimation.addUpdateListener { animator ->
            run {
                rootLayout.setBackgroundColor(animator.animatedValue as Int)
                setSystemBarsColor(animator.animatedValue as Int)
            }
        }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                startBackgroundAnimation()
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationStart(animator: Animator) {
            }
        })
        colorAnimation.start()

        currentColorIndex++
    }

    @Throws(IOException::class)
    private fun createImageFileInAppDir(): File {
        val timeStamp = Date().time.toString()
        val imagePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(imagePath, "borders_${timeStamp}" + ".jpg")
    }
}
