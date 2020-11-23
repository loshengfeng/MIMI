package com.dabenxiang.mimi.view.actress

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.inviteviprecord.InviteVipRecordAdapter
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment
import kotlinx.android.synthetic.main.fragment_actress.*
import kotlinx.android.synthetic.main.fragment_invite_vip_record.*
import kotlinx.android.synthetic.main.fragment_my_follow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ActressFragment : BaseFragment() {

    private val actressAdapter by lazy { ActressAdapter(actressListener) }
    private val actressListener = object : ActressAdapter.EventListener {
        override fun onClickListener(item: ReferrerHistoryItem, position: Int){
            Timber.d("onDetail")
        }
    }

    private val viewModel: ActressViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_actress
    }

    override fun setupFirstTime() {
        super.setupFirstTime()
        rv_hot_actresses.adapter = actressAdapter
        rv_all_actresses.adapter = actressAdapter
    }

    override fun initSettings() {
        super.initSettings()
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }

}