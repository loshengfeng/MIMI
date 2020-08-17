package com.dabenxiang.mimi.model.api.vo.error

import retrofit2.HttpException

data class HttpExceptionItem(
    var errorItem: ErrorItem,
    var httpExceptionClone: HttpException,
    var url: String = ""
)
