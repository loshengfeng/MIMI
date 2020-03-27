package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.HttpExceptionData

sealed class ExceptionResult {

    object RefreshTokenExpired : ExceptionResult()

    data class HttpError(val httpExceptionData: HttpExceptionData) : ExceptionResult()

    data class Crash(val throwable: Throwable) : ExceptionResult()
}
