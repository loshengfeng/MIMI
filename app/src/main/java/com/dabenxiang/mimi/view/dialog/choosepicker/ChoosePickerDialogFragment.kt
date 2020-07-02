package com.dabenxiang.mimi.view.dialog.choosepicker

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_choose_picker.*

class ChoosePickerDialogFragment : BaseDialogFragment() {

    var onChoosePickerDialogListener: OnChoosePickerDialogListener? = null

    companion object {
        fun newInstance(listener: OnChoosePickerDialogListener? = null): ChoosePickerDialogFragment {
            val fragment = ChoosePickerDialogFragment()
            fragment.onChoosePickerDialogListener = listener
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_choose_picker
    }

    override fun setupListeners() {
        super.setupListeners()
        View.OnClickListener { btnView ->
            dismiss()
            when (btnView.id) {
                R.id.btn_camera -> onChoosePickerDialogListener?.onPickFromCamera()
                R.id.btn_album -> onChoosePickerDialogListener?.onPickFromAlbum()
            }
        }.also {
            layout_root.setOnClickListener(it)
            btn_close.setOnClickListener(it)
            btn_camera.setOnClickListener(it)
            btn_album.setOnClickListener(it)
        }
    }
}