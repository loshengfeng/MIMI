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
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_picture.view.*
import java.util.*

class PictureAdapter(
    val context: Context,
    private val imageItems: ArrayList<ImageItem>,
    private val onItemClickListener: OnItemClickListener,
    private val memberPostFuncItem: MemberPostFuncItem = MemberPostFuncItem()
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
        holder as PictureViewHolder

        val item = imageItems[position]
        holder.onBind(item, onItemClickListener, memberPostFuncItem)

    }

    class PictureViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val picture: ImageView = itemView.iv_picture

        fun onBind(
            item: ImageItem,
            onItemClickListener: OnItemClickListener,
            memberPostFuncItem: MemberPostFuncItem
        ) {
            if (!TextUtils.isEmpty(item.url)) {
                Glide.with(picture.context)
                    .load(item.url).placeholder(R.drawable.img_nopic_03).into(picture)
            } else {
                if (!TextUtils.isEmpty(item.id) && item.id != LruCacheUtils.ZERO_ID) {
                    if (LruCacheUtils.getLruCache(item.id) == null) {
                        memberPostFuncItem.getBitmap(item.id) { id -> updatePicture(id)}
                    } else {
                        updatePicture(item.id)
                    }
                } else {
                    Glide.with(picture.context).load(R.drawable.img_nopic_03).into(picture)
                }
            }

            picture.setOnClickListener {
                onItemClickListener.onItemClick()
            }
        }

        private fun updatePicture(id: String) {
            val bitmap = LruCacheUtils.getLruCache(id)
            Glide.with(picture.context).load(bitmap).into(picture)
        }
    }

}
