package com.dabenxiang.mimi.model.api

class ErrorCode {
    companion object {

        /**********************************************************
         *
         *                  Common
         *
         ***********************************************************/
        const val TOKEN_NOT_FOUND = "401001"
        const val WRONG_NAME = "404002"
        const val WRONG_PW = "401002"
        const val DUPLICATE_ACCOUNT = "409001"
        const val DUPLICATE_QR_CODE = "409005"
        const val INSUFFICIENT_BALANCE = "403011"
    }
}