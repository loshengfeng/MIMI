package com.dabenxiang.mimi.view.my_pages.pages.like

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyCollectionVideoListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.VideoType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_my_collection_favorites.*
import kotlinx.android.synthetic.main.fragment_my_collection_videos.*
import kotlinx.android.synthetic.main.fragment_my_collection_videos.id_empty_group
import kotlinx.android.synthetic.main.fragment_my_collection_videos.img_page_empty
import kotlinx.android.synthetic.main.fragment_my_collection_videos.layout_refresh
import kotlinx.android.synthetic.main.fragment_my_collection_videos.text_page_empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class LikeMimiVideoFragment(val tab: Int, val myPagesType: MyPagesType) : BaseFragment() {
    private val viewModel: LikeMimiVideoViewModel by viewModels()
    private val myPagesViewModel: MyPagesViewModel by viewModels({ requireParentFragment() })

    private val adapter: LikeMimiVideoAdapter by lazy {
        LikeMimiVideoAdapter(requireContext(), viewModel.viewModelScope, listener, myPagesType)
    }

    override fun getLayoutId() = R.layout.fragment_my_collection_videos

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val listener = object : MyCollectionVideoListener {
        override fun onMoreClick(item: PlayItem, position: Int) {

        }

        override fun onLikeClick(item: PlayItem, position: Int, isLike: Boolean) {
            val dialog = CleanDialogFragment.newInstance(object : OnCleanDialogListener {
                override fun onClean() {
                    viewModel.like(VideoItem(id = item.videoId ?: 0), LikeType.DISLIKE, myPagesType)
                }
            })

            dialog.setMsg(getString(R.string.like_delete_favorite_message))
            dialog.show(
                requireActivity().supportFragmentManager,
                CleanDialogFragment::class.java.simpleName
            )
        }

        override fun onClipCommentClick(item: List<PlayItem>, position: Int) {

        }

        override fun onChipClick(type: VideoType, tag: String) {
            Timber.d("onChipClick")
            val bundle = SearchVideoFragment.createBundle(tag = tag, videoType = type)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_searchVideoFragment,
                    bundle
                )
            )
        }

        override fun onItemClick(item: PlayItem, type: MyPagesType) {
            val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0))
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_playerV2Fragment,
                            bundle
                    )
            )
        }

        override fun onCommentClick(item: PlayItem, type: MyPagesType) {
            Timber.d("onCommentClick, item = $item")
            val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0), true)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_playerV2Fragment,
                    bundle
                )
            )
        }

        override fun onFavoriteClick(
            item: PlayItem,
            position: Int,
            isFavorite: Boolean,
            type: MyPagesType
        ) {
            viewModel.favorite(item, position, myPagesType)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        myPagesViewModel.deleteAll.observe(this, {
            if (tab == it) {
                layout_refresh.isRefreshing = false
                viewModel.deleteAllLike(myPagesType, adapter.snapshot().items)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                layout_refresh?.isRefreshing = loadStates.refresh is LoadState.Loading
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            viewModel.posts(myPagesType).flowOn(Dispatchers.IO).collectLatest {
                adapter.submitData(it)
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        posts_list.adapter = adapter

        viewModel.postCount.observe(viewLifecycleOwner) {
            Timber.i("postCount= $it")
            if (it == 0) {
                text_page_empty.text = getString(R.string.like_empty_msg)
                id_empty_group.visibility = View.VISIBLE
                posts_list.visibility = View.INVISIBLE
            } else {
                id_empty_group.visibility = View.GONE
                posts_list.visibility = View.VISIBLE
            }
            myPagesViewModel.changeDataCount(tab, it)
            layout_refresh.isRefreshing = false
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            adapter.refresh()
        }

        img_page_empty.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.img_love_empty
            )
        )
    }

    override fun onResume() {
        super.onResume()
        if(adapter.snapshot().items.isEmpty()) adapter.refresh()
    }
}