package com.dabenxiang.mimi.view.picturedetail

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.view.picturedetail.viewholder.PictureGridViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils


class PhotoGridAdapter(
    val context: Context,
    val images: ArrayList<ImageItem>,
    private val onPictureDetailListener: PictureDetailAdapter.OnPictureDetailListener
) : RecyclerView.Adapter<PictureGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureGridViewHolder {
        val mView = LayoutInflater.from(context)
            .inflate(R.layout.item_photo_grid, parent, false)
        return PictureGridViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return if (images.size > 6) 6
        else images.size
    }

    override fun onBindViewHolder(holder: PictureGridViewHolder, position: Int) {
        val imageItem = images[position]
        val bitmap = LruCacheUtils.getLruCache(imageItem.id)

        if (images.size > 3) {
            val imgParams = holder.picture.layoutParams
            imgParams.width = GeneralUtils.dpToPx(context, 102)
            imgParams.height = GeneralUtils.dpToPx(context, 102)
            holder.picture.layoutParams = imgParams

            val cardParams = holder.cardView.layoutParams
            cardParams.width = GeneralUtils.dpToPx(context, 102)
            cardParams.height = GeneralUtils.dpToPx(context, 102)
            holder.cardView.layoutParams = cardParams
        }

        if (!TextUtils.isEmpty(imageItem.url)) {
            Glide.with(context)
                .load(imageItem.url)
                .into(holder.picture)
        } else {
            if (LruCacheUtils.getLruCache(imageItem.id) == null) {
                onPictureDetailListener.onGetAttachment(imageItem.id, position)
            } else {
                Glide.with(context)
                    .load(bitmap)
                    .into(holder.picture)
            }
        }

        if (position == 5) {
            holder.mask.visibility = View.VISIBLE
            holder.imageCount.visibility = View.VISIBLE
            holder.imageCount.text =
                StringBuilder("+").append((images.size - 5).toString()).toString()
        }
    }
}