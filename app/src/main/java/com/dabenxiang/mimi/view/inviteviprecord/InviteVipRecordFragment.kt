package com.dabenxiang.mimi.view.inviteviprecord

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_text_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class InviteVipRecordFragment : BaseFragment() {
    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text_toolbar_title.text = getString(R.string.invite_vip_record_title)

        toolbar.setBackgroundColor(requireContext().getColor(R.color.color_gray_2))
        text_toolbar_title.setTextColor(requireContext().getColor(R.color.color_black_1))
        toolbarContainer.toolbar.navigationIcon = ContextCompat.getDrawable(
            requireContext(), R.drawable.btn_back_black_n
        )

        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_invite_vip_record
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }
}