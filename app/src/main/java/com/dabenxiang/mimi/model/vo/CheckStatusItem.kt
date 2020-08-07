package com.dabenxiang.mimi.model.vo

data class CheckStatusItem(
    val status: StatusItem,
    val onLoginAndEmailConfirmed: () -> Unit
)

enum class StatusItem {
    NOT_LOGIN,
    LOGIN_BUT_EMAIL_NOT_CONFIRMED,
    LOGIN_AND_EMAIL_CONFIRMED
}