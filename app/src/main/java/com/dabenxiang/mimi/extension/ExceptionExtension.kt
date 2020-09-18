package com.dabenxiang.mimi.extension

import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.ExceptionResult.*
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import retrofit2.HttpException

infix fun Throwable.handleException(processException: (ExceptionResult) -> Unit): ExceptionResult {
    val result = when (this) {
        is HttpException -> {
            val httpExceptionItem = GeneralUtils.getHttpExceptionData(this)
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