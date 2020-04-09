package com.dabenxiang.mimi.model.api

class ErrorCode {
    companion object {

        /**********************************************************
         *
         *                  Common
         *
         ***********************************************************/
        const val SUCCESS      = "201000" // Success Created
        const val REDIRECT     = "302"    // Redirect
        const val BAD_REQUEST  = "400000" // Bad Request (400) - One of the request inputs is not valid.
        const val NOT_FOUND    = "404000" // The specified resource does not exist.
        const val SERVER_ERROR = "500000" // Internal Server Error
    }
}