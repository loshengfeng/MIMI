package com.dabenxiang.mimi.view.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import timber.log.Timber

class FavoriteAdapter : RecyclerView.Adapter<FavoriteViewHolder>() {
    var no = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun getItemCount(): Int {
        Timber.d("${FavoriteAdapter::class.java.simpleName}_getItemCount")
        return 10
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        Timber.d("${FavoriteAdapter::class.java.simpleName}_onBindViewHolder")
        holder.tvNo.text = no.toString()
//        holder.ivPhoto.xxxxxxxxxxxxx
        holder.tvInfo.text = "01:54:10"
        holder.tvTitle.text = "冰题标题标题标题标题标题标题标题标…"
        holder.tvContent.text = "副标副标副标副标…"
//        holder.ivFavorite.xxxxxxxxxxxxxxx
        no++
    }
}