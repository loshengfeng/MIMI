package com.dabenxiang.mimi.model.api.vo

data class CategoriesItem (
    val content: Content,
    val code: Long,
    val message: String
)

data class Content (
    val categories: List<ContentCategory>
)

data class ContentCategory (
    val id: String,
    val name: String,
    val categories: List<PurpleCategory>
)

data class PurpleCategory (
    val id: String,
    val name: String,
    val categories: List<FluffyCategory>
)

data class FluffyCategory (
    val id: String,
    val name: String,
    val categories: List<TentacledCategory>? = null
)

data class TentacledCategory (
    val name: String,
    val categories: List<StickyCategory>
)

data class StickyCategory (
    val name: String
)
