package com.dabenxiang.mimi.view.picturedetail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.fullpicture.FullPictureFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_picture_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import timber.log.Timber

class PictureDetailFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val viewModel: PictureDetailViewModel by viewModels()

    private var adapter: PictureDetailAdapter? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val memberPostItem = arguments?.getSerializable(KEY_DATA) as MemberPostItem

        text_toolbar_title.text = getString(R.string.picture_detail_title)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_white_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter = PictureDetailAdapter(
            requireContext(),
            memberPostItem,
            onPictureDetailListener,
            onItemClickListener
        )
        recycler_picture_detail.layoutManager = LinearLayoutManager(context)
        recycler_picture_detail.adapter = adapter
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_picture_detail
    }

    override fun setupObservers() {
        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val item = it.result
                    LruCacheUtils.putLruCache(item.id!!, item.bitmap!!)
                    adapter?.updatePhotoGridItem(item.position!!)
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.followPostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> adapter?.notifyItemChanged(it.result)
                is Error -> Timber.e(it.throwable)
            }
        })
    }

    override fun setupListeners() {

    }

    private val onPictureDetailListener = object : PictureDetailAdapter.OnPictureDetailListener {
        override fun onGetAttachment(id: String, position: Int) {
            viewModel.getAttachment(id, position)
        }

        override fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            viewModel.followPost(item, position, isFollow)
        }
    }

    private val onItemClickListener = object : PhotoGridAdapter.OnItemClickListener {
        override fun onItemClick(position: Int, imageItems: ArrayList<ImageItem>) {
            val bundle = FullPictureFragment.createBundle(position, imageItems)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_pictureDetailFragment_to_pictureFragment,
                    bundle
                )
            )
        }
    }

}