package com.matteolobello.borders.extensions

import android.app.Activity

fun Activity.setSystemBarsColor(color: Int) {
    window.navigationBarColor = color
    window.statusBarColor = color
}