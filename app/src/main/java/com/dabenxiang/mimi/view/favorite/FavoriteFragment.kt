package com.dabenxiang.mimi.view.favorite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.FavoriteItem
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class FavoriteFragment : BaseFragment<FavoriteViewModel>() {
    private val viewModel: FavoriteViewModel by viewModels()

    companion object {
        var lastPosition = 0
        const val TYPE_NORMAL = 0
        const val TYPE_ADULT = 1
    }

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                viewModel.setTopTabPosition(index)
            }
        }, false, isFavorite = true)
    }

    private val favoriteListener = object : AdapterEventListener<FavoriteItem> {
        override fun onItemClick(view: View, item: FavoriteItem) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_favorite }

    override fun fetchViewModel(): FavoriteViewModel? { return  viewModel }

    override fun setupObservers() {
        viewModel.tabLayoutPosition.observe(viewLifecycleOwner, Observer { position ->
            lastPosition = position

            tabAdapter.setLastSelectedIndex(lastPosition)

            when (position) {
                TYPE_NORMAL -> layout_adult.visibility = View.GONE
                TYPE_ADULT -> layout_adult.visibility = View.VISIBLE
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when(buttonView.id) {
                R.id.tv_clean -> GeneralUtils.showToast(requireContext(), "clean")
            }
        }.also {
            tv_clean.setOnClickListener(it)
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

//        when(hasData) {
//            true -> {
//                rv_content.visibility = View.VISIBLE
//                item_no_data.visibility = View.GONE
//            }
//            false -> {
//                rv_content.visibility = View.GONE
//                item_no_data.visibility = View.VISIBLE
//            }
//        }

//        activity?.also { activity ->
//            LinearLayoutManager(activity).also { layoutManager ->
//                layoutManager.orientation = LinearLayoutManager.VERTICAL
//                rv_content.layoutManager = layoutManager
//            }
//        }

//        rv_content.adapter = FavoriteAdapter(favoriteListener)
//        val proxyAdapter = rv_content.adapter as FavoriteAdapter
//        proxyAdapter.setDataSrc(proxyPayList)

    }

//    private fun showMenu(btnMenu: View) {
//        val menu = PopupMenu(context, btnMenu)
//        val inflater = menu.menuInflater
//        inflater.inflate(R.menu.menu_favorite, menu.menu)
//        menu.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.action_clean -> {
//                    GeneralUtils.showToast(context!!, "Clear")
//                    hasData = !hasData
//                    initSettings()
//                }
//            }
//            return@setOnMenuItemClickListener false
//        }
//        menu.show()
//    }
}