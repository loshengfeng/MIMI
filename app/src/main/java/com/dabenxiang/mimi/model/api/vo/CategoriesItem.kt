package com.dabenxiang.mimi.model.api.vo

data class CategoriesItem (
    val content: Content?,
    val code: Long,
    val message: String
) {
    data class Content (
        val id: String?,
        val name: String?,
        val categories: List<Content>?
    )
}


