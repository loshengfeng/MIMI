package com.dabenxiang.mimi.model.vo

data class CheckEmailResult(
    val isConfirmed: Boolean,
    val onConfirmed: () -> Unit,
    val onUnconfirmed: () -> Unit
)