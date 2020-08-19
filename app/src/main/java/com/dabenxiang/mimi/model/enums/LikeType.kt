package com.dabenxiang.mimi.model.enums

enum class LikeType(val value: Int) {
    LIKE(0),
    DISLIKE(1);

    companion object {
        fun getByValue(target: Int): LikeType {
            return when(target){
                0->LIKE
                1->DISLIKE
                else->DISLIKE
            }
        }
    }
}