package com.dabenxiang.mimi.view.fans

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.vo.error.FansItem
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.view.adapter.FansListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_fans.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber

class FansListFragment : BaseFragment() {

    private val viewModel: FansListViewModel by viewModels()
    private val adapter by lazy { FansListAdapter(listener) }
    private val listener = object : BaseItemListener {
        override fun onItemClick(item: Any, type: ClickType) {
            Timber.i("FansListFragment onItemClick $item")
            if (item is FansItem) {
                if (type == ClickType.TYPE_AUTHOR) {
                    //TODO go to author item page
                }
            }
        }
    }

    companion object {
        const val NO_DATA = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun setupFirstTime() {}

    override fun initSettings() {
        super.initSettings()
        tv_title.setText(R.string.title_fans)
        list_fans.adapter = adapter
        layout_refresh.setColorSchemeColors(layout_refresh.context.getColor(R.color.color_red_1))
        viewModel.getData(adapter)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_fans
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
            }
        }.also {
            tv_back.setOnClickListener(it)
        }

        layout_refresh.setOnRefreshListener {
        }
    }
}