package com.matteolobello.borders.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.matteolobello.borders.R
import com.matteolobello.borders.algorithm.AspectRatio
import com.matteolobello.borders.algorithm.PhotoFrameAlgorithm
import com.matteolobello.borders.data.bundle.BundleKeys
import com.matteolobello.borders.ui.adapter.ColorsRecyclerViewAdapter

class EditImageActivity : AppCompatActivity(), ColorPickerDialogListener {

    private val photoFrameAlgorithm = PhotoFrameAlgorithm.instance

    private lateinit var colorsRecyclerViewAdapter: ColorsRecyclerViewAdapter
    private lateinit var foregroundImageBitmap: Bitmap
    private lateinit var aspectRatio: AspectRatio

    private var backgroundColor = Color.WHITE

    private val seekBar by lazy {
        findViewById<SeekBar>(R.id.seekBar)
    }

    private val colorsRecyclerView by lazy {
        findViewById<RecyclerView>(R.id.colorsRecyclerView)
    }

    private val editRotateImageView by lazy {
        findViewById<ImageView>(R.id.editRotateImageView)
    }

    private val editCloseImageView by lazy {
        findViewById<ImageView>(R.id.editCloseImageView)
    }

    private val generatedImageView by lazy {
        findViewById<ImageView>(R.id.generatedImageView)
    }

    private val editNextArrowImageView by lazy {
        findViewById<ImageView>(R.id.editNextArrowImageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)
        setupViews()
        firstSetupBitmap()
    }

    override fun onDialogDismissed(dialogId: Int) {
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        backgroundColor = color
        generateAndSetImage(false)
    }

    private fun setupViews() {
        seekBar.progress = PhotoFrameAlgorithm.INITIAL_BORDER_PX
        seekBar.max = PhotoFrameAlgorithm.MAX_BORDER_PX
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, madeByUser: Boolean) {
                generateAndSetImage(false)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        colorsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        colorsRecyclerView.setHasFixedSize(true)
        colorsRecyclerViewAdapter = ColorsRecyclerViewAdapter { color ->
            run {
                if (backgroundColor == color) {
                    return@run
                }

                if (color == ContextCompat.getColor(this, R.color.border_custom)) {
                    ColorPickerDialog.newBuilder().show(this)
                    return@run
                }

                backgroundColor = color
                generateAndSetImage(false)
            }
        }.also {
            colorsRecyclerView.adapter = it
        }

        editRotateImageView.setOnClickListener {
            photoFrameAlgorithm.rotateBitmapAsync(foregroundImageBitmap, 90f) {
                foregroundImageBitmap = it
                generateAndSetImage(false)
            }
        }

        editCloseImageView.setOnClickListener {
            finish()
        }

        editNextArrowImageView.setOnClickListener {
            val intent = Intent(this, SaveImageActivity::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                generatedImageView, getString(R.string.transition_image)
            )
            startActivity(intent, options.toBundle())
        }
    }

    private fun firstSetupBitmap() {
        Thread {
            val imageUri = intent.extras?.getParcelable<Uri>(BundleKeys.EXTRA_PATH_URI)
                ?: return@Thread
            val aspectRatioString = intent.extras?.getString(BundleKeys.EXTRA_ASPECT_RATIO_STRING)
                ?: return@Thread
            foregroundImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            aspectRatio = AspectRatio.fromString(aspectRatioString)

            val extractedColors = extractColors()
            backgroundColor =
                if (extractedColors.first == Color.TRANSPARENT) Color.BLACK else extractedColors.first

            generateAndSetImage(true)
        }.start()
    }

    private fun extractColors(): Triple<Int, Int, Int> {
        val palette = Palette.from(foregroundImageBitmap).generate()

        val vibrantColor = palette.getVibrantColor(Color.TRANSPARENT)
        val primaryColor = palette.getDominantColor(Color.TRANSPARENT)
        val primaryDarkColor = palette.getDarkMutedColor(Color.TRANSPARENT)

        return Triple(primaryColor, primaryDarkColor, vibrantColor)
    }

    private fun extractColorsAsync(callback: (extractedColors: Triple<Int, Int, Int>) -> Unit) {
        Thread {
            val extractedColors = extractColors()
            callback(extractedColors)
        }.start()
    }

    private fun generateAndSetImage(updateExtractedColors: Boolean) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            photoFrameAlgorithm.generateAsync(
                foregroundImageBitmap,
                seekBar.progress,
                backgroundColor,
                aspectRatio
            ) {
                if (updateExtractedColors) {
                    extractColorsAsync {
                        runOnUiThread {
                            colorsRecyclerViewAdapter.setColorsOfBitmap(
                                primaryColor = it.first,
                                primaryDarkColor = it.second,
                                vibrantColor = it.third
                            )
                        }
                    }
                }

                generatedImageView.post {
                    generatedImageView.setImageBitmap(it)
                }
            }
        } else {
            val bitmap = photoFrameAlgorithm.generate(
                foregroundImageBitmap,
                seekBar.progress,
                backgroundColor,
                aspectRatio
            )
            if (updateExtractedColors) {
                extractColorsAsync {
                    runOnUiThread {
                        colorsRecyclerViewAdapter.setColorsOfBitmap(
                            primaryColor = it.first,
                            primaryDarkColor = it.second,
                            vibrantColor = it.third
                        )
                    }
                }
            }

            generatedImageView.post {
                generatedImageView.setImageBitmap(bitmap)
            }
        }
    }
}
