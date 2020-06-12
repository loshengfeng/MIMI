package com.dabenxiang.mimi.view.myfollow

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_my_follow.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MyFollowFragment : BaseFragment<MyFollowViewModel>() {
    private val viewModel by viewModel<MyFollowViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_my_follow }

    override fun fetchViewModel(): MyFollowViewModel? { return viewModel }

    override fun setupObservers() {
        Timber.d("setupObservers")
    }

    override fun setupListeners() {
        View.OnClickListener { btnView ->
            when(btnView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
            }
        }.also {
            tv_back.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        super.initSettings()
        tv_clean.visibility = View.VISIBLE
        tv_title.setText(R.string.follow_title)
        tv_all.text = getString(R.string.follow_all, "32")
    }
}