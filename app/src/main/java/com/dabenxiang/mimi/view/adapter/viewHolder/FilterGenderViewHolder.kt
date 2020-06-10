package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R

class FilterGenderViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    val radioFilter = itemView.findViewById(R.id.radioFilter) as RadioButton
}
