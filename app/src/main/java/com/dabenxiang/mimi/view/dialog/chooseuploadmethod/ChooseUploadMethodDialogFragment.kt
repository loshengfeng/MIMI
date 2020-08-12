package com.dabenxiang.mimi.view.dialog.chooseuploadmethod

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_choose_upload_method.*

class ChooseUploadMethodDialogFragment : BaseDialogFragment() {

    var onChooseUploadMethodDialogListener: OnChooseUploadMethodDialogListener? = null

    companion object {
        fun newInstance(listener: OnChooseUploadMethodDialogListener? = null): ChooseUploadMethodDialogFragment {
            val fragment = ChooseUploadMethodDialogFragment()
            fragment.onChooseUploadMethodDialogListener = listener
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_choose_upload_method
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txt_cancel.setOnClickListener { dismiss() }
        background.setOnClickListener { dismiss() }
    }

    override fun setupListeners() {
        super.setupListeners()

        View.OnClickListener { btnView ->
            when (btnView.id) {
                R.id.tv_video -> {
                    onChooseUploadMethodDialogListener?.onUploadVideo()
                }
                R.id.tv_camera -> {
                    onChooseUploadMethodDialogListener?.onUploadPic()
                }
                R.id.tv_article -> {
                    onChooseUploadMethodDialogListener?.onUploadArticle()
                }
            }
            dismiss()
        }.also {
            tv_video.setOnClickListener(it)
            tv_camera.setOnClickListener(it)
            tv_article.setOnClickListener(it)
        }
    }
}