package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.viewHolder.FilterGenderViewHolder
import com.dabenxiang.mimi.view.listener.OnDialogListener

class FilterGenderAdapter(private val textArray: Array<String>,
                          private val valueArray: IntArray,
                          private var selectedValue: Int,
                          private var onFilterDialogListener: OnDialogListener? = null) : RecyclerView.Adapter<FilterGenderViewHolder>() {

    private var lastSelectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterGenderViewHolder {
        return FilterGenderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_gender, parent, false))
    }

    override fun getItemCount(): Int { return textArray.size }

    override fun onBindViewHolder(holder: FilterGenderViewHolder, position: Int) {
        holder.radioFilter.setOnCheckedChangeListener(null)
        val text = textArray[position]
        val value = valueArray[position]
        holder.radioFilter.text = text
        holder.radioFilter.isChecked = value == selectedValue
        if (holder.radioFilter.isChecked) { lastSelectedPosition = position }

        holder.radioFilter.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                notifyItemChanged(lastSelectedPosition)
                onFilterDialogListener?.onItemSelected(valueArray[position], textArray[position])
            }
        }
    }
}