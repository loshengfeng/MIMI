package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
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
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
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
import kotlinx.android.synthetic.main.item_ad.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

class MyCollectionMimiVideoFragment(val tab:Int, val type: MyPagesType) : BaseFragment() {
    private val viewModel: MyCollectionMimiVideoViewModel by viewModels()
    private val myPagesViewModel: MyPagesViewModel by viewModels({requireParentFragment()})

    private val adapter: MyCollectionMimiVideoAdapter by lazy {
        MyCollectionMimiVideoAdapter(requireContext(), viewModel.viewModelScope, listener, type)
    }

    val pageCode = MyPagesPostMediator::class.simpleName+ type.toString()

    override fun getLayoutId() = R.layout.fragment_my_collection_videos

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.likePostResult.observe(this) {
            when (it) {
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        }

        myPagesViewModel.deleteAll.observe(this){
            if(tab == it){
                viewModel.deleteVideos(type, adapter.snapshot().items)
            }
        }

        viewModel.cleanResult.observe(this) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Empty -> {
                    Timber.i("cleanResult items:${adapter.snapshot().items.isEmpty()}")
                    emptyPageToggle(true)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }

        }

        viewModel.videoFavoriteResult.observe(this) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    if(adapter.snapshot().items.size <=1) {
                        viewModel.viewModelScope.launch {
                            val dbSize=viewModel.checkoutItemsSize(pageCode)
                            if(dbSize<=0) emptyPageToggle(true)
                        }
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.deleteFavoriteResult.observe(this) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    if(adapter.snapshot().items.size <=1) {
                        viewModel.viewModelScope.launch {
                            val dbSize=viewModel.checkoutItemsSize(pageCode)
                            if(dbSize<=0) emptyPageToggle(true)
                        }
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }
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


        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            adapter.refresh()
        }

        img_page_empty.setImageDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.img_love_empty
        ))
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
            viewModel.posts(pageCode, type).flowOn(Dispatchers.IO).collectLatest {
                adapter.submitData(it)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            @OptIn(FlowPreview::class)
            adapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .onEach { delay(1000) }
                .collect {
                    if(adapter.snapshot().items.isEmpty()&& timeout >0) {
                        timeout--
                        adapter.refresh()
                    }
                }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
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
                        likeType = item.likeType,
                        likeCount = item.likeCount?:0

                ), if(item.likeType == LikeType.LIKE) LikeType.DISLIKE else LikeType.LIKE, type)
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
            if (this@MyCollectionMimiVideoFragment.type == MyPagesType.FAVORITE_MIMI_VIDEO) {
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
                    viewModel.deleteVideoFavorite(type, item.videoId.toString())
                }

                override fun onCancel() {
                    //do nothing
                }
            })

            dialog.setMsg(getString(R.string.follow_delete_favorite_message))
            dialog.show(
                requireActivity().supportFragmentManager,
                CleanDialogFragment::class.java.simpleName
            )

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