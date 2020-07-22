package com.dabenxiang.mimi.view.fullpicture

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_full_picture.*
import timber.log.Timber

class FullPictureFragment : BaseFragment() {

    companion object {
        private const val KEY_POSITION = "position"
        const val KEY_IMAGE = "image"

        fun createBundle(position: Int, imageItems: ArrayList<ImageItem>): Bundle {
            return Bundle().also {
                it.putInt(KEY_POSITION, position)
                it.putSerializable(KEY_IMAGE, imageItems)
            }
        }
    }

    private val viewModel: FullPictureViewModel by viewModels()

    private var adapter: FullPictureAdapter? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val position = arguments?.getInt(KEY_POSITION) ?: 0
        val imageItems = arguments?.getSerializable(KEY_IMAGE) as ArrayList<ImageItem>

        adapter = FullPictureAdapter(requireContext(), imageItems, onFullPictureListener)
        recycler_picture.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        recycler_picture.adapter = adapter
        recycler_picture.scrollToPosition(position)
        PagerSnapHelper().attachToRecyclerView(recycler_picture)

        recycler_picture.setOnScrollChangeListener { _, _, _, _, _ ->
            val currentPosition =
                (recycler_picture.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            tv_picture_count.text = "${currentPosition + 1}/${imageItems.size}"
        }

        iv_delete.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_full_picture
    }

    override fun setupObservers() {
        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val item = it.result
                    LruCacheUtils.putLruCache(item.id!!, item.bitmap!!)
                    adapter?.notifyItemChanged(item.position!!)
                }
                is Error -> Timber.e(it.throwable)
            }
        })
    }

    override fun setupListeners() {

    }

    override fun statusBarVisibility() {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private val onFullPictureListener = object : FullPictureAdapter.OnFullPictureListener {
        override fun onGetAttachment(id: String, position: Int) {
            viewModel.getAttachment(id, position)
        }
    }
}