package com.dabenxiang.mimi.view.dialog.preview

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.chatcontent.ChatContentViewModel
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import kotlinx.android.synthetic.main.fragment_dialog_image_preview.*

class ImagePreviewDialogFragment : BaseDialogFragment() {

    private var onCleanDialogListener: OnCleanDialogListener? = null
    private var imageArray: ChatContentItem? = null
    private val viewModel: ImagePreviewViewModel by viewModels()

    companion object {

        fun newInstance(
                imageArray: ChatContentItem?,
                listener: OnCleanDialogListener? = null
        ): ImagePreviewDialogFragment {
            val fragment = ImagePreviewDialogFragment()
            fragment.onCleanDialogListener = listener
            fragment.imageArray = imageArray
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_image_preview
    }

    override fun setupListeners() {
        super.setupListeners()
        img_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (imageArray != null) {
            txt_file_invalid.visibility = View.INVISIBLE
            img_logo.visibility = View.INVISIBLE
            if (!TextUtils.isEmpty(imageArray?.cacheImagePath)) {
                imageArray?.cacheImagePath?.let { viewModel.loadImage(0, ima_bg, LoadImageType.PICTURE_FULL, filePath = it) }
            } else {
                viewModel.loadImage(
                        imageArray?.payload?.content?.toLongOrNull(), ima_bg, LoadImageType.PICTURE_FULL)
            }
        } else {
            txt_file_invalid.visibility = View.VISIBLE
            img_logo.visibility = View.VISIBLE
            ima_bg.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }
}