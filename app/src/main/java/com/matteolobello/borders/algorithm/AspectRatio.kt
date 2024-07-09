package com.matteolobello.borders.algorithm

sealed class AspectRatio {

    object OneToOne : AspectRatio()
    object SixteenToNine : AspectRatio()
    object NineToSixteen : AspectRatio()

    companion object {
        private const val ONE_TO_ONE_WIDTH = 1080
        private const val ONE_TO_ONE_HEIGHT = 1080

        private const val SIXTEEN_TO_NINE_WIDTH = 1920
        private const val SIXTEEN_TO_NINE_HEIGHT = 1080

        private const val NINE_TO_SIXTEEN_WIDTH = 1080
        private const val NINE_TO_SIXTEEN_HEIGHT = 1920

        fun fromString(string: String): AspectRatio {
            return when (string) {
                OneToOne::class.java.simpleName -> OneToOne
                SixteenToNine::class.java.simpleName -> SixteenToNine
                else -> NineToSixteen
            }
        }
    }

    fun toWidthHeightPair(): Pair<Int, Int> {
        return when (this) {
            is OneToOne -> Pair(ONE_TO_ONE_WIDTH, ONE_TO_ONE_HEIGHT)
            is SixteenToNine -> Pair(SIXTEEN_TO_NINE_WIDTH, SIXTEEN_TO_NINE_HEIGHT)
            else -> Pair(NINE_TO_SIXTEEN_WIDTH, NINE_TO_SIXTEEN_HEIGHT)
        }
    }

    override fun toString(): String {
        return this::class.java.simpleName
    }
}