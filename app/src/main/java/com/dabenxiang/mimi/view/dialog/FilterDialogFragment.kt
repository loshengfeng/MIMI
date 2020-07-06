package com.dabenxiang.mimi.view.dialog

import android.os.Bundle
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.FilterGenderAdapter
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.listener.OnDialogListener
import kotlinx.android.synthetic.main.fragment_dialog_filter.*
import java.io.Serializable

class FilterDialogFragment : BaseDialogFragment() {
    companion object {
        private const val KEY_CONTENT = "KEY_CONTENT"
        fun newInstance(content: Content): FilterDialogFragment {
            val fragment =
                FilterDialogFragment()
            val args = Bundle()
            args.putSerializable(KEY_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }

    data class Content(
        @StringRes val title: Int,
        @ArrayRes val textArray: Int,
        @ArrayRes val valueArray: Int,
        val dialogListener: OnDialogListener? = null,
        val selectedValue: Int = 0
    ) : Serializable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_filter
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    private fun initSettings() {
        val content = arguments?.getSerializable(KEY_CONTENT) as Content

        tv_title.text = getString(content.title)

        recyclerFilter.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val textArray = resources.getStringArray(content.textArray)
        val valueArray = resources.getIntArray(content.valueArray)

        recyclerFilter.adapter = FilterGenderAdapter(
            textArray,
            valueArray,
            content.selectedValue,
            object : OnDialogListener {
                override fun onItemSelected(value: Int, text: String) {
                    content.dialogListener?.onItemSelected(value, text)
                    dismiss()
                }
            })
    }

    override fun setupListeners() {
        super.setupListeners()
        View.OnClickListener { dismiss() }
            .also { layout_root.setOnClickListener(it) }
    }
}