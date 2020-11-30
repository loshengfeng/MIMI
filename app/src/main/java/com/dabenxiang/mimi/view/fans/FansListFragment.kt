package com.dabenxiang.mimi.view.fans

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.error.FansItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.adapter.FansListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_fans.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class FansListFragment : BaseFragment() {

    private val viewModel: FansListViewModel by viewModels()

    private val listener = object : FansListAdapter.EventListener {
        override fun onClickListener(item: FansItem, position: Int) {
            TODO("Not yet implemented")
        }
//        override fun onClickListener(item: FansItem, position: Int) {
//            val bundle = ChatContentFragment.createBundle(item)
//            navigateTo(
//                NavigateItem.Destination(
//                    R.id.action_chatHistoryFragment_to_chatContentFragment,
//                    bundle
//                )
//            )
//        }

        override fun onGetAttachment(id: Long?, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR_CS)
        }
    }

    private val adapter by lazy { FansListAdapter(listener) }

    companion object {
        const val NO_DATA = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun setupFirstTime() {
        viewModel.getFansList()
    }

    override fun initSettings() {
        super.initSettings()
        tv_title.setText(R.string.title_fans)
        list_fans.adapter = adapter
        layout_refresh.setColorSchemeColors(layout_refresh.context.getColor(R.color.color_red_1))
    }

//    private fun refreshUi(size: Int) {
//        list_fans.visibility = when (size) {
//            NO_DATA -> View.GONE
//            else -> View.VISIBLE
//        }
//
////        item_no_data.visibility = when (size) {
////            NO_DATA -> View.VISIBLE
////            else -> View.GONE
////        }
//    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_fans
    }

    override fun setupObservers() {
//        viewModel.chatHistory.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
//
//        viewModel.pagingResult.observe(viewLifecycleOwner, Observer {
//            when (it) {
//                is ApiResult.Loaded,
//                is ApiResult.Error -> {
//                    swipeRefreshLayout.isRefreshing = false
//                }
//            }
//        })
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
//            viewModel.getChatList()
        }
    }
}