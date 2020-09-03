package com.dabenxiang.mimi.view.fullpicture

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_full_picture.view.*

class FullPictureAdapter(
    val context: Context,
    private val imageItems: ArrayList<ImageItem>,
    private val onFullPictureListener: OnFullPictureListener
) : RecyclerView.Adapter<FullPictureAdapter.FullPictureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullPictureViewHolder {
        val mView = LayoutInflater.from(context)
            .inflate(R.layout.item_full_picture, parent, false)
        return FullPictureViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return imageItems.size
    }

    override fun onBindViewHolder(holder: FullPictureViewHolder, position: Int) {
        val imageItem = imageItems[position]

        onFullPictureListener.onGetAttachment(imageItem.id.toLongOrNull(), holder.picture)
    }

    class FullPictureViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val picture: ImageView = itemView.iv_picture
    }

    interface OnFullPictureListener {
        fun onGetAttachment(id: Long?, view:ImageView)
    }
}