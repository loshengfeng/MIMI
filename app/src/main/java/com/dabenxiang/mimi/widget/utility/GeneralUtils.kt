package com.dabenxiang.mimi.widget.utility

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.model.pref.Pref
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

object GeneralUtils : KoinComponent {

    private val pref: Pref by inject()

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun hideKeyboard(activity: FragmentActivity) {
        val view = activity.currentFocus
        if (view != null) {
            val inputManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showKeyboard(context: Context, editText: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
    }

    fun getEllipsizeString(text: String, startIndex: Int = 2, endIndex: Int = 2): String {
        Timber.i("Disable ellipsize :${pref.disableEllipsize}")
        val disableEllipsize = pref.disableEllipsize
        if (disableEllipsize) return text

        var ellipsize = ""

        if (text.length == 2) {
            ellipsize = "${text[0]}*"
        } else {
            text.forEachIndexed { index, char ->
                ellipsize += if (index < startIndex || index >= text.length - endIndex) {
                    char.toString()
                } else {
                    "*"
                }
            }
        }
        return ellipsize
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val bytes = ImageUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.PNG)
        return EncodeUtils.base64Encode2String(bytes)
    }

    fun Int.toDp(resources: Resources): Int {
        return (resources.displayMetrics.density * this).toInt()
    }

}