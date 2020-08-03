package com.dabenxiang.mimi.view.post.viewer

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_post_viewer.*


class PostViewerFragment : BaseFragment() {

    companion object {
        const val VIEWER_DATA = "viewer_data"
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_viewer
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewerItem = arguments?.getSerializable(VIEWER_DATA) as ViewerItem

        if (viewerItem.attachmentId.isBlank()) {
            val uriP = Uri.parse(viewerItem.url)
            iv_cover.setImageURI(uriP)
        } else {
            LruCacheUtils.getLruCache(viewerItem.attachmentId)?.also { bitmap ->
                Glide.with(requireContext()).load(bitmap).into(iv_cover)
            }
        }

        ib_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
    }
}