package com.dabenxiang.mimi.view.clip

import android.widget.ImageView
import androidx.paging.PagingData
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import kotlinx.coroutines.CoroutineScope

data class ClipFuncItem(
    val getClip: ((String, Int) -> Unit) = { _, _ -> },
    val getBitmap: ((Long?, ImageView, LoadImageType) -> Unit) = { _, _, _ -> },
    val onFollowClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onFavoriteClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onLikeClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onCommentClick: ((MemberPostItem) -> Unit) = { _ -> },
    val onBackClick: (() -> Unit) = {},
    val onPlayerError: ((String, String) -> Unit) = { _, _ -> },
    val onVipClick: (() -> Unit) = {},
    val onPromoteClick: (() -> Unit) = {},
    val getClips: (((PagingData<MemberPostItem>, CoroutineScope) -> Unit)) -> Unit = { _ -> },
    val getPostDetail: (MemberPostItem, Int,  (Int, Boolean) -> Unit) -> Unit = { _, _, _ -> }
)