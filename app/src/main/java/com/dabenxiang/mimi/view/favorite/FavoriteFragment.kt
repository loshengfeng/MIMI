package com.dabenxiang.mimi.view.favorite

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.item_favorite_no_data.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class FavoriteFragment  : BaseFragment() {
    private val viewModel by viewModel<FavoriteViewModel>()
    private var hasData = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_favorite
    }

    override fun setupObservers() {
        Timber.d("${FavoriteFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${FavoriteFragment::class.java.simpleName}_setupListeners")
        View.OnClickListener { buttonView ->
            when(buttonView.id) {
                R.id.tv_more -> showMenu(buttonView)
            }
        }.also {
            tv_more.setOnClickListener(it)
        }
    }

    private fun initSettings() {
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
        rv_content.adapter = FavoriteAdapter()
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