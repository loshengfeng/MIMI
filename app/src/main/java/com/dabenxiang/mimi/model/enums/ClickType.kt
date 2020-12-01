package com.dabenxiang.mimi.model.enums

enum class ClickType(val value: Int) {
    TYPE_ITEM(-1),
    TYPE_AUTHOR(1),
    TYPE_CLUB(2),
    TYPE_LIKE(3),
    TYPE_FOLLOW(4),
    TYPE_FAVORITES(5),
    TYPE_LABEL(6),
    TYPE_COMMENT(7),
    TYPE_FILM(8);

    companion object {
        fun getByValue(target: Int): ClickType {
            return when (target) {
                1 -> TYPE_AUTHOR
                2 -> TYPE_CLUB
                3 -> TYPE_LIKE
                4 -> TYPE_FOLLOW
                5 -> TYPE_FAVORITES
                6 -> TYPE_LABEL
                7 -> TYPE_COMMENT
                8 -> TYPE_FILM
                else -> TYPE_ITEM
            }
        }
    }
}