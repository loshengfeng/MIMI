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

        // todo: not confirm...
        const val TOKEN_NOT_FOUND = "401001"

        /**********************************************************
         *
         *                  Login
         *
         ***********************************************************/
        const val LOGIN_403001 = "403001" // Username or Password is incorrect.

        /**********************************************************
         *
         *                  Me
         *
         ***********************************************************/
        const val SIGN_UP  = "400000" // Username' is not a valid username., Email' is not a valid email.
    }
}