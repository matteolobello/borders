package com.matteolobello.borders.extensions

fun <T> ArrayList<T>.createNewMergedArray(arrayTwo: ArrayList<T>): ArrayList<T> {
    val returnValue = arrayListOf<T>()
    returnValue.addAll(this)
    returnValue.addAll(arrayTwo)
    return returnValue
}