package com.dabenxiang.mimi.view.actress

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_actress.*
import timber.log.Timber

class ActorFragment : BaseFragment() {

    private val actorVideosAdapter by lazy { ActorVideosAdapter(requireContext(), actorListener) }
    private val actorListener = object : ActorVideosAdapter.EventListener {
        override fun onClickListener(item: ReferrerHistoryItem, position: Int){
            Timber.d("onDetail")
        }
    }

    private val viewModel: ActorViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_actress
    }

    override fun setupFirstTime() {
        super.setupFirstTime()
        rv_hot_actresses.adapter = actorVideosAdapter
        rv_all_actresses.adapter = actorVideosAdapter
    }

    override fun initSettings() {
        super.initSettings()
        viewModel.getActorList()
    }

    override fun setupObservers() {
        viewModel.actorVideosResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loaded -> ""
                is ApiResult.Success -> {
                    val actorVideos = it.result
                    actorVideosAdapter.setupData(actorVideos)
                    actorVideosAdapter.notifyDataSetChanged()
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

    }

    override fun setupListeners() {

    }

}