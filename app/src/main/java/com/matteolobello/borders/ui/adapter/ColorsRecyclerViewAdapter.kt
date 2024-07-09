package com.matteolobello.borders.ui.adapter

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.matteolobello.borders.R
import com.matteolobello.borders.algorithm.PhotoFrameAlgorithm
import com.matteolobello.borders.extensions.createNewMergedArray
import com.matteolobello.borders.extensions.isAlmostBlack
import com.mikhaellopez.circularimageview.CircularImageView

class ColorsRecyclerViewAdapter(private val onColorClick: (color: Int) -> Unit)
    : RecyclerView.Adapter<ColorsRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private val COLORS_RES = arrayListOf(
                R.color.border_1,
                R.color.border_2,
                R.color.border_3,
                R.color.border_4,
                R.color.border_5,
                R.color.border_6,
                R.color.border_7,
                R.color.border_8,
                R.color.border_9,
                R.color.border_10,
                R.color.border_11,
                R.color.border_12,
                R.color.border_13,
                R.color.border_14,
                R.color.border_15,
                R.color.border_custom
        )
    }

    private val extractedColorsList = arrayListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false))

    override fun getItemCount(): Int = COLORS_RES.size + extractedColorsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)

        val mergedColors = extractedColorsList.createNewMergedArray(COLORS_RES)

        val color = mergedColors[position]

        val isColorRes = position >= extractedColorsList.size

        val imageView = holder.itemView as CircularImageView
        imageView.post {
            imageView.setImageBitmap(PhotoFrameAlgorithm.instance
                    .createMonoColourImageBitmap(imageView.width, imageView.height,
                            if (isColorRes)
                                ContextCompat.getColor(imageView.context, color)
                            else
                                color))

            if (color == R.color.border_2 || (!isColorRes && color.isAlmostBlack())) {
                // Black color needs a border
                imageView.setBorderColor(Color.WHITE)
                imageView.setBorderWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f,
                        imageView.context.resources.displayMetrics))
            } else if (color == R.color.border_custom) {
                imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, R.drawable.color_wheel))
                imageView.setBorderColor(Color.TRANSPARENT)
                imageView.setBorderWidth(0f)
            } else {
                imageView.setBorderWidth(0f)
            }

            imageView.setOnClickListener {
                onColorClick(if (isColorRes) ContextCompat.getColor(it.context, color) else color)
            }
        }
    }

    fun setColorsOfBitmap(primaryColor: Int, primaryDarkColor: Int, vibrantColor: Int) {
        extractedColorsList.clear()

        if (vibrantColor != Color.TRANSPARENT) {
            extractedColorsList.add(vibrantColor)
        }

        if (primaryColor != Color.TRANSPARENT) {
            extractedColorsList.add(primaryColor)
        }

        if (primaryDarkColor != Color.TRANSPARENT) {
            extractedColorsList.add(primaryDarkColor)
        }

        notifyDataSetChanged()
    }

    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)
}