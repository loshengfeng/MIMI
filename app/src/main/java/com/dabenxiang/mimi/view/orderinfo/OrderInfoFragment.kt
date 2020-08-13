package com.dabenxiang.mimi.view.orderinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderingPackageItem
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_text_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class OrderInfoFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: OrderingPackageItem?): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val viewModel: OrderInfoViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_toolbar_title.text = getString(R.string.order_detail_title)

        toolbar.setBackgroundColor(requireContext().getColor(R.color.color_gray_2))
        text_toolbar_title.setTextColor(requireContext().getColor(R.color.color_black_1))
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_black_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_info
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }

}