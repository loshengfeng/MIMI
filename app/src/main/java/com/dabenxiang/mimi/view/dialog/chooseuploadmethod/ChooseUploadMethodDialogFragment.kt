package com.dabenxiang.mimi.view.dialog.chooseuploadmethod

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_choose_picker.btn_article
import kotlinx.android.synthetic.main.fragment_dialog_choose_picker.btn_camera
import kotlinx.android.synthetic.main.fragment_dialog_choose_picker.layout_root
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
        txt_cancel.setOnClickListener {
            dismiss()
        }
    }

    override fun setupListeners() {
        super.setupListeners()

        View.OnClickListener { btnView ->
            when (btnView.id) {
                R.id.btn_camera -> {
                    onChooseUploadMethodDialogListener?.onUploadPic()
                }
                R.id.btn_article -> {
                    onChooseUploadMethodDialogListener?.onUploadArticle()
                }
            }

            dismiss()
        }.also {
            layout_root.setOnClickListener(it)
            btn_camera.setOnClickListener(it)
            btn_article.setOnClickListener(it)
        }
    }
}