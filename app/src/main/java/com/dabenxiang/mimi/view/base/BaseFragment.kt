package com.dabenxiang.mimi.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.HttpException

abstract class BaseFragment<out VM : BaseViewModel> : Fragment() {

    open var mainViewModel: MainViewModel? = null
    var progressHUD: KProgressHUD? = null

    abstract fun fetchViewModel(): VM?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
        }

        fetchViewModel()?.navigateDestination?.observe(this, Observer { item ->
            findNavController().also { navController ->
                when (item) {
                    NavigateItem.Up -> navController.navigateUp() //.popBackStack()
                    is NavigateItem.PopBackStack -> navController.popBackStack(item.fragmentId, item.inclusive)
                    is NavigateItem.Destination -> {
                        if (item.bundle == null) {
                            navController.navigate(item.action)
                        } else {
                            navController.navigate(item.action, item.bundle)
                        }
                    }
                }
            }
        })
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

        activity?.bottom_navigation?.visibility = bottomNavigationVisibility

        fetchViewModel()?.also { viewModel ->
            viewModel.showProgress.observe(viewLifecycleOwner, Observer {
                if (it) {
                    progressHUD?.show()
                } else {
                    progressHUD?.dismiss()
                }
            })

            viewModel.toastData.observe(viewLifecycleOwner, Observer {
                GeneralUtils.showToast(context!!, it)
            })
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
