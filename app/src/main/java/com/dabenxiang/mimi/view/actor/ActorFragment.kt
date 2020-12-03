package com.dabenxiang.mimi.view.actor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.model.api.vo.ActorVideoItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.actorvideos.ActorVideosFragment
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import kotlinx.android.synthetic.main.fragment_actor.*
import kotlinx.android.synthetic.main.fragment_actor.tv_search
import timber.log.Timber

class ActorFragment : BaseFragment() {

    companion object {
        const val VIEW_TYPE_ACTOR_HEADER = 1
        const val VIEW_TYPE_ACTOR_VIDEOS = 2
        const val VIEW_TYPE_ACTOR_LIST = 3
    }

    private val actorVideosAdapter by lazy {
        ActorVideosAdapter(requireContext(),
            ActorVideosFuncItem(
                getActorAvatarAttachment =  { id, view -> viewModel.loadImage(id, view, LoadImageType.AVATAR_CS) },
                onVideoClickListener = { actorVideoItem, position -> onVideoClickListener(actorVideoItem, position) },
                onActorClickListener = { actorVideosItem, position -> onActorClickListener(actorVideosItem, position) }
            )
        ) }

    private fun onVideoClickListener(item: ActorVideoItem, position: Int){
        navToPlayer(PlayerItem(item.id))
    }

    private val actorListAdapter by lazy {
        ActorListAdapter(requireContext(),
            ActorListFuncItem(
                getActorAvatarAttachment =  { id, view -> viewModel.loadImage(id, view, LoadImageType.AVATAR_CS) },
                onActorClickListener = { id, position -> onActorClickListener(id, position) }
            )
        ) }

    private val baseGridConcatAdapter by lazy {
        BaseGridConcatAdapter(requireContext(), actorListAdapter, 4)
    }

    private fun onActorClickListener(id: Long, position: Int){
        navToActorVideosFragment(id)
    }

    private val concatAdapter by lazy {
        ConcatAdapter(
            ActorHeaderAdapter(getString(R.string.actor_hot_actresses),requireContext()),
            actorVideosAdapter,
            ActorHeaderAdapter(getString(R.string.actor_all_actresses),requireContext()),
            baseGridConcatAdapter
        )
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
        rv_all_actresses.adapter = concatAdapter

    }

    override fun initSettings() {
        super.initSettings()
        viewModel.getActors()
        viewModel.getData(actorListAdapter)
    }

    override fun setupObservers() {
        viewModel.actorVideosResult.observe(viewLifecycleOwner, Observer {
            when (it.first) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Success -> {
                    val actorVideos = (it.first as ApiResult.Success).result
                    actorVideosAdapter.setupData(actorVideos)
                    actorVideosAdapter.notifyDataSetChanged()
                }
                is ApiResult.Error -> onApiError((it.first as ApiResult.Error<ArrayList<ActorCategoriesItem>>).throwable)
            }
        })

        viewModel.actorsCount.observe(viewLifecycleOwner, Observer {
            Timber.d("All actors count is $it")
        })
    }

    override fun setupListeners() {
        tv_search.setOnClickListener {
            navToSearch()
        }
    }

    private fun navToActorVideosFragment(id: Long) {
        val bundle = ActorVideosFragment.createBundle(id)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_mimiFragment_to_actorVideosFragment,
                bundle
            )
        )
    }

    private fun navToPlayer(item: PlayerItem){
        val bundle = PlayerV2Fragment.createBundle(item)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_navigation_player,
                bundle
            )
        )
    }

    private fun navToSearch() {
        val bundle = SearchVideoFragment.createBundle(category = "女优")
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_searchVideoFragment,
                bundle
            )
        )
    }
}