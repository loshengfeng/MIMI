package com.dabenxiang.mimi.view.login

import android.Manifest
import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel

class LoginFragment: BaseFragment(), OnPermissionListener {

    private val viewModel by viewModel<LoginViewModel>()

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
        const val INSTALL_REQUEST_CODE = 200
    }

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE
    )

//    private var dialog: LoginDialogFragment? = null
//    private val onLoginDialogListener = object : OnLoginDialogListener {
//        override fun onSuccess() {
//            dialog?.dismiss()
//            goHome()
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return  R.layout.fragment_login
    }

    override fun onAllPermissionsGranted() {
        // todo
    }

    private fun goHome() {
        // todo
    }
}