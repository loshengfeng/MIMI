package com.dabenxiang.mimi.view.actor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_actor.*
import timber.log.Timber

class ActorFragment : BaseFragment() {

    private val actorVideosAdapter by lazy {
        ActorVideosAdapter(requireContext(),
            ActorVideosFuncItem(
                getActorAvatarAttachment =  { id, view -> viewModel.loadImage(id, view, LoadImageType.AVATAR_CS) },
                onClickListener = { actorVideosItem, position -> onClickListener(actorVideosItem, position) }
            )
        ) }

    private fun onClickListener(item: ActorVideosItem, position: Int){
        Timber.d("actorVideosItem onDetail: ${item.name}")
    }

    private val actorCategoriesAdapter by lazy {
        ActorCategoriesAdapter(requireContext(),
            ActorCategoriesFuncItem(
                getActorAvatarAttachment =  { id, view -> viewModel.loadImage(id, view, LoadImageType.AVATAR_CS) },
                onClickListener = { actorCategoriesItem, position -> onCategoriesClickListener(actorCategoriesItem, position) }
            )
        ) }

    private fun onCategoriesClickListener(item: ActorCategoriesItem, position: Int){
            Timber.d("actorCategoriesItem onDetail: ${item.name}")
    }

    private val viewModel: ActorViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_actor
    }

    override fun setupFirstTime() {
        super.setupFirstTime()
        rv_hot_actresses.layoutManager = LinearLayoutManager(context)
        rv_hot_actresses.adapter = actorVideosAdapter
        rv_all_actresses.layoutManager = GridLayoutManager(context, 4)
        rv_all_actresses.adapter = actorCategoriesAdapter
    }

    override fun initSettings() {
        super.initSettings()
        viewModel.getActorList()
    }

    override fun setupObservers() {
        viewModel.actorVideosResult.observe(viewLifecycleOwner, Observer {
            when (it.first) {
                is ApiResult.Loaded -> ""
                is ApiResult.Success -> {
                    val actorVideos = (it.first as ApiResult.Success).result
                    actorVideosAdapter.setupData(actorVideos)
                    actorVideosAdapter.notifyDataSetChanged()
                    val actorCategories = (it.second as ApiResult.Success).result
                    actorCategoriesAdapter.setupData(actorCategories)
                    actorCategoriesAdapter.notifyDataSetChanged()

                }
                is ApiResult.Error -> onApiError((it.first as ApiResult.Error<ArrayList<ActorCategoriesItem>>).throwable)
            }
        })

    }

    override fun setupListeners() {

    }

}