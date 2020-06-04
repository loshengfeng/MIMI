package com.dabenxiang.mimi.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.handleException
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.view.dialog.message.MessageDialogFragment
import com.dabenxiang.mimi.view.dialog.message.OnMessageDialogListener
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.net.UnknownHostException

abstract class BaseFragment<out VM : BaseViewModel> : Fragment() {

    open var mainViewModel: MainViewModel? = null
    var progressHUD: KProgressHUD? = null

    abstract fun fetchViewModel(): VM?

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

    private fun showErrorDialog(message: String) {
        MaterialDialog(context!!).show {
            cancelable(false)
            message(text = message)
            positiveButton(R.string.btn_confirm) { dialog ->
                dialog.dismiss()
            }
            lifecycleOwner(this@BaseFragment)
        }
    }

    fun showErrorMessageDialog(message: String, listener: OnMessageDialogListener? = null) {
        val content = MessageDialogFragment.Content(
            title = getString(R.string.error_device_binding_title),
            message = message,
            positiveBtnText = getString(R.string.error_device_binding_positive),
            listener = listener)
        MessageDialogFragment.newInstance(content).show(requireActivity().supportFragmentManager, MessageDialogFragment::class.java.simpleName)
    }

    fun showHttpErrorToast(e: HttpException) {
        GeneralUtils.showToast(context!!, "$e")
    }

    abstract fun getLayoutId(): Int

    abstract fun setupObservers()

    abstract fun setupListeners()

    open fun initSettings() {}

    open val bottomNavigationVisibility: Int = View.VISIBLE

    open fun navigateTo(item: NavigateItem) {
        lifecycleScope.launch {
            navigationTaskJoinOrRun {
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
                delay(1000L)
            }
        }
    }

    private var navigationTask: Deferred<Any>? = null

    private suspend fun navigationTaskJoinOrRun(block: suspend () -> Any): Any {
        navigationTask?.let {
            return it.await()
        }

        return coroutineScope {
            val newTask = async {
                block()
            }

            newTask.invokeOnCompletion {
                navigationTask = null
            }

            navigationTask = newTask
            newTask.await()
        }
    }

    fun backToDesktop() {
        activity?.moveTaskToBack(true)

        /*
        startActivity(
            Intent(Intent.ACTION_MAIN).also {
                it.addCategory(Intent.CATEGORY_DEFAULT)
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        */
    }

    open fun onApiError(throwable: Throwable) {
        when (val errorHandler = throwable.handleException { ex -> mainViewModel?.processException(ex) }) {
            is ExceptionResult.RefreshTokenExpired -> logoutLocal()
            is ExceptionResult.HttpError -> {
                handleHttpError(errorHandler)
                GeneralUtils.showToast(requireContext(), errorHandler.httpExceptionItem.errorItem.toString())
            }
            is ExceptionResult.Crash -> {
                if (errorHandler.throwable is UnknownHostException) {
                    GeneralUtils.showHttpErrorDialog(requireContext(), HttpErrorMsgType.CHECK_NETWORK)
                } else {
                    GeneralUtils.showToast(requireContext(), "${errorHandler.throwable}")
                }
            }
        }
    }

    open fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        GeneralUtils.showToast(requireContext(), "${errorHandler.httpExceptionItem.errorItem}")
    }

    private fun logoutLocal() {
        view?.let {
            mainViewModel?.clearToken()
            // todo: 05/06/2020
//            findNavController().navigate(R.id.action_to_loginFragment)
        }
    }
}
