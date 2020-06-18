package com.dabenxiang.mimi.view.postfavorite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.PlayListItem
import com.dabenxiang.mimi.view.adapter.PostFavoriteAdapter
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_post_favorite.*
import kotlinx.android.synthetic.main.fragment_post_favorite.item_no_data
import kotlinx.android.synthetic.main.fragment_post_favorite.layout_refresh
import kotlinx.android.synthetic.main.fragment_post_favorite.rv_content
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber

class PostFavoriteFragment : BaseFragment<PostFavoriteViewModel>() {
    private val viewModel: PostFavoriteViewModel by viewModels()

    private val postFavoriteAdapter by lazy { PostFavoriteAdapter(listener) }

    companion object {
        var lastPosition = 0
        const val NO_DATA = 0
        const val TYPE_NORMAL = 0
        const val TYPE_ADULT = 1
    }

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTopTabPosition(index)
            }
        }, false, isFavorite = true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_post_favorite }

    override fun fetchViewModel(): PostFavoriteViewModel? { return viewModel }

    override fun setupObservers() {
        viewModel.favoriteList.observe(viewLifecycleOwner, Observer {
            Timber.d("it: $it")
            Timber.d("it: ${it.size}")
            postFavoriteAdapter.submitList(it)
        })

        viewModel.dataCount.observe(viewLifecycleOwner, Observer {
            refreshUi(it)
        })
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
            viewModel.initData()
        }
    }

    override fun initSettings() {
        tv_back.visibility = View.GONE
        tv_title.text = getString(R.string.favorite_title)
        tv_clean.visibility = View.GONE

        recyclerview_tab.adapter = tabAdapter

        val tabList = listOf(
            getString(R.string.favorite_normal),
            getString(R.string.favorite_adult)
        )

        tabAdapter.submitList(tabList, lastPosition)

        rv_content.adapter = postFavoriteAdapter

        viewModel.initData()
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
    }

    private fun setTopTabPosition(position: Int) {
        lastPosition = position

        tabAdapter.setLastSelectedIndex(lastPosition)

        when (position) {
            TYPE_NORMAL -> layout_adult.visibility = View.GONE
            TYPE_ADULT -> layout_adult.visibility = View.VISIBLE
        }
    }

    private val listener = object : PostFavoriteAdapter.EventListener {
        // todo: call api...
        override fun onVideoClick(view: View, item: PlayListItem) {
            Timber.d("onVideoClick")
        }

        override fun onLikeClick(view: View, item: PlayListItem) {
            Timber.d("onLikeClick")
        }

        override fun onFavoriteClick(view: View, item: PlayListItem) {
            Timber.d("onFavoriteClick")
        }

        override fun onMsgClick(view: View, item: PlayListItem) {
            Timber.d("onMsgClick")
        }

        override fun onShareClick(view: View, item: PlayListItem) {
            Timber.d("onShareClick")
        }

        override fun onMoreClick(view: View, item: PlayListItem) {
            Timber.d("onMoreClick")
        }

    }

}