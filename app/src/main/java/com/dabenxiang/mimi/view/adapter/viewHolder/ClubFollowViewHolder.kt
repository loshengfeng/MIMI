package com.dabenxiang.mimi.view.adapter.viewHolder

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import timber.log.Timber

class ClubFollowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val ivPhoto = itemView.findViewById(R.id.iv_photo) as ImageView
    private val tvName = itemView.findViewById(R.id.tv_name) as TextView
//    private val tvSubTitle = itemView.findViewById(R.id.tv_sub_title) as TextView
//    private val tvFollow = itemView.findViewById(R.id.tv_follow) as TextView

    private var clubFollowItem : ClubFollowItem? = null

    init {
        view.setOnClickListener {
            // todo : 16/06/2020
            Timber.d("onClick")
//            post?.url?.let { url ->
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                view.context.startActivity(intent)
//            }
        }
    }

    companion object {
        fun create(parent: ViewGroup): ClubFollowViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_follow, parent, false)
            return ClubFollowViewHolder(view)
        }
    }

    fun bind(clubFollowItem: ClubFollowItem?) {
        this.clubFollowItem = clubFollowItem
//        getAttachment()
        tvName.text = clubFollowItem?.name ?: ""
//        tvSubTitle.text = clubFollowItem?.description
//        tvFollow.text = clubFollowItem?.tag
    }

    private fun setupPhoto(bitmap: Bitmap) {
        val options: RequestOptions = RequestOptions()
            .transform(MultiTransformation(CenterCrop(), CircleCrop()))
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .priority(Priority.NORMAL)

        Glide.with(ivPhoto.context).load(bitmap)
            .apply(options)
            .into(ivPhoto)
    }

//    private fun getAttachment() {
//        viewModelScope.launch {
//            flow {
//                val result = domainManager.getApiRepository()
//                    .getAttachment(accountManager.getProfile().avatarAttachmentId)
//                if (!result.isSuccessful) throw HttpException(result)
//                val byteArray = result.body()?.bytes()
//                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
//                emit(ApiResult.success(bitmap))
//            }
//                .onStart { emit(ApiResult.loading()) }
//                .catch { e -> emit(ApiResult.error(e)) }
//                .onCompletion { emit(ApiResult.loaded()) }
//                .collect { _imageBitmap.value = it }
//        }
//    }
}