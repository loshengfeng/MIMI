package com.dabenxiang.mimi.view.club.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class AdHeaderAdapter(
    val context: Context,
    var adItem: AdItem = AdItem()
) : RecyclerView.Adapter<AdHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ad, parent, false)
        return AdHolder(view)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        val options = RequestOptions()
            .priority(Priority.NORMAL)
            .placeholder(R.drawable.img_ad)
            .error(R.drawable.img_ad)
        Glide.with(context)
            .load(adItem?.href)
            .apply(options)
            .into(holder.adImg)
        holder.adImg.setOnClickListener {
            GeneralUtils.openWebView(context, adItem?.target )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_AD
    }
}