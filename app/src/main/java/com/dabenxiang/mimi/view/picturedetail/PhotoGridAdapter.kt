package com.dabenxiang.mimi.view.picturedetail

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.view.picturedetail.viewholder.PictureGridViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils

class PhotoGridAdapter(
    val context: Context,
    val images: ArrayList<ImageItem>
) : RecyclerView.Adapter<PictureGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureGridViewHolder {
        val mView = LayoutInflater.from(context)
            .inflate(R.layout.item_photo_grid, parent, false)
        return PictureGridViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: PictureGridViewHolder, position: Int) {
        val imageItem = images[position]
        val bitmap = LruCacheUtils.getLruCache(imageItem.id)

        if (!TextUtils.isEmpty(imageItem.url)) {
            Glide.with(context)
                .load(imageItem.url)
                .into(holder.picture)
        } else {
            Glide.with(context)
                .load(bitmap)
                .into(holder.picture)
        }
    }
}