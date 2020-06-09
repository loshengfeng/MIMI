package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.ExceptionResult.*
import com.dabenxiang.mimi.widget.utility.AppUtils
import retrofit2.HttpException

fun Throwable.handleException(processException: (ExceptionResult) -> Unit): ExceptionResult {
    val result = when (this) {
        is HttpException -> {
            val httpExceptionItem = AppUtils.getHttpExceptionData(this)
            val result = ApiRepository.isRefreshTokenFailed(httpExceptionItem.errorItem.code)
            if (result) {
                RefreshTokenExpired
            } else {
                HttpError(httpExceptionItem)
            }
        }
        else -> {
            Crash(this)
        }
    }

    processException(result)

    return result
}