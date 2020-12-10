package com.dabenxiang.mimi.view.my_pages.pages.like

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyCollectionVideoListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.model.enums.VideoType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clipsingle.ClipSingleFragment
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_my_collection_videos.*
import kotlinx.android.synthetic.main.item_ad.view.*
import timber.log.Timber

class LikeMimiVideoFragment(val tab: Int, val type: MyCollectionTabItemType) : BaseFragment() {
    private val viewModel: LikeMimiVideoViewModel by viewModels()
    private val myPagesViewModel: MyPagesViewModel by viewModels({ requireParentFragment() })

    private val adapter: LikeMimiVideoAdapter by lazy {
        LikeMimiVideoAdapter(requireContext(), viewModel.viewModelScope, listener, type)
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
                    viewModel.like(VideoItem(id = item.videoId ?: 0), LikeType.DISLIKE)
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

        override fun onItemClick(item: PlayItem, type: MyCollectionTabItemType) {
            if (this@LikeMimiVideoFragment.type == MyCollectionTabItemType.MIMI_VIDEO) {
                val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0))
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_playerV2Fragment,
                        bundle
                    )
                )
            } else {
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_clipSingleFragment,
                        ClipSingleFragment.createBundle(item)
                    )
                )
            }
        }

        override fun onCommentClick(item: PlayItem, type: MyCollectionTabItemType) {
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
            type: MyCollectionTabItemType
        ) {
            viewModel.favorite(item, position)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        viewModel.deleteFavoriteResult.observe(this) {
            viewModel.getData(adapter)
        }

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.postCount.observe(this) {
            Timber.i("postCount= $it")
            if (it == 0) {
                text_page_empty.text = getString(R.string.like_empty_msg)
                id_empty_group.visibility = View.VISIBLE
                list_short.visibility = View.INVISIBLE
            } else {
                id_empty_group.visibility = View.GONE
                list_short.visibility = View.VISIBLE
            }
            myPagesViewModel.changeDataCount(tab, it)
            layout_refresh.isRefreshing = false
        }

        viewModel.likeResult.observe(this, Observer {
            when (it) {
                is ApiResult.Success -> viewModel.getData(adapter)
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(this, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter.notifyItemChanged(
                        it.result,
                        LikeMimiVideoAdapter.PAYLOAD_UPDATE_FAVORITE
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanResult.observe(this, {
            when (it) {
                is ApiResult.Loading -> layout_refresh.isRefreshing = true
                is ApiResult.Loaded -> layout_refresh.isRefreshing = false
                is ApiResult.Empty -> viewModel.getData(adapter)
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        myPagesViewModel.deleteAll.observe(this, {
            if (tab == it) viewModel.deleteAllLike(adapter.snapshot().items)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list_short.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getData(adapter)
        }

        img_page_empty.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.img_love_empty
            )
        )
    }

    override fun initSettings() {

    }

    override fun onResume() {
        super.onResume()

        if (viewModel.postCount.value ?: -1 <= 0) {
            viewModel.getData(adapter)
        }
    }
}