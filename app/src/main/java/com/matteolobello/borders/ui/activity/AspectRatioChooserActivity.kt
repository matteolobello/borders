package com.matteolobello.borders.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.matteolobello.borders.R
import com.matteolobello.borders.algorithm.AspectRatio
import com.matteolobello.borders.data.bundle.BundleKeys

class AspectRatioChooserActivity : AppCompatActivity() {

    private val aspectRatioCloseImageView by lazy {
        findViewById<ImageView>(R.id.aspectRatioCloseImageView)
    }

    private val oneToOneWrapper by lazy {
        findViewById<RelativeLayout>(R.id.oneToOneWrapper)
    }

    private val sixteenToNineWrapper by lazy {
        findViewById<RelativeLayout>(R.id.sixteenToNineWrapper)
    }

    private val nineToSixteenWrapper by lazy {
        findViewById<RelativeLayout>(R.id.nineToSixteenWrapper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aspect_ratio_chooser)

        val imageUri = intent.extras?.getParcelable<Uri>(BundleKeys.EXTRA_PATH_URI)
            ?: return

        aspectRatioCloseImageView.setOnClickListener {
            finish()
        }

        oneToOneWrapper.setOnClickListener {
            startActivity(
                Intent(this, EditImageActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_PATH_URI, imageUri)
                    .putExtra(
                        BundleKeys.EXTRA_ASPECT_RATIO_STRING,
                        AspectRatio.OneToOne.toString()
                    )
            )
        }

        sixteenToNineWrapper.setOnClickListener {
            startActivity(
                Intent(this, EditImageActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_PATH_URI, imageUri)
                    .putExtra(
                        BundleKeys.EXTRA_ASPECT_RATIO_STRING,
                        AspectRatio.SixteenToNine.toString()
                    )
            )
        }

        nineToSixteenWrapper.setOnClickListener {
            startActivity(
                Intent(this, EditImageActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_PATH_URI, imageUri)
                    .putExtra(
                        BundleKeys.EXTRA_ASPECT_RATIO_STRING,
                        AspectRatio.NineToSixteen.toString()
                    )
            )
        }
    }
}