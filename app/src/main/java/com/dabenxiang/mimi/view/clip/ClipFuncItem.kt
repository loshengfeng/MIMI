package com.dabenxiang.mimi.view.clip

data class ClipFuncItem(
    val getClip: ((String, Int) -> Unit) = { _, _ -> },
    val getCover: ((String, Int) -> Unit) = { _, _ -> },
    val onBackClick: (() -> Unit) = {}
)