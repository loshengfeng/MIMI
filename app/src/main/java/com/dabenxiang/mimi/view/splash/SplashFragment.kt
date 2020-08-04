package com.dabenxiang.mimi.view.splash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Empty
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.UpdateMessageAlertDialog
import com.dabenxiang.mimi.view.listener.OnSimpleDialogListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils.installApk
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import tw.gov.president.manager.submanager.update.callback.DownloadProgressCallback
import tw.gov.president.manager.submanager.update.data.VersionStatus

class SplashFragment : BaseFragment() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 637
    }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val externalPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val cameraPermissions = arrayOf(Manifest.permission.CAMERA)

    private val permissions = locationPermissions + externalPermissions + cameraPermissions

    private val viewModel: SplashViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_splash
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun setupObservers() {
        viewModel.versionStatus.observe(viewLifecycleOwner, Observer {
            when (it) {
                VersionStatus.UPDATE -> {
                    if (viewModel.isUpgradeApp()) {
                        updateDialog(requireContext())
                    } else {
                        initSettings()
                    }
                }
                VersionStatus.FORCE_UPDATE -> updateDialog(requireContext())
                else -> {
                    pb_update.progress = 100
                    initSettings()
                }
            }
        })

        viewModel.apiError.observe(viewLifecycleOwner, Observer { isError ->
            if (isError) {
                initSettings()
            }
        })
    }

    override fun setupListeners() {}

    private fun requestPermissions() {
        val requestList = getNotGrantedPermissions(permissions)
        for (i in requestList.indices) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    requestList[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestList.add(requestList[i])
            }
        }

        if (requestList.size > 0) {
            requestPermissions(requestList.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
//            initSettings()
            checkVersion()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var isPermissionAllGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    isPermissionAllGranted = false
                    break
                }
            }

            if (isPermissionAllGranted) {
//                initSettings()
                checkVersion()
            } else {
                requestPermissions()
            }
        }
    }

    override fun initSettings() {
        super.initSettings()
        viewModel.autoLoginResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    goToHomePage()
                }
                is Error -> {
                    onApiError(it.throwable)
                    viewModel.logoutLocal()
                    goToHomePage()
                }
            }
        })
        viewModel.autoLogin()
    }

    private fun goToHomePage() {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.Main) {
                navigateTo(NavigateItem.Destination(R.id.action_splashFragment_to_homeFragment))
            }
        }
    }

    private fun checkVersion() {
        viewModel.checkVersion()
        planned_speed.setText(R.string.check_version)
    }

    private fun updateDialog(context: Context) {
        UpdateMessageAlertDialog(
            context,
            R.string.updated_version_title,
            R.string.update_immediately,
            R.string.remind_later,
            object : OnSimpleDialogListener {
                override fun onConfirm() {
                    val requestList = getNotGrantedPermissions(externalPermissions)
                    Timber.d("AppDownloadRequestPermissions : ${requestList.size}")
                    if (requestList.size > 0) {
                        requestPermissions(
                            requestList.toTypedArray(),
                            PERMISSION_REQUEST_CODE
                        )
                    } else {
                        viewModel.updateApp(progressCallback)
                    }

                }

                override fun onCancle() {
                    viewModel.setupRecordTimestamp()
                    initSettings()

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
}