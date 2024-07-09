package com.matteolobello.borders.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.matteolobello.borders.R

class AboutActivity : AppCompatActivity() {

    companion object {
        private const val MATTEO_URL = "https://matteolobello.com"
        private const val GIANLUCA_URL = "https://instagram.com/gianluca_astorino"
    }

    private val aboutCloseImageView by lazy {
        findViewById<ImageView>(R.id.aboutCloseImageView)
    }

    private val matteoWrapper by lazy {
        findViewById<LinearLayout>(R.id.matteoWrapper)
    }

    private val gianlucaWrapper by lazy {
        findViewById<RelativeLayout>(R.id.gianlucaWrapper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        aboutCloseImageView.setOnClickListener {
            finish()
        }

        matteoWrapper.setOnClickListener {
            launchUrl(MATTEO_URL)
        }

        gianlucaWrapper.setOnClickListener {
            launchUrl(GIANLUCA_URL)
        }
    }

    private fun launchUrl(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}