package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.getLruCache
import kotlinx.android.synthetic.main.item_picture.view.*
import timber.log.Timber
import java.util.*

class PictureAdapter(
    val context: Context,
    private val attachmentListener: AttachmentListener,
    private val imageItems: ArrayList<ImageItem>,
    private val parentPosition: Int
) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val mView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_picture, parent, false)
        return PictureViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return imageItems.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        Timber.d("@@pa oBind parentPosition: $parentPosition")
        holder as PictureViewHolder

        val item = imageItems[position]

        if (!TextUtils.isEmpty(item.url)) {
            Glide.with(context)
                .load(item.url)
                .into(holder.picture)
        } else {
            if (getLruCache(item.id) == null) {
                Timber.d("@@pa onGet parentPosition: $parentPosition")
                attachmentListener.onGetAttachment(item.id, parentPosition, position)

//                attachmentListener.onGetAttachment(
//                    item.id,
//                    position,
//                    AttachmentType.ADULT_PICTURE_INTERNAL_ITEM
//                )
            } else {
                Timber.d("@@pa Glide parentPosition: $parentPosition")
                val bitmap = getLruCache(item.id)
                Glide.with(context)
                    .load(bitmap)
                    .into(holder.picture)
            }
        }
    }

    class PictureViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val picture: ImageView = itemView.iv_picture
    }
}
