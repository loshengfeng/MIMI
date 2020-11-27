package com.dabenxiang.mimi.view.clip

import android.widget.ImageView
import androidx.paging.PagingData
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import kotlinx.coroutines.CoroutineScope

data class ClipFuncItem(
    val getBitmap: ((Long?, ImageView, LoadImageType) -> Unit) = { _, _, _ -> },
    val onFavoriteClick: ((VideoItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onLikeClick: ((VideoItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onCommentClick: ((VideoItem) -> Unit) = { _ -> },
    val onPlayerError: ((String, String) -> Unit) = { _, _ -> },
    val onVipClick: (() -> Unit) = {},
    val onPromoteClick: (() -> Unit) = {},
    val getClips: (((PagingData<VideoItem>, CoroutineScope) -> Unit)) -> Unit = { _ -> },
    val getM3U8: (VideoItem, Int,  (Int, String, Int) -> Unit) -> Unit = { _, _, _ -> }
)