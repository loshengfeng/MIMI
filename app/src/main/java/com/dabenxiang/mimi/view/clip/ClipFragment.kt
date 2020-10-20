package com.dabenxiang.mimi.view.clip

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.comment.CommentDialogFragment
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import kotlinx.android.synthetic.main.fragment_clip.*
import timber.log.Timber
import java.io.File

class ClipFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        const val KEY_POSITION = "position"
        const val KEY_SHOW_COMMENT = "show_comment"

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

    private val clipMap: HashMap<String, File> = hashMapOf()
    private val memberPostItems: ArrayList<MemberPostItem> = arrayListOf()
    private var interactionListener: InteractionListener? = null

    override val bottomNavigationVisibility = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (rv_clip.adapter as ClipAdapter).releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        (rv_clip.adapter as ClipAdapter).pausePlayer()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_clip
    }

    override fun setupFirstTime() {
        initSettings()
    }

    override fun setupObservers() {
        viewModel.clipResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    val result = it.result
                    clipMap[result.first] = result.third
                    Timber.d("clipResult notifyItemChanged: ${result.second}")
                    rv_clip.adapter?.notifyItemChanged(result.second)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> rv_clip.adapter?.notifyItemChanged(
                    it.result,
                    ClipAdapter.PAYLOAD_UPDATE_UI
                )
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> rv_clip.adapter?.notifyItemChanged(
                    it.result,
                    ClipAdapter.PAYLOAD_UPDATE_UI
                )
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    rv_clip.adapter?.notifyItemChanged(
                        it.result,
                        ClipAdapter.PAYLOAD_UPDATE_UI
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.postDetailResult.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    Timber.i("postDetailResult Success")
                    rv_clip.adapter?.notifyItemChanged(
                        it.result,
                        ClipAdapter.PAYLOAD_UPDATE_UI
                    )
                }
                is Error -> {
                    progressHUD?.dismiss()
                    onApiError(it.throwable)
                }

            }
        })

        viewModel.meItem.observe(viewLifecycleOwner, {
            when (it) {
                is Success -> {
                }

                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

    }

    override fun initSettings() {
        val position = arguments?.getInt(KEY_POSITION) ?: 0

        (arguments?.getSerializable(KEY_DATA) as ArrayList<MemberPostItem>).also { data ->
            memberPostItems.addAll(data)
            rv_clip.adapter = ClipAdapter(
                requireContext(),
                memberPostItems,
                clipMap,
                position,
                ClipFuncItem(
                    { id, pos -> getClip(id, pos) },
                    { id, view, type -> viewModel.loadImage(id, view, type) },
                    { item, pos, isFollow -> onFollowClick(item, pos, isFollow) },
                    { item, pos, isFavorite -> onFavoriteClick(item, pos, isFavorite) },
                    { item, pos, isLike -> onLikeClick(item, pos, isLike) },
                    { item -> onCommentClick(item) },
                    { onBackClick() },
                    { item, pos -> viewModel.getMe(item, pos) },
                    { item, error -> viewModel.sendPlayerError(item, error) },
                    { onVipClick()},
                    { onPromoteClick() }
                )
            )

            (rv_clip.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            PagerSnapHelper().attachToRecyclerView(rv_clip)
            rv_clip.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            val currentPos =
                                (rv_clip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            Timber.d("SCROLL_STATE_IDLE position: $currentPos")

                            val clipAdapter = rv_clip.adapter as ClipAdapter
                            val lastPosition = clipAdapter.getCurrentPos()
                            Timber.d("SCROLL_STATE_IDLE lastPosition: $lastPosition")
                            takeIf { currentPos >= 0 && currentPos != lastPosition }?.also {
                                clipAdapter.releasePlayer()
                                clipAdapter.updateCurrentPosition(currentPos)
                                clipAdapter.notifyItemChanged(lastPosition)
                                clipAdapter.notifyItemChanged(currentPos)
                            } ?: clipAdapter.updateCurrentPosition(lastPosition)
                        }
                    }
                }
            })
            rv_clip.scrollToPosition(position)

            (arguments?.getSerializable(KEY_SHOW_COMMENT) as Boolean).takeIf { it }?.also {
                val item = data[position]
                showCommentDialog(item)
            }
        }
    }

    private fun onPromoteClick() {
         //TODO
    }

    private fun onVipClick() {
        navigateTo(NavigateItem.Destination(R.id.action_to_topup))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            interactionListener = context as InteractionListener
        } catch (e: ClassCastException) {
            Timber.e("ClipFragment interaction listener can't cast")
        }
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
                    rv_clip.adapter?.notifyItemChanged(index, ClipAdapter.PAYLOAD_UPDATE_UI)
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
