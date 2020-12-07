package com.dabenxiang.mimi.view.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.CategoryBanner
import kotlinx.android.synthetic.main.item_banner.view.*
import org.json.JSONObject

class BannerAdapter(
    private val banners: List<CategoryBanner>,
    private val bannerFuncItem: BannerFuncItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val KEY_APP = "app"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(mView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as BannerViewHolder

        val banner = banners[position]
        holder.bannerImage.setOnClickListener { bannerFuncItem.onItemClick(banner) }

        val bannerId = JSONObject(banner.content).optString(KEY_APP).toLong()
        bannerFuncItem.getBitmap(bannerId, holder.bannerImage)
    }

    override fun getItemCount(): Int {
        return banners.size
    }

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bannerImage: ImageView = itemView.iv_banner
    }
}