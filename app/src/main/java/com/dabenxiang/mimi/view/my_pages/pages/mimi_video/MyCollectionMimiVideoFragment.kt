package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MyCollectionVideoListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clipsingle.ClipSingleFragment
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_my_collection_videos.*
import kotlinx.android.synthetic.main.item_ad.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class MyCollectionMimiVideoFragment(val tab:Int, val type: MyPagesType) : BaseFragment() {
    private val viewModel: MyCollectionMimiVideoViewModel by viewModels()
    private val myPagesViewModel: MyPagesViewModel by viewModels({requireParentFragment()})

    private val adapter: MyCollectionMimiVideoAdapter by lazy {
        MyCollectionMimiVideoAdapter(requireContext(), viewModel.viewModelScope, listener, type)
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
            checkStatus {
                viewModel.videoLike(VideoItem(
                        id = item.videoId?:0,
                        favorite = item.favorite ?: false,
                        favoriteCount = item.favoriteCount?:0,
                        like = item.like,
                        likeType = if(item.like==true) LikeType.LIKE else if(item.like==false) LikeType.DISLIKE else null,
                        likeCount = item.likeCount?:0

                ), LikeType.LIKE)
            }
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
            if (this@MyCollectionMimiVideoFragment.type == MyPagesType.MIMI_VIDEO) {
                val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0))
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_playerV2Fragment,
                        bundle
                    ))
            } else {
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_clipSingleFragment,
                        ClipSingleFragment.createBundle(item)
                    ))
            }
        }

        override fun onCommentClick(item: PlayItem, type: MyPagesType) {
            Timber.d("onCommentClick, item = ${item}")
            val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0), true)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_playerV2Fragment,
                            bundle
                    ))
        }

        override fun onFavoriteClick(item: PlayItem, position: Int, isFavorite: Boolean, type: MyPagesType) {
            val dialog = CleanDialogFragment.newInstance(object : OnCleanDialogListener {
                override fun onClean() {
                    checkStatus { viewModel.deleteMIMIVideoFavorite(type, item.videoId.toString()) }
                }
            })

            dialog.setMsg(getString(R.string.follow_delete_favorite_message))
            dialog.show(
                        requireActivity().supportFragmentManager,
                        CleanDialogFragment::class.java.simpleName
                )

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.likePostResult.observe(this, Observer {
            when (it) {
                is ApiResult.Success -> {
                }
                is ApiResult.Error -> Timber.e(it.throwable)
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

            viewModel.posts(type).flowOn(Dispatchers.IO).collectLatest {
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

            emptyPageToggle(it==0)
            myPagesViewModel.changeDataCount(tab, it)
            layout_refresh.isRefreshing = false
        }

        myPagesViewModel.deleteAll.observe(viewLifecycleOwner,  {
            if(tab == it){
                viewModel.deleteVideos(adapter.snapshot().items)
            }
        })


        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            adapter.refresh()
        }

        img_page_empty.setImageDrawable(ContextCompat.getDrawable(requireContext(),
                R.drawable.img_love_empty
        ))
    }

    override fun onResume() {
        super.onResume()
        if(adapter.snapshot().items.isEmpty()) adapter.refresh()
    }

    private fun emptyPageToggle(isHide:Boolean){
        if (isHide) {
            text_page_empty.text = getString(R.string.follow_empty_msg)
            id_empty_group.visibility = View.VISIBLE
            posts_list.visibility = View.INVISIBLE
        } else {
            id_empty_group.visibility = View.GONE
            posts_list.visibility = View.VISIBLE

        }
    }

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
        }
    }
}