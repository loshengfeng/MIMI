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
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.handleException
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
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

    open fun onApiError(throwable: Throwable, onHttpErrorBlock: ((ExceptionResult.HttpError) -> Unit)? = null) {
        when (val errorHandler =
            throwable.handleException { ex -> mainViewModel?.processException(ex) }) {
            is ExceptionResult.RefreshTokenExpired -> logoutLocal()
            is ExceptionResult.HttpError -> {
                if (onHttpErrorBlock == null) handleHttpError(errorHandler)
                else onHttpErrorBlock(errorHandler)
            }
            is ExceptionResult.Crash -> {
                if (errorHandler.throwable is UnknownHostException) {
                    showCrashDialog(HttpErrorMsgType.CHECK_NETWORK)
                } else {
                    GeneralUtils.showToast(context!!, errorHandler.throwable.toString())
                }
            }
        }
    }

    open fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = errorHandler.httpExceptionItem.errorItem.toString(),
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_confirm)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private fun showCrashDialog(type: HttpErrorMsgType = HttpErrorMsgType.API_FAILED) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = when(type) {
                    HttpErrorMsgType.API_FAILED -> getString(R.string.api_failed_msg)
                    HttpErrorMsgType.CHECK_NETWORK -> getString(R.string.server_error)
                },
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_close)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private fun logoutLocal() {
        view?.let {
            mainViewModel?.logoutLocal()
        }
    }
}
