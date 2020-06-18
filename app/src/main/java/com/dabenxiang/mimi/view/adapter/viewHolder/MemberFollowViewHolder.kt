package com.dabenxiang.mimi.view.adapter.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import timber.log.Timber

class MemberFollowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val ivPhoto = itemView.findViewById(R.id.iv_photo) as ImageView
    private val tvName = itemView.findViewById(R.id.tv_name) as TextView
//    private val tvSubTitle = itemView.findViewById(R.id.tv_sub_title) as TextView
//    private val tvFollow = itemView.findViewById(R.id.tv_follow) as TextView

    private var memberFollowItem : MemberFollowItem? = null

    init {
        view.setOnClickListener {
            // todo : 點擊後，進入該會員的VAI4.1.6_蜜主頁
            Timber.d("onClick")
        }

        // todo: [已關注]按鈕點擊後，取消關注
    }

    companion object {
        fun create(parent: ViewGroup): ClubFollowViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_follow, parent, false)
            return ClubFollowViewHolder(view)
        }
    }

    fun bind(MemberFolllowItem: MemberFollowItem?) {
        this.memberFollowItem = MemberFolllowItem
//        getAttachment()
        tvName.text = memberFollowItem?.friendlyName ?: ""
//        tvSubTitle.text = MemberFolllowItem?.description
//        tvFollow.text = MemberFolllowItem?.tag
    }
    // todo : not sure...
//    private fun setupPhoto(bitmap: Bitmap) {
//        val options: RequestOptions = RequestOptions()
//            .transform(MultiTransformation(CenterCrop(), CircleCrop()))
//            .placeholder(R.mipmap.ic_launcher)
//            .error(R.mipmap.ic_launcher)
//            .priority(Priority.NORMAL)
//
//        Glide.with(ivPhoto.context).load(bitmap)
//            .apply(options)
//            .into(ivPhoto)
//    }

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