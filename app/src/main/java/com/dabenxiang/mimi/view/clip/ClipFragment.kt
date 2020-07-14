package com.dabenxiang.mimi.view.clip

import android.os.Bundle
import android.view.View
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
import kotlinx.android.synthetic.main.fragment_clip.*
import timber.log.Timber
import java.io.File

class ClipFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        const val KEY_POSITION = "position"

        fun createBundle(
            items: ArrayList<MemberPostItem>,
            position: Int
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, items)
                it.putInt(KEY_POSITION, position)
            }
        }
    }

    private val viewModel: ClipViewModel by viewModels()

    private val clipMap: HashMap<String, File> = hashMapOf()
    private val memberPostItems: ArrayList<MemberPostItem> = arrayListOf()

    override val bottomNavigationVisibility = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (rv_clip.adapter as ClipAdapter).releasePlayer()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_clip
    }

    override fun setupObservers() {
        viewModel.clipResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    val result = it.result
                    clipMap[result.first] = result.third
                    rv_clip.adapter?.notifyItemChanged(result.second)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.coverResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    rv_clip.adapter?.notifyItemChanged(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> rv_clip.adapter?.notifyItemChanged(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> rv_clip.adapter?.notifyItemChanged(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> rv_clip.adapter?.notifyItemChanged(it.result)
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

    }

    override fun initSettings() {
        val position = arguments?.getInt(KEY_POSITION) ?: 0
        (arguments?.getSerializable(KEY_DATA) as ArrayList<MemberPostItem>).also { data ->
            Timber.d("data: $data")
            memberPostItems.addAll(data)
            rv_clip.adapter = ClipAdapter(
                requireContext(),
                memberPostItems,
                clipMap,
                position,
                ClipFuncItem(
                    { id, pos -> getClip(id, pos) },
                    { id, pos -> getCover(id, pos) },
                    { item, pos, isFollow -> onFollowClick(item, pos, isFollow) },
                    { item, pos, isFavorite -> onFavoriteClick(item, pos, isFavorite) },
                    { item, pos, isLike -> onLikeClick(item, pos, isLike) },
                    { onBackClick() })
            )
            (rv_clip.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            PagerSnapHelper().attachToRecyclerView(rv_clip)
            rv_clip.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            val currentPos = (rv_clip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            Timber.d("SCROLL_STATE_IDLE position: $currentPos")
                            val clipAdapter = rv_clip.adapter as ClipAdapter
                            val lastPosition = clipAdapter.getCurrentPos()
                            takeIf { currentPos != lastPosition }?.also {
                                clipAdapter.updateCurrentPosition(currentPos)
                                clipAdapter.notifyItemChanged(lastPosition)
                                clipAdapter.notifyItemChanged(currentPos)
                            }
                        }
                    }
                }
            })
            rv_clip.scrollToPosition(position)
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

    private fun getCover(id: String, pos: Int) {
        Timber.d("getCover, id: $id, position: $pos")
        viewModel.getCover(id, pos)
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
}
