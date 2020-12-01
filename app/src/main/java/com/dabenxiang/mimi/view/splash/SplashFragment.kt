package com.dabenxiang.mimi.view.splash

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.MIMI_INVITE_CODE
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Empty
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.UpdateMessageAlertDialog
import com.dabenxiang.mimi.view.listener.OnSimpleDialogListener
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.GeneralUtils.installApk
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import tw.gov.president.manager.submanager.update.callback.DownloadProgressCallback
import tw.gov.president.manager.submanager.update.data.VersionStatus

class SplashFragment : BaseFragment() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate")
        setVersionObserve()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_splash
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        val requestList = getNotGrantedPermissions(externalPermissions)
        if (requestList.size > 0) {
            requestPermissions(
                requestList.toTypedArray(),
                PERMISSION_EXTERNAL_REQUEST_CODE
            )
        } else {
            firstTimeCheck()
            checkVersion()
        }
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun setupObservers() {

        viewModel.apiError.observe(viewLifecycleOwner, Observer { isError ->
            if (isError) {
                initSettings()
            }
        })
    }


    private fun setVersionObserve() =
        viewModel.versionStatus.observe(this, Observer {

            Timber.i("versionStatus=$it   isVersionChecked=${mainViewModel?.isVersionChecked}")
            if (mainViewModel?.isVersionChecked == true) return@Observer
            when (it) {
                VersionStatus.UPDATE -> {
                    if (viewModel.isUpgradeApp()) {
                        updateDialog(requireContext())
                    } else {
                        initSettings()
                    }
                }

                VersionStatus.FORCE_UPDATE -> updateDialog(requireContext(), true)
                else -> {
                    pb_update.progress = 100
                    initSettings()
                }
            }
        })


    override fun setupListeners() {}

//    private fun requestPermissions() {
//        val requestList = getNotGrantedPermissions(externalPermissions)
//
//        if (requestList.size > 0) {
//            requestPermissions(requestList.toTypedArray(), PERMISSION_EXTERNAL_REQUEST_CODE)
//        } else {
//            checkVersion()
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult")
        if (requestCode == PERMISSION_EXTERNAL_REQUEST_CODE
            && getNotGrantedPermissions(externalPermissions).isEmpty()
        ) {
            Timber.i("onRequestPermissionsResult check ok")
            firstTimeCheck()
            if (mainViewModel?.isVersionChecked == true) {
                viewModel.updateApp(progressCallback)
            } else {
                checkVersion()
            }
        } else {
            checkVersion()
        }
    }

    override fun initSettings() {
        super.initSettings()
        viewModel.autoLoginResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    mainViewModel?.startMQTT()
                    deleteCacheFile()
                    goToHomePage()
                }
                is Error -> {
                    onApiError(it.throwable)
                    viewModel.logoutLocal()
                    goToHomePage()
                }
            }
        })

        viewModel.getDecryptSettingResult()
        viewModel.autoLogin()
    }

    private fun goToHomePage() {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.Main) {
                navigateTo(NavigateItem.Destination(R.id.action_splashFragment_to_mimiFragment))
            }
        }
    }

    private fun checkVersion() {
        mainViewModel?.isVersionChecked = false
        viewModel.checkVersion()
        planned_speed.setText(R.string.check_version)
    }

    private fun updateDialog(context: Context, isForceUpdate: Boolean = false) {
        Timber.d("updateDialog")
        UpdateMessageAlertDialog(
            context,
            R.string.updated_version_title,
            R.string.update_immediately,
            if (isForceUpdate) R.string.btn_close else R.string.remind_later,
            object : OnSimpleDialogListener {
                override fun onConfirm() {
                    val requestList = getNotGrantedPermissions(externalPermissions)
                    Timber.d("AppDownloadRequestPermissions : ${requestList.size}")
                    if (requestList.size > 0) {
                        requestPermissions(
                            requestList.toTypedArray(),
                            PERMISSION_EXTERNAL_REQUEST_CODE
                        )
                    } else {
                        viewModel.updateApp(progressCallback)
                    }
                    mainViewModel?.isVersionChecked = true
                }

                override fun onCancel() {
                    if (isForceUpdate) {
                        activity?.finish()
                    } else {
                        viewModel.setupRecordTimestamp()
                        initSettings()
                        mainViewModel?.isVersionChecked = true
                    }
                }

            }
        ).show()
    }

    private val progressCallback = object :
        DownloadProgressCallback {
        override fun schedule(longId: Long, totalSize: Int, currentSize: Int, status: Int) {
            Timber.d("progress: ${((currentSize.toFloat() / totalSize.toFloat()) * 100).toInt()}")
            pb_update.progress = ((currentSize.toFloat() / totalSize.toFloat()) * 100).toInt()
            lifecycleScope.launch {
                planned_speed.text =
                    String.format(
                        getString(R.string.update_version),
                        ((currentSize.toFloat() / totalSize.toFloat()) * 100).toInt()
                    )
            }
        }

        override fun complete(longId: Long, path: String, mimeType: String) {
            Timber.d("complete path: $path, mimeType: $mimeType")
            pb_update.progress = 100

            lifecycleScope.launch {
                planned_speed.text =
                    String.format(getString(R.string.update_version), 100)
            }
            installApk(App.applicationContext(), path)
        }
    }

    private fun deleteCacheFile() {
        mainViewModel?.deleteCacheFile(requireActivity().cacheDir)
    }

    private fun firstTimeCheck() {
        getPromoteCode().takeIf {
//            !FileUtil.isSecreteFileExist(requireContext()) && !TextUtils.isEmpty(it)
            !FileUtil.isSecreteFileExist(requireContext())
        }?.run {
            viewModel.firstTimeStatistics(requireContext(), this)
        }
    }

    private fun getPromoteCode() =
        GeneralUtils.getCopyText(requireContext()).takeIf { it.contains(MIMI_INVITE_CODE) }?.let {
            val startIndex = it.lastIndexOf(MIMI_INVITE_CODE) + MIMI_INVITE_CODE.length
            it.substring(startIndex, it.length)
        } ?: let { "" }

}