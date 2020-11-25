package com.dabenxiang.mimi.view.clip

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dabenxiang.mimi.NAVIGATE_TO_TOPUP_ACTION
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
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
import java.io.File

class ClipPagerFragment : BaseFragment() {
    private val viewModel: ClipViewModel by viewModels()

    private val clipFuncItem by lazy {
        ClipFuncItem(
            { id, pos -> getClip(id, pos) },
            { id, view, type -> viewModel.loadImage(id, view, type) },
            { item, pos, isFollow -> onFollowClick(item, pos, isFollow) },
            { item, pos, isFavorite -> onFavoriteClick(item, pos, isFavorite) },
            { item, pos, isLike -> onLikeClick(item, pos, isLike) },
            { item -> onCommentClick(item) },
            { onBackClick() },
            { id, error -> viewModel.sendVideoReport(id, error) },
            { onVipClick() },
            { onPromoteClick() },
            { update -> getClips(update) },
            { item, pos, update -> getPostDetail(item, pos, update) }
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
                }
                is LoadState.NotLoading -> {
                    Timber.d("refresh NotLoading endOfPaginationReached:${(loadStatus.refresh as LoadState.NotLoading).endOfPaginationReached}")
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


    private val clipMap: HashMap<String, File> = hashMapOf()
    private val memberPostItems: ArrayList<MemberPostItem> = arrayListOf()

    private var isShowComment = false
    override fun getLayoutId(): Int {
        return R.layout.item_clip_pager
    }

    override val isNavTransparent: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.run {
            this.statusBarColor = ContextCompat.getColor(
                requireContext(),
                R.color.color_black_1
            )
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun setupFirstTime() {
        if (rv_clip.adapter == null) {
            clipAdapter.setClipFuncItem(clipFuncItem)
            rv_clip.adapter = clipAdapter
            (rv_clip.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            PagerSnapHelper().attachToRecyclerView(rv_clip)
            rv_clip.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            val lastPos = clipAdapter.getCurrentPos()
                            val currentPos = (rv_clip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            Timber.d("SCROLL_STATE_IDLE lastPosition: $lastPos, currentPos:$currentPos")
                            takeIf { currentPos >= 0 && currentPos != lastPos }?.run {
                                clipAdapter.pausePlayer()
                                clipAdapter.releasePlayer()
                                clipAdapter.updateCurrentPosition(currentPos)
                                clipAdapter.notifyItemChanged(lastPos)
                                clipAdapter.getMemberPostItem(currentPos)?.run {
                                    clipFuncItem.getPostDetail(this, currentPos, ::updateAfterGetDeducted)
                                }
                                clipAdapter.notifyItemChanged(currentPos, ClipAdapter.PAYLOAD_UPDATE_DEDUCTED)
                            } ?: clipAdapter.updateCurrentPosition(lastPos)
                        }
                    }
                }
            })
            clipFuncItem.getClips(::setupClips)
        }
    }

    private fun setupClips(data: PagingData<MemberPostItem>, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            (rv_clip.adapter as ClipAdapter).submitData(data)
        }
    }

    private fun updateAfterGetDeducted(currentPos: Int, deducted: Boolean) {
        clipAdapter.getMemberPostItem(currentPos)?.run { this.deducted = deducted }
        clipAdapter.notifyItemChanged(currentPos, ClipAdapter.PAYLOAD_UPDATE_DEDUCTED)
    }

    private fun setObservers() {
//        viewModel.clipResult.observe(viewLifecycleOwner, Observer {
//            when (it) {
//                is Loading -> progressHUD.show()
//                is Loaded -> progressHUD.dismiss()
//                is Success -> {
//                    val result = it.result
//                    clipMap[result.first] = result.third
//                    Timber.d("clipResult notifyItemChanged: ${result.second}")
//                    rv_clip.adapter?.notifyItemChanged(
//                        result.second,
//                        ClipAdapter.PAYLOAD_UPDATE_PLAYER
//                    )
//                }
//                is Error -> onApiError(it.throwable)
//            }
//        })
//
//        viewModel.followResult.observe(viewLifecycleOwner, Observer {
//            when (it) {
//                is Loading -> progressHUD?.show()
//                is Loaded -> progressHUD?.dismiss()
//                is Success -> {
//                    rv_clip.adapter?.notifyItemChanged(
//                        it.result,
//                        ClipAdapter.PAYLOAD_UPDATE_UI
//                    )
//                    mainViewModel?.setShowPopHint(
//                        if (memberPostItems[it.result].isFollow) getString(R.string.followed)
//                        else getString(R.string.cancel_follow)
//                    )
//                }
//                is Error -> onApiError(it.throwable)
//            }
//        })
//
//        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
//            when (it) {
//                is Loading -> progressHUD?.show()
//                is Loaded -> progressHUD?.dismiss()
//                is Success -> rv_clip.adapter?.notifyItemChanged(
//                    it.result,
//                    ClipAdapter.PAYLOAD_UPDATE_UI
//                )
//                is Error -> onApiError(it.throwable)
//            }
//        })
//
//        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
//            when (it) {
//                is Loading -> progressHUD?.show()
//                is Loaded -> progressHUD?.dismiss()
//                is Success -> {
//                    rv_clip.adapter?.notifyItemChanged(
//                        it.result,
//                        ClipAdapter.PAYLOAD_UPDATE_UI
//                    )
//                }
//                is Error -> onApiError(it.throwable)
//            }
//        })
//
//        viewModel.postDetailResult.observe(viewLifecycleOwner, {
//            when (it) {
//                is Loading -> progressHUD?.show()
//                is Loaded -> progressHUD?.dismiss()
//                is Success -> {
//                    val position = it.result
//                    if (memberPostItems[position].deducted && isShowComment)
//                        showCommentDialog(memberPostItems[position])
//                    rv_clip.adapter?.notifyItemChanged(
//                        position,
//                        ClipAdapter.PAYLOAD_UPDATE_DEDUCTED
//                    )
//                }
//                is Error -> {
//                    progressHUD?.dismiss()
//                    onApiError(it.throwable)
//                }
//            }
//        })
//
//        viewModel.videoReport.observe(viewLifecycleOwner, {
//            when (it) {
//                is Loading -> progressHUD?.show()
//                is Loaded -> progressHUD?.dismiss()
//                is Success -> {
//                    Timber.i("videoReported")
//                }
//                is Error -> onApiError(it.throwable)
//            }
//        })
//
//        viewModel.clipPostItemListResult.observe(this, {
//            progressHUD.dismiss()
//            Timber.d("@@size: ${it.size}")
//            it.takeIf { list -> list.size > 0 }?.run {
//                setupClips(this.toList().subList(1, this.size))
//            }
//        })
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

    private fun onBackClick() {
        Timber.d("onBackClick")
        Navigation.findNavController(requireView()).navigateUp()
    }

    private fun getClip(id: String, pos: Int) {
        Timber.d("getClip, id: $id, position: $pos")
        viewModel.getClip(id, pos)
    }

    private fun onFollowClick(item: MemberPostItem, pos: Int, isFollow: Boolean) {
        checkStatus {
            Timber.d("onFollowClick, item:$item, pos:$pos, isFollow:$isFollow")
            viewModel.followPost(item, pos, isFollow)
        }
    }

    private fun onFavoriteClick(item: MemberPostItem, pos: Int, isFavorite: Boolean) {
        checkStatus {
            Timber.d("onFavoriteClick,  item:$item, pos:$pos, isFavorite:$isFavorite")
            viewModel.favoritePost(item, pos, isFavorite)
        }
    }

    private fun onLikeClick(item: MemberPostItem, pos: Int, isLike: Boolean) {
        checkStatus {
            Timber.d("onLikeClick, item:$item, pos:$pos, isLike:$isLike")
            viewModel.likePost(item, pos, isLike)
        }
    }

    private fun onCommentClick(item: MemberPostItem) {
        checkStatus {
            Timber.d("onCommentClick, item:$item")
            showCommentDialog(item)
        }
    }

    private fun getClips(update: ((PagingData<MemberPostItem>, CoroutineScope) -> Unit)) {
        lifecycleScope.launch {
            viewModel.getClips().collectLatest {
                update(it, this)
            }
        }
    }

    private fun getPostDetail(item: MemberPostItem, position: Int, update: (Int, Boolean) -> Unit) {
        viewModel.getPostDetail(item, position, update)
    }

    private fun showCommentDialog(item: MemberPostItem) {
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
                val index = memberPostItems.indexOf(item)
                if (index >= 0) {
                    memberPostItems[index].commentCount = count
//                    rv_clip.adapter?.notifyItemChanged(index, ClipAdapter.PAYLOAD_UPDATE_UI)
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
