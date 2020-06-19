package com.dabenxiang.mimi.view.favroite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.PlayListItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.adapter.FavoriteTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_post_favorite.*
import kotlinx.android.synthetic.main.fragment_post_favorite.item_no_data
import kotlinx.android.synthetic.main.fragment_post_favorite.layout_refresh
import kotlinx.android.synthetic.main.fragment_post_favorite.rv_content
import kotlinx.android.synthetic.main.item_setting_bar.*

class FavoriteFragment : BaseFragment<FavoriteViewModel>() {
    private val viewModel: FavoriteViewModel by viewModels()

    private val favoriteAdapter by lazy { FavoriteAdapter(listener) }

    companion object {
        const val NO_DATA = 0
        const val TAB_PRIMARY = 0
        const val TAB_SECONDARY = 1
        const val TYPE_NORMAL = 0
        const val TYPE_ADULT = 1
        const val TYPE_MIMI = 0
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

    override fun getLayoutId(): Int { return R.layout.fragment_post_favorite }

    override fun fetchViewModel(): FavoriteViewModel? { return viewModel }

    override fun setupObservers() {
        viewModel.playList.observe(viewLifecycleOwner, Observer { favoriteAdapter.submitList(it) })
        viewModel.postList.observe(viewLifecycleOwner, Observer { favoriteAdapter.submitList(it) })
        viewModel.dataCount.observe(viewLifecycleOwner, Observer { refreshUi(it) })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                // todo: clean single tab or all tabs?
                R.id.tv_clean -> GeneralUtils.showToast(requireContext(), "clean")
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
        tv_clean.visibility = View.GONE

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
            TYPE_MIMI, TYPE_SHORT_VIDEO -> layout_adult.visibility = View.VISIBLE
        }
    }

    private fun setTabPosition(type: Int, index: Int) {
        when(type) {
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
        override fun onItemClick(type: FavoriteAdapter.FunctionType, iew: View, item: Any) {
            when(type) {
                FavoriteAdapter.FunctionType.Video, FavoriteAdapter.FunctionType.Msg -> {
                    when (item) {
                        is PlayListItem -> {
                            val playerData = PlayerData(item.videoId ?: 0, item.isAdult ?: false)
                            val intent = Intent(requireContext(), PlayerActivity::class.java)
                            intent.putExtras(PlayerActivity.createBundle(playerData))
                            startActivity(intent)
                        }
                        is PostFavoriteItem -> { /*todo:...*/}
                    }
                }
                FavoriteAdapter.FunctionType.Like -> {
                    /*點擊後按讚次數+1，再次點擊則-1*/
                    when (item) {
                        is PlayListItem -> {}
                        is PostFavoriteItem -> {}
                    }
                }
                FavoriteAdapter.FunctionType.Favorite -> {
                    /* 按鈕點擊後加入收藏*/
                    when (item) {
                        is PlayListItem -> {}
                        is PostFavoriteItem -> {}
                    }
                }
                FavoriteAdapter.FunctionType.Share -> {
                    /* 點擊後複製網址 */
                    val url = when (item) {
                        is PlayListItem -> {
                            // todo: API hsn no url...
//                            item.url
                            "url"
                        }
                        else -> { "url" }
                    }
                    val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(url, url)
                    clipboard.primaryClip = clip
                    GeneralUtils.showToast(requireContext(), "already copy url")
                }
                FavoriteAdapter.FunctionType.More -> {
                    // 點擊後popup視窗，popup(2)問題回報視窗
                    // 若已經檢舉過則Disable
                    when (item) {
                        is PlayListItem -> {}
                        is PostFavoriteItem -> {}
                    }
                }
            }
        }
    }
}