package com.dabenxiang.mimi.view.favorite

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.FavoriteItem
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.item_favorite_no_data.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class FavoriteFragment : BaseFragment<FavoriteViewModel>() {
    private val viewModel by viewModel<FavoriteViewModel>()
    private var hasData = true

    private val favoriteListener = object : AdapterEventListener<FavoriteItem> {
        override fun onItemClick(view: View, item: FavoriteItem) {
            Timber.d("${FavoriteFragment::class.java.simpleName}_onlinePayListener_onItemClick_item: $item")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_favorite }

    override fun fetchViewModel(): FavoriteViewModel? { return  viewModel }

    override fun setupObservers() {}

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when(buttonView.id) {
                R.id.tv_more -> showMenu(buttonView)
            }
        }.also {
            tv_more.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        when(hasData) {
            true -> {
                rv_content.visibility = View.VISIBLE
                item_no_data.visibility = View.GONE
            }
            false -> {
                rv_content.visibility = View.GONE
                item_no_data.visibility = View.VISIBLE
            }
        }

        // todo: for testing
        tv_text.text = "提示字提示字"

        activity?.also { activity ->
            LinearLayoutManager(activity).also { layoutManager ->
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                rv_content.layoutManager = layoutManager
            }
        }

        val proxyPayList = mutableListOf<FavoriteItem>(
            FavoriteItem("1", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("2", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("3", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("4", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("5", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("6", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("7", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("8", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("9", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…"),
            FavoriteItem("10", "Photo", "01:54:10", "冰题标题标题标题标题标题标题标题标…", "副标副标副标副标…")
        )

        rv_content.adapter = FavoriteAdapter(favoriteListener)
        val proxyAdapter = rv_content.adapter as FavoriteAdapter
        proxyAdapter.setDataSrc(proxyPayList)

    }

    private fun showMenu(btnMenu: View) {
        val menu = PopupMenu(context, btnMenu)
        val inflater = menu.menuInflater
        inflater.inflate(R.menu.menu_favorite, menu.menu)
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clean -> {
                    GeneralUtils.showToast(context!!, "Clear")
                    hasData = !hasData
                    initSettings()
                }
            }
            return@setOnMenuItemClickListener false
        }
        menu.show()
    }
}