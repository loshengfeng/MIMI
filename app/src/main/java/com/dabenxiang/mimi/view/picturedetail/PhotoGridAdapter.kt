package com.dabenxiang.mimi.view.picturedetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.picturedetail.viewholder.PictureGridViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class PhotoGridAdapter(
    val context: Context,
    val images: ArrayList<ImageItem>,
    private val onPictureDetailListener: PictureDetailAdapter.OnPictureDetailListener,
    private val onItemClickListener: OnItemClickListener
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

        onPictureDetailListener.onGetAttachment(imageItem.id.toLongOrNull(), holder.picture, LoadImageType.THUMBNAIL)

        if (position == 5) {
            holder.mask.visibility = View.VISIBLE
            holder.imageCount.visibility = View.VISIBLE
            holder.imageCount.text =
                StringBuilder("+").append((images.size - 5).toString()).toString()
        }

        holder.cardView.setOnClickListener {
            onItemClickListener.onItemClick(position, images)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, imageItems: ArrayList<ImageItem>)
    }
}