package com.matteolobello.borders.extensions

import android.animation.Animator
import android.graphics.Rect
import android.view.View
import android.view.ViewAnimationUtils


fun View.circularReveal(onViewRevealed: () -> Unit) {
    alpha = 1f
    visibility = View.VISIBLE

    val bounds = Rect()
    getDrawingRect(bounds)
    val centerX = bounds.centerX()
    val centerY = bounds.centerY()
    val finalRadius = Math.max(bounds.width(), bounds.height())
    val animator = ViewAnimationUtils.createCircularReveal(this, centerX, centerY, 0f, finalRadius.toFloat())
    animator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationEnd(animator: Animator) {
            onViewRevealed()
        }

        override fun onAnimationCancel(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    })
    animator.duration = 350
    animator.start()
}

