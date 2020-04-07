package com.dabenxiang.mimi.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.HttpException

abstract class BaseFragment : Fragment() {

    open var mainViewModel: MainViewModel? = null
    var progressHUD: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressHUD = KProgressHUD.create(context)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)

        (activity as MainActivity?)?.also {
            it.bottom_navigation?.visibility = bottomNavigationVisibility
        }

        setupListeners()
        setupObservers()
    }

    fun showHttpErrorDialog(type: HttpErrorMsgType = HttpErrorMsgType.API_FAILED) {
        when (type) {
            HttpErrorMsgType.API_FAILED -> showErrorDialog(getString(R.string.api_failed_msg))
            HttpErrorMsgType.CHECK_NETWORK -> showErrorDialog(getString(R.string.server_error))
        }
    }

    fun showErrorDialog(message: String) {
        MaterialDialog(context!!).show {
            cancelable(false)
            message(text = message)
            positiveButton(R.string.btn_confirm) { dialog ->
                dialog.dismiss()
            }
            lifecycleOwner(this@BaseFragment)
        }
    }

    fun showHttpErrorToast(e: HttpException) {
        GeneralUtils.showToast(context!!, "$e")
    }

    abstract fun getLayoutId(): Int

    abstract fun setupObservers()

    abstract fun setupListeners()

    open val bottomNavigationVisibility: Int = View.VISIBLE
}
