package com.dabenxiang.mimi.view.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Empty
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SplashFragment : BaseFragment() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 637
    }

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

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

    override fun setupObservers() {}

    override fun setupListeners() {}

    private fun requestPermissions() {
        val requestList = arrayListOf<String>()
        for (i in permissions.indices) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permissions[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestList.add(permissions[i])
            }
        }

        if (requestList.size > 0) {
            requestPermissions(requestList.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            initSettings()
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
                initSettings()
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
                    lifecycleScope.launch(Dispatchers.IO) {
                        delay(1000)
                        withContext(Dispatchers.Main) {
                            navigateTo(NavigateItem.Destination(R.id.action_splashFragment_to_homeFragment))
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })
        viewModel.autoLogin()
    }
}