package com.dabenxiang.mimi.view.dialog.comment

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_comment.*

class CommentDialogFragment: BaseDialogFragment() {

    private var data: MemberPostItem? = null

    companion object {
        private const val KEY_DATA = "KEY_DATA"

        fun newInstance(
            item: MemberPostItem
        ): CommentDialogFragment {
            val fragment = CommentDialogFragment()
            val args = Bundle()
            args.putSerializable(KEY_DATA, item)
            fragment.arguments = args
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_comment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_DATA) as MemberPostItem).also {
            data = it
            tv_comment_count.text = String.format(requireContext().getString(R.string.clip_comment_count), it.commentCount)
        }
    }
}