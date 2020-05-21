package com.dabenxiang.mimi.view.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import com.dabenxiang.mimi.R
import com.kaopiz.kprogresshud.KProgressHUD

abstract class BaseDialogFragment : DialogFragment() {

    var progressHUD: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isFullLayout()) {
            val dialog = super.onCreateDialog(savedInstanceState)
            val window = dialog.window
            if (window != null) {
                //window.requestFeature(Window.FEATURE_NO_TITLE)
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                setStyle(STYLE_NORMAL, R.style.AppTheme)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressHUD = KProgressHUD.create(context)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)

        setupListeners()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()

        if (isFullLayout()) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } else {
            val window = dialog?.window
            if (window != null) {
                val widthPixels = (resources.displayMetrics.widthPixels * 0.8).toInt()
                window.setLayout(widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    abstract fun isFullLayout(): Boolean

    @LayoutRes
    abstract fun getLayoutId(): Int

    open fun setupObservers() {

    }

    open fun setupListeners() {

    }

}
