package com.dabenxiang.mimi.view.clip

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.NAVIGATE_TO_TOPUP_ACTION
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clubdetail.ClubDetailFragment
import com.dabenxiang.mimi.view.dialog.comment.CommentDialogFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.order.OrderFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_clip.*
import kotlinx.android.synthetic.main.fragment_clip.tl_type
import kotlinx.android.synthetic.main.fragment_clip.viewPager
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class ClipFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        const val KEY_POSITION = "position"
        const val KEY_SHOW_COMMENT = "show_comment"

        val tabTitle = arrayListOf(
            App.self.getString(R.string.clip_newest),
            App.self.getString(R.string.clip_top_hit)
        )

        fun createBundle(
            items: ArrayList<MemberPostItem>,
            position: Int,
            showComment: Boolean = false
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, items)
                it.putInt(KEY_POSITION, position)
                it.putBoolean(KEY_SHOW_COMMENT, showComment)
            }
        }
    }

    private val viewModel: ClipViewModel by viewModels()

    private val clipPagerAdapter by lazy {
        ClipPagerAdapter(ClipFuncItem(
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
            { update -> getClips(update) }
        ))
    }

    private val clipMap: HashMap<String, File> = hashMapOf()
    private val memberPostItems: ArrayList<MemberPostItem> = arrayListOf()

    private var isShowComment = false

    override val bottomNavigationVisibility = View.VISIBLE

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("@@onDestroyView")
//        (rv_clip.adapter as ClipAdapter).releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        Timber.d("@@onPause")
//        (rv_clip.adapter as ClipAdapter).pausePlayer()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_clip
    }

    override fun setupFirstTime() {
        initSettings()
        setObservers()
        progressHUD.show()
        viewModel.getClipPosts()
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

    override fun setupListeners() {
    }

    override fun initSettings() {
        viewPager.adapter = clipPagerAdapter
        TabLayoutMediator(tl_type, viewPager) { tab, position ->
            tab.text = ClubDetailFragment.tabTitle[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    private fun setupClips(data: List<MemberPostItem> = arrayListOf(), position: Int = 0) {
//        memberPostItems.addAll(data)
//        rv_clip.adapter = ClipAdapter(
//            requireContext(),
//            clipMap,
//            position,
//            ClipFuncItem(
//                { id, pos -> getClip(id, pos) },
//                { id, view, type -> viewModel.loadImage(id, view, type) },
//                { item, pos, isFollow -> onFollowClick(item, pos, isFollow) },
//                { item, pos, isFavorite -> onFavoriteClick(item, pos, isFavorite) },
//                { item, pos, isLike -> onLikeClick(item, pos, isLike) },
//                { item -> onCommentClick(item) },
//                { onBackClick() },
//                { id, error -> viewModel.sendVideoReport(id, error) },
//                { onVipClick() },
//                { onPromoteClick() },
//                { update -> getClips(update) }
//            )
//        )
//
//        rv_clip.scrollToPosition(position)

//        viewModel.getPostDetail(memberPostItems[position], position)
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
