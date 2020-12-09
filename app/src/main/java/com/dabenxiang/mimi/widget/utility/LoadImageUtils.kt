package com.dabenxiang.mimi.widget.utility

import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.pref.Pref
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object LoadImageUtils: KoinComponent{

    val domainManager: DomainManager by inject()
    val accountManager: AccountManager by inject()
    val pref: Pref by inject()

    suspend fun loadImage(id: Long? = 0, view: ImageView, type: LoadImageType, filePath: String = "") {
        val defaultResId = when (type) {
            LoadImageType.AVATAR -> R.drawable.default_profile_picture
            LoadImageType.AVATAR_CS -> R.drawable.icon_cs_photo
            LoadImageType.PICTURE_THUMBNAIL -> R.drawable.img_nopic_02
            LoadImageType.PICTURE_FULL -> R.drawable.img_nopic_03
            LoadImageType.CLUB -> R.drawable.ico_group
            LoadImageType.CHAT_CONTENT -> R.drawable.bg_gray_6_radius_16
            LoadImageType.CLUB_TOPIC -> R.drawable.bg_topic_tab
            LoadImageType.PICTURE_EMPTY -> 0
        }
        if ((id == null || id == 0L) && TextUtils.isEmpty(filePath)) {
            Glide.with(view.context).load(defaultResId).into(view)
        } else {
            val accessToken = if (accountManager.isLogin()) {
                pref.memberToken.accessToken
            } else {
                pref.publicToken.accessToken
            }
            val auth = StringBuilder(ApiRepository.BEARER).append(accessToken).toString()
            var glideUrl: GlideUrl? = null

            if (TextUtils.isEmpty(filePath)) {
                val url = "${domainManager.getApiDomain()}/v1/Attachments/$id"
                glideUrl = GlideUrl(
                        url,
                        LazyHeaders.Builder()
                                .addHeader(ApiRepository.AUTHORIZATION, auth)
                                .addHeader(ApiRepository.X_DEVICE_ID, GeneralUtils.getAndroidID())
                                .build()
                )
            }

            val options = RequestOptions()
                    .priority(Priority.NORMAL)
                    .placeholder(defaultResId)
                    .error(defaultResId)
            when (type) {
                LoadImageType.AVATAR,
                LoadImageType.AVATAR_CS,
                LoadImageType.CLUB -> {
                    options.transform(MultiTransformation(CenterCrop(), CircleCrop()))
                }
                LoadImageType.PICTURE_THUMBNAIL -> {
                    options.transform(MultiTransformation(CenterCrop()))
                }
                LoadImageType.PICTURE_FULL -> {
                }
                LoadImageType.CHAT_CONTENT -> {
                    options.transform(CenterCrop(), RoundedCorners(16))
                }

                LoadImageType.CLUB_TOPIC -> {
                    options.transform(CenterInside(), RoundedCorners(15))
                }
            }
            Glide.with(view.context).load(glideUrl ?: filePath)
                    .apply(options)
                    .into(view)
        }
    }
}