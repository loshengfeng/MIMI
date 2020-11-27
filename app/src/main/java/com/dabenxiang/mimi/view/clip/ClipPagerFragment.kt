package com.dabenxiang.mimi.view.clip

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dabenxiang.mimi.NAVIGATE_TO_TOPUP_ACTION
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.comment.CommentDialogFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import kotlinx.android.synthetic.main.item_clip_pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


class ClipPagerFragment(private val orderByType: StatisticsOrderType) : BaseFragment() {
    private val viewModel: ClipViewModel by viewModels()

    private val clipFuncItem by lazy {
        ClipFuncItem(
            { id, view, type -> viewModel.loadImage(id, view, type) },
            { item, pos, isFavorite -> onFavoriteClick(item, pos, isFavorite) },
            { item, pos, isLike -> onLikeClick(item, pos, isLike) },
            { item -> onCommentClick(item) },
            { id, error -> viewModel.sendVideoReport(id, error) },
            { onVipClick() },
            { onPromoteClick() },
            { update -> getClips(update) },
            { item, pos, update -> getM3U8(item, pos, update) },
            { pos -> scrollToNext(pos) }
        )
    }

    private val clipAdapter by lazy {
        val adapter = ClipAdapter(requireContext(), clipFuncItem = clipFuncItem)
        val loadStateListener = { loadStatus: CombinedLoadStates ->
            when (loadStatus.refresh) {
                is LoadState.Error -> {
                    Timber.e("refresh Error:${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                }
                is LoadState.Loading -> {
                    Timber.d("refresh Loading endOfPaginationReached:${(loadStatus.refresh as LoadState.Loading).endOfPaginationReached}")
                    progressHUD.show()
                }
                is LoadState.NotLoading -> {
                    Timber.d("refresh NotLoading endOfPaginationReached:${(loadStatus.refresh as LoadState.NotLoading).endOfPaginationReached}")
                    progressHUD.dismiss()
                    takeIf { this@ClipPagerFragment.isVisible && adapter.itemCount > 0 }?.let {
                        adapter.getVideoItem(0)
                    }?.run {
                        clipFuncItem.getM3U8(this, 0, ::updateAfterM3U8)
                    }
                }
            }

            when (loadStatus.append) {
                is LoadState.Error -> {
                    Timber.e("append Error:${(loadStatus.append as LoadState.Error).error.localizedMessage}")
                }
                is LoadState.Loading -> {
                    Timber.d("append Loading endOfPaginationReached:${(loadStatus.append as LoadState.Loading).endOfPaginationReached}")
                }
                is LoadState.NotLoading -> {
                    Timber.d("append NotLoading endOfPaginationReached:${(loadStatus.append as LoadState.NotLoading).endOfPaginationReached}")
                }
            }
        }
        adapter.addLoadStateListener(loadStateListener)
        adapter
    }

    private val onScrollListener by lazy {
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        val lastPos = clipAdapter.getCurrentPos()
                        val currentPos =
                            (rv_clip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        Timber.d("SCROLL_STATE_IDLE lastPosition: $lastPos, currentPos:$currentPos")
                        takeIf { currentPos >= 0 && currentPos != lastPos }?.run {
                            clipAdapter.pausePlayer()
                            clipAdapter.releasePlayer()
                            clipAdapter.updateCurrentPosition(currentPos)
                            clipAdapter.notifyItemChanged(lastPos)
                            clipAdapter.getVideoItem(currentPos)?.run {
                                clipFuncItem.getM3U8(this, currentPos, ::updateAfterM3U8)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.item_clip_pager
    }

    override val isNavTransparent: Boolean = true
    override val isStatusBarDark: Boolean = true

    override fun onPause() {
        super.onPause()
        clipAdapter.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        clipAdapter.releasePlayer()
    }

    override fun setupFirstTime() {
        if (rv_clip.adapter == null) {
            clipAdapter.setClipFuncItem(clipFuncItem)
            rv_clip.adapter = clipAdapter
            (rv_clip.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            PagerSnapHelper().attachToRecyclerView(rv_clip)
            rv_clip.addOnScrollListener(onScrollListener)
            clipFuncItem.getClips(::setupClips)
        }
    }

    private fun setupClips(data: PagingData<VideoItem>, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            (rv_clip.adapter as ClipAdapter).submitData(data)
        }
    }

    private fun updateAfterM3U8(currentPos: Int, url: String, errorCode: Int) {
        clipAdapter.setM3U8Result(url, errorCode)
        clipAdapter.notifyItemChanged(currentPos, ClipAdapter.PAYLOAD_UPDATE_AFTER_M3U8)
    }

    override fun setupObservers() {
        viewModel.favoriteResult.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> progressHUD.show()
                is Loaded -> progressHUD.dismiss()
                is Success -> rv_clip.adapter?.notifyItemChanged(
                    it.result,
                    ClipAdapter.PAYLOAD_UPDATE_UI
                )
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> progressHUD.show()
                is Loaded -> progressHUD.dismiss()
                is Success -> {
                    rv_clip.adapter?.notifyItemChanged(
                        it.result,
                        ClipAdapter.PAYLOAD_UPDATE_UI
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    private fun onPromoteClick() {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_inviteVipFragment,
                null
            )
        )
    }

    private fun onVipClick() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.action = NAVIGATE_TO_TOPUP_ACTION
        startActivity(intent)
    }

    private fun onFavoriteClick(item: VideoItem, pos: Int, isFavorite: Boolean) {
        checkStatus {
            Timber.d("onFavoriteClick,  item:$item, pos:$pos, isFavorite:$isFavorite")
            viewModel.modifyFavorite(item, pos, isFavorite)
        }
    }

    private fun onLikeClick(item: VideoItem, pos: Int, isLike: Boolean) {
        checkStatus {
            Timber.d("onLikeClick, item:$item, pos:$pos, isLike:$isLike")
            viewModel.likePost(item, pos, isLike)
        }
    }

    private fun onCommentClick(item: VideoItem) {
        checkStatus {
            Timber.d("onCommentClick, item:$item")
            showCommentDialog(item)
        }
    }

    private fun getClips(update: ((PagingData<VideoItem>, CoroutineScope) -> Unit)) {
        lifecycleScope.launch {
            viewModel.getClips(orderByType).collectLatest {
                update(it, this)
            }
        }
    }

    private fun getM3U8(item: VideoItem, position: Int, update: (Int, String, Int) -> Unit) {
        viewModel.getM3U8(item, position, update)
    }

    private fun scrollToNext(nextPosition: Int) {
        rv_clip.smoothScrollToPosition(nextPosition)
    }

    private fun showCommentDialog(item: VideoItem) {
        val listener = object : CommentDialogFragment.CommentListener {
            override fun onAvatarClick(userId: Long, name: String) {
                val bundle = MyPostFragment.createBundle(
                    userId, name,
                    isAdult = true,
                    isAdultTheme = true
                )
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_myPostFragment,
                        bundle
                    )
                )
            }

            override fun onUpdateCommentCount(count: Int) {
                val currentPos = clipAdapter.getCurrentPos()
                if (currentPos >= 0) {
                    clipAdapter.getVideoItem(currentPos)?.commentCount = count.toLong()
                    rv_clip.adapter?.notifyItemChanged(currentPos, ClipAdapter.PAYLOAD_UPDATE_UI)
                }
            }
        }
        CommentDialogFragment.newInstance(item, listener).also {
            it.isCancelable = true
            it.show(
                requireActivity().supportFragmentManager,
                CommentDialogFragment::class.java.simpleName
            )
        }
    }
}
