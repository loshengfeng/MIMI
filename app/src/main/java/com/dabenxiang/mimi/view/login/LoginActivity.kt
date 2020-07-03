package com.dabenxiang.mimi.view.login

import android.os.Bundle
import androidx.navigation.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseActivity

class LoginActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findNavController(R.id.nav_host_fragment).setGraph(
            R.navigation.navigation_login,
            intent.extras
        )
    }
}