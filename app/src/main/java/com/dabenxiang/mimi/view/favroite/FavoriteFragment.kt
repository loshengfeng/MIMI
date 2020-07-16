package com.dabenxiang.mimi.view.favroite

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.adapter.FavoriteTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_post_favorite.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class FavoriteFragment : BaseFragment() {

    private val viewModel: FavoriteViewModel by viewModels()

    private val favoriteAdapter by lazy { FavoriteAdapter(listener) }

    companion object {
        const val NO_DATA = 0
        const val TAB_PRIMARY = 0
        const val TAB_SECONDARY = 1
        const val TYPE_NORMAL = 0
        const val TYPE_ADULT = 1
        private const val TYPE_MIMI = 0
        const val TYPE_SHORT_VIDEO = 1
        var lastPrimaryIndex = TYPE_NORMAL
        var lastSecondaryIndex = TYPE_MIMI
    }

    private val primaryAdapter by lazy {
        FavoriteTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTabPosition(TAB_PRIMARY, index)
                viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
            }
        }, true)
    }

    private val secondaryAdapter by lazy {
        FavoriteTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTabPosition(TAB_SECONDARY, index)
                viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
            }
        }, false)
    }

    override fun onResume() {
        super.onResume()
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_favorite
    }

    override fun setupObservers() {
        viewModel.playList.observe(viewLifecycleOwner, Observer { favoriteAdapter.submitList(it) })
        viewModel.postList.observe(viewLifecycleOwner, Observer { favoriteAdapter.submitList(it) })
        viewModel.dataCount.observe(viewLifecycleOwner, Observer { refreshUi(it) })


        viewModel.cleanResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Empty -> {
                    viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.likeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    refreshUI(it.result)
                    refreshLikeLeftIcon(it.result)
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    if (viewModel.viewFavoriteStatus[it.result.tag as Long] == LikeType.DISLIKE.value) {
                        viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
                        GeneralUtils.showToast(
                            requireContext(),
                            getString(R.string.favorite_delete_favorite)
                        )
                    }
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.reportResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_clean -> {
                    CleanDialogFragment.newInstance(onCleanDialogListener).also {
                        it.show(
                            requireActivity().supportFragmentManager,
                            CleanDialogFragment::class.java.simpleName
                        )
                    }
                }
            }
        }.also {
            tv_clean.setOnClickListener(it)
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
        }
    }

    override fun initSettings() {
        tv_back.visibility = View.GONE
        tv_title.text = getString(R.string.favorite_title)
        tv_clean.visibility = View.VISIBLE

        rv_primary.adapter = primaryAdapter

        val primaryList = listOf(
            getString(R.string.favorite_normal),
            getString(R.string.favorite_adult)
        )

        primaryAdapter.submitList(primaryList, lastPrimaryIndex)

        rv_secondary.adapter = secondaryAdapter

        val secondaryList = listOf(
            getString(R.string.favorite_tab_mimi),
            getString(R.string.favorite_tab_short)
        )

        secondaryAdapter.submitList(secondaryList, lastSecondaryIndex)

        rv_content.adapter = favoriteAdapter

        viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
    }

    private fun refreshUi(size: Int) {
        rv_content.visibility = when (size) {
            NO_DATA -> View.GONE
            else -> View.VISIBLE
        }

        item_no_data.visibility = when (size) {
            NO_DATA -> View.VISIBLE
            else -> View.GONE
        }

        when (lastPrimaryIndex) {
            TYPE_NORMAL -> layout_adult.visibility = View.GONE
            TYPE_ADULT -> layout_adult.visibility = View.VISIBLE
        }
    }

    private fun setTabPosition(type: Int, index: Int) {
        when (type) {
            TAB_PRIMARY -> {
                lastPrimaryIndex = index
                primaryAdapter.setLastSelectedIndex(lastPrimaryIndex)
            }
            TAB_SECONDARY -> {
                lastSecondaryIndex = index
                secondaryAdapter.setLastSelectedIndex(lastSecondaryIndex)
            }
        }
    }

    private val listener = object : FavoriteAdapter.EventListener {
        override fun onAvatarDownload(view: ImageView, id: String) {
            viewModel.getAttachment(view, id)
        }

        override fun onVideoClick(item: Any) {
            when (item) {
                is PlayItem -> {
                    val playerData = PlayerData(item.videoId ?: 0, item.isAdult ?: false)
                    val intent = Intent(requireContext(), PlayerActivity::class.java)
                    intent.putExtras(PlayerActivity.createBundle(playerData))
                    startActivity(intent)
                }
                is PostFavoriteItem -> {
                    // todo: 進入VAI4.1.2.1_短視頻詳細頁，wainting for 短視頻詳細頁...
                }
            }
        }


        override fun onFunctionClick(type: FunctionType, view: View, item: Any) {
            val textView = view as TextView
            when (type) {
                FunctionType.LIKE -> {
                    when (item) {
                        is PlayItem -> {
                            item.videoId?.let {
                                viewModel.viewStatus[it] = viewModel.viewStatus[it]
                                    ?: if (item.like == true) LikeType.LIKE else LikeType.DISLIKE
                                viewModel.modifyLike(textView, it)
                            }
                        }
                        is PostFavoriteItem -> {
                            item.id?.let {
                                viewModel.viewStatus[it] =
                                    viewModel.viewStatus[it] ?: LikeType.DISLIKE
                                viewModel.modifyLike(textView, it)
                            }
                        }
                    }
                }

                FunctionType.FAVORITE -> {
                    // 點擊後加入收藏,
                    when (item) {
                        is PlayItem -> {
                            item.videoId?.let {
                                viewModel.viewFavoriteStatus[it] =
                                    viewModel.viewFavoriteStatus[it]
                                        ?: if (item.favorite == true) LikeType.LIKE.value else LikeType.DISLIKE.value
                                viewModel.modifyFavorite(textView, it)
                            }
                        }
                        is PostFavoriteItem -> {
                            item.id?.let {
                                viewModel.viewFavoriteStatus[it] =
                                    viewModel.viewFavoriteStatus[it] ?: LikeType.DISLIKE.value
                                viewModel.modifyFavorite(textView, it)
                            }
                        }
                    }
                }

                FunctionType.SHARE -> {
                    /* 點擊後複製網址 */
                    when (item) {
                        is PlayItem -> {
                            if (item.tags == null || item.tags.first()
                                    .isEmpty() || item.videoId == null
                            ) {
                                GeneralUtils.showToast(requireContext(), "copy url error")
                            } else {
                                GeneralUtils.copyToClipboard(
                                    requireContext(),
                                    viewModel.getShareUrl(item.tags[0], item.videoId, item.episode)
                                )
                                GeneralUtils.showToast(requireContext(), "already copy url")
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun refreshUI(view: TextView) {
        var count = view.text.toString().toInt()
        when (viewModel.viewStatus[view.tag as Long]) {
            LikeType.LIKE -> {
                count++
            }
            LikeType.DISLIKE -> {
                count--
            }
        }
        view.text = count.toString()
    }

    private fun refreshLikeLeftIcon(view: TextView) {
        val res = when (viewModel.viewStatus[view.tag as Long]) {
            LikeType.LIKE -> R.drawable.ico_nice_s
            LikeType.DISLIKE -> R.drawable.ico_nice_gray
            else -> R.drawable.ico_nice_gray
        }

        view.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
    }

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            viewModel.deleteFavorite()
        }
    }
}