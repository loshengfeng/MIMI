package com.dabenxiang.mimi.model.api.vo.error

/**********************************************************
 *
 *                  Common
 *
 ***********************************************************/
const val SUCCESS = "201000" // Success Created
const val REDIRECT = "302"    // Redirect
const val BAD_REQUEST = "400000" // Bad Request (400) - One of the request inputs is not valid.
const val NOT_FOUND = "404000" // The specified resource does not exist.
const val SERVER_ERROR = "500000" // Internal Server Error

// todo: not confirm...
const val TOKEN_NOT_FOUND = "401001"

/**********************************************************
 *
 *                  Login
 *
 ***********************************************************/
const val LOGIN_400000 = "400000" // Bad Request (400) - One of the request inputs is not valid.
const val LOGIN_403001 = "403001" // Username or Password is incorrect.
const val LOGIN_403002 = "403002" // The specified account is disabled.
const val LOGIN_403004 = "403004" // Two-factor authentication failed..
const val LOGIN_403006 = "403006"
const val LOGIN_406000 = "406000" // Invalid code
const val LOGIN_409000 = "409000" // Conflict (409) - The specified resource already exists..

/**********************************************************
 *
 *                  Me
 *
 ***********************************************************/
const val SIGN_UP = "400000" // Username' is not a valid username., Email' is not a valid email.