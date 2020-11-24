package com.dabenxiang.mimi.view.generalvideo

import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment

class GeneralVideoFragment : BaseFragment() {

    private val viewModel: GeneralVideoViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_general_video
    }

}