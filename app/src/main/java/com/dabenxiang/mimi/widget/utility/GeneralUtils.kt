package com.dabenxiang.mimi.widget.utility

import android.content.Context
import android.widget.Toast
import org.koin.core.KoinComponent

object GeneralUtils : KoinComponent {

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

//    @ExperimentalCoroutinesApi
//    fun sendApiLog(data: LogApiItem) {
//        MainScope().launch {
//            deviceManager.sendApiLog(data)
//        }
//    }

}