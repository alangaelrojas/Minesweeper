package com.alangaelrojas.buscaminas

data class Box(
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 0,
    var content: Int = 0,
    var isUncovered: Boolean = false
) {

    fun fixXAndY(x: Int, y: Int, width: Int) {
        this.x = x
        this.y = y
        this.width = width
    }

    fun isInside(xx: Int, yy: Int): Boolean {
        return xx >= x && xx <= x + width && yy >= y && yy <= y + width
    }

}