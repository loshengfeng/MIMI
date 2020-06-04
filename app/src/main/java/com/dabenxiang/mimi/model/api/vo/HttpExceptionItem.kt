package com.dabenxiang.mimi.model.api.vo

import retrofit2.HttpException

data class HttpExceptionItem(
    var errorItem: ErrorItem,
    var httpExceptionClone: HttpException,
    var url: String = ""
)
