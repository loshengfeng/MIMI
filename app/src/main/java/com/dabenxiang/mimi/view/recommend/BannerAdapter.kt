package com.dabenxiang.mimi.view.recommend

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.CategoryBanner
import kotlinx.android.synthetic.main.item_banner.view.*

class BannerAdapter(
    private val context: Context,
    private val banners: List<CategoryBanner>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(mView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as BannerViewHolder
        val banner = banners[position]

//        Glide.with(context).load(banner).into(holder.bannerImg)
    }

    override fun getItemCount(): Int {
        return banners.size
    }

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bannerImg: ImageView = itemView.iv_banner
    }

}