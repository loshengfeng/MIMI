package com.dabenxiang.mimi.view.dialog.preview

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import kotlinx.android.synthetic.main.fragment_dialog_image_preview.*

class ImagePreviewDialogFragment : BaseDialogFragment() {

    private var onCleanDialogListener: OnCleanDialogListener? = null
    private var imageArray: ByteArray? = null

    companion object {

        fun newInstance(
                imageArray: ByteArray?,
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
            Glide.with(App.self)
                    .asBitmap()
                    .load(imageArray)
                    .into(ima_bg)
        } else {
            txt_file_invalid.visibility = View.VISIBLE
            img_logo.visibility = View.VISIBLE
            ima_bg.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }
}