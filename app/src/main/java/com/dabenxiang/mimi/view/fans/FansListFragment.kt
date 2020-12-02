package com.dabenxiang.mimi.view.fans

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.error.FansItem
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.adapter.FansListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import kotlinx.android.synthetic.main.fragment_fans.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber

class FansListFragment : BaseFragment() {

    private val viewModel: FansListViewModel by viewModels()
    private val adapter by lazy { FansListAdapter(fanListener) }
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

    override val bottomNavigationVisibility: Int
        get() = View.GONE

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
        viewModel.fansCount.observe(this, Observer {
            tv_title_count.text = String.format(
                getString(R.string.total_count_fans),
                it
            )

            if (it == 0.toLong()) {
                img_page_empty.visibility = View.VISIBLE
                text_page_empty.visibility = View.VISIBLE
                list_fans.visibility = View.GONE
            } else {
                img_page_empty.visibility = View.GONE
                text_page_empty.visibility = View.GONE
                list_fans.visibility = View.VISIBLE
            }
        })

        viewModel.followPostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> adapter?.notifyItemChanged(it.result)
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
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

    private val fanListener = object : FansListAdapter.FanListener {
        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                userId, name,
                isAdult = true,
                isAdultTheme = false
            )
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_fansListFragment_to_navigation_my_post,
                    bundle
                )
            )
        }

        override fun onGetAvatarAttachment(id: Long?, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR)
        }

        override fun onFollow(
            item: com.dabenxiang.mimi.model.api.vo.FansItem,
            position: Int,
            isFollow: Boolean
        ) {
            viewModel.followPost(item, position, isFollow)
        }
    }
}