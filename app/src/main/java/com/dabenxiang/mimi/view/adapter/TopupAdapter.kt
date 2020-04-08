package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.topup.TopupViewHolder
import timber.log.Timber

class TopupAdapter : RecyclerView.Adapter<TopupViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopupViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_topup, parent, false)
        return TopupViewHolder(view)
    }

    override fun getItemCount(): Int {
        Timber.d("${TopupAdapter::class.java.simpleName}_getItemCount")
        return 4
    }

    override fun onBindViewHolder(holder: TopupViewHolder, position: Int) {
        Timber.d("${TopupAdapter::class.java.simpleName}_onBindViewHolder")
        holder.ivCheck.visibility = View.VISIBLE
        holder.tvToken.text = "300"
        holder.tvOriginalPrice.text = "¥ 50"
        holder.tvPrice.text = "¥ 50.00"
    }

}