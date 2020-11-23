package com.dabenxiang.mimi.view.club.follow

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.club.ClubViewModel

class FollowFragment : BaseFragment() {

    private val viewModel: ClubViewModel by viewModels()

    override fun getLayoutId() = R.layout.fragment_follow
    override fun setupObservers() {}
    override fun setupListeners() {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
