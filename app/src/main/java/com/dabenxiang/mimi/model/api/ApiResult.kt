package com.dabenxiang.mimi.model.api

sealed class ApiResult<T> {
    companion object {
        fun <T> loading(arg: T? = null): ApiResult<T> {
            return Loading(arg)
        }

        fun <T> loaded(): ApiResult<T> {
            return Loaded()
        }

        fun <T> error(throwable: Throwable): ApiResult<T> {
            return Error(throwable)
        }

        fun <T> success(result: T?): ApiResult<T> {
            return when (result) {
                null -> Empty()
                else -> Success(result)
            }
        }
    }

    data class Success<T>(val result: T) : ApiResult<T>()

    data class Error<T>(val throwable: Throwable) : ApiResult<T>()

    class Empty<T> : ApiResult<T>()

    class Loading<T>(val arg: T?) : ApiResult<T>()

    class Loaded<T> : ApiResult<T>()
}
