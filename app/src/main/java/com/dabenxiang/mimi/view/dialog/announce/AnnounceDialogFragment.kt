package com.dabenxiang.mimi.view.dialog.announce

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_announcement.*
import org.koin.android.ext.android.inject

class AnnounceDialogFragment : BaseDialogFragment() {

    private val viewModel: AnnounceDialogViewModel by viewModels()

    companion object {
        fun newInstance(
        ): AnnounceDialogFragment {
            return AnnounceDialogFragment()
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_announcement
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        web.loadUrl(viewModel.getUrl())
        ib_close.setOnClickListener { dismiss() }
    }
}