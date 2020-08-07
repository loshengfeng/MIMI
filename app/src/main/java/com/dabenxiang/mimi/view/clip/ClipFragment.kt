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
import com.dabenxiang.mimi.view.main.MainActivity
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
        (rv_third.adapter as ClipAdapter).releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        (rv_third.adapter as ClipAdapter).pausePlayer()
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
                    rv_third.adapter?.notifyItemChanged(result.second)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.bitmapResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> rv_third.adapter?.notifyItemChanged(
                    it.result,
                    ClipAdapter.PAYLOAD_UPDATE_UI
                )
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> rv_third.adapter?.notifyItemChanged(
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
                is Success -> rv_third.adapter?.notifyItemChanged(
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
                is Success -> rv_third.adapter?.notifyItemChanged(
                    it.result,
                    ClipAdapter.PAYLOAD_UPDATE_UI
                )
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
            rv_third.adapter = ClipAdapter(
                requireContext(),
                memberPostItems,
                clipMap,
                position,
                ClipFuncItem(
                    { id, pos -> getClip(id, pos) },
                    { id, pos -> getBitmap(id, pos) },
                    { item, pos, isFollow -> onFollowClick(item, pos, isFollow) },
                    { item, pos, isFavorite -> onFavoriteClick(item, pos, isFavorite) },
                    { item, pos, isLike -> onLikeClick(item, pos, isLike) },
                    { item -> onCommentClick(item) },
                    { onBackClick() })
            )
            (rv_third.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            PagerSnapHelper().attachToRecyclerView(rv_third)
            rv_third.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            val currentPos =
                                (rv_third.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            Timber.d("SCROLL_STATE_IDLE position: $currentPos")

                            val clipAdapter = rv_third.adapter as ClipAdapter
                            val lastPosition = clipAdapter.getCurrentPos()
                            Timber.d("SCROLL_STATE_IDLE lastPosition: $lastPosition")
                            takeIf { currentPos>0 && currentPos != lastPosition }?.also {
                                clipAdapter.releasePlayer()
                                clipAdapter.updateCurrentPosition(currentPos)
                                clipAdapter.notifyItemChanged(lastPosition)
                                clipAdapter.notifyItemChanged(currentPos)
                            } ?: clipAdapter.updateCurrentPosition(lastPosition)
                        }
                    }
                }
            })
            rv_third.scrollToPosition(position)

            (arguments?.getSerializable(KEY_SHOW_COMMENT) as Boolean).takeIf { it }?.also {
                val item = data[position]
                showCommentDialog(item)
            }
        }
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

    private fun getBitmap(id: String, pos: Int) {
        Timber.d("getCover, id: $id, position: $pos")
        viewModel.getBitmap(id, pos)
    }

    private fun onFollowClick(item: MemberPostItem, pos: Int, isFollow: Boolean) {
        Timber.d("onFollowClick, item:$item, pos:$pos, isFollow:$isFollow")
        viewModel.followPost(item, pos, isFollow)
    }

    private fun onFavoriteClick(item: MemberPostItem, pos: Int, isFavorite: Boolean) {
        Timber.d("onFavoriteClick,  item:$item, pos:$pos, isFavorite:$isFavorite")
        viewModel.favoritePost(item, pos, isFavorite)
    }

    private fun onLikeClick(item: MemberPostItem, pos: Int, isLike: Boolean) {
        Timber.d("onLikeClick, item:$item, pos:$pos, isLike:$isLike")
        viewModel.likePost(item, pos, isLike)
    }

    private fun onCommentClick(item: MemberPostItem) {
        Timber.d("onCommentClick, item:$item")
        showCommentDialog(item)
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
                        R.id.action_clipFragment_to_myPostFragment,
                        bundle
                    )
                )
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
