package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdClickListener
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.item_ad.view.*
import timber.log.Timber

class AdHolder(itemView: View, private val adClickListener: AdClickListener? = null) : BaseViewHolder(itemView) {
    val adImg: ImageView = itemView.iv_ad

    fun onBind(adItem: AdItem) {
        val options = RequestOptions()
            .priority(Priority.NORMAL)
            .placeholder(R.drawable.img_ad_df)
            .error(R.drawable.img_ad)
        Glide.with(adImg.context)
            .load(adItem.href)
            .apply(options)
            .into(adImg)

        adImg.setOnClickListener {
            adClickListener?.onAdClick(adItem)
        }
    }
}