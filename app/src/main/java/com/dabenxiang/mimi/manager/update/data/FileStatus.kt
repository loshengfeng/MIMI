package com.dabenxiang.mimi.manager.update.data

enum class FileStatus(val value: Int) {
    NOT_EXIST(0),
    NOT_LATEST_FILE(1),
    LATEST_FILE(2)
}