package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.callback.OnPictureItemClickListener
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LoadImageUtils
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_picture.view.*
import kotlinx.android.synthetic.main.item_picture_is_not_login.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class PictureAdapter(
    private val context: Context,
    private val imageItems: ArrayList<ImageItem>,
    private val onItemClickListener: OnPictureItemClickListener,
    private val viewModelScope: CoroutineScope
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
        holder.onBind(position, item, onItemClickListener, viewModelScope)
    }

    class PictureViewHolder(itemView: View) : BaseViewHolder(itemView), KoinComponent {

        val accountManager: AccountManager by inject()
        val picture: ImageView = itemView.iv_picture
        val isNotLogin:View = itemView.item_is_not_login

        fun onBind(
            position: Int,
            item: ImageItem,
            onItemClickListener: OnPictureItemClickListener,
            viewModelScope: CoroutineScope
        ) {

            val blur = BlurTransformation( 15, 1)
            if (!TextUtils.isEmpty(item.url)) {

                val options = RequestOptions()
                        .priority(Priority.NORMAL)
                if(position > 0 && !accountManager.isLogin())
                    options.optionalTransform(blur)
                Glide.with(picture.context)
                    .load(item.url).apply(options).into(picture)
            } else {
                viewModelScope.launch {
                    LoadImageUtils.loadImage(id = item.id.toLongOrNull(),
                            view =picture,
                            type =LoadImageType.PICTURE_EMPTY,
                            blur =  if(position > 0 && !accountManager.isLogin()) blur else null
                    )
                }
            }

            isNotLogin.visibility = if(position==0 || accountManager.isLogin()) View.GONE else View.VISIBLE

            picture.setOnClickListener {
                onItemClickListener.onItemClick()
            }

            isNotLogin.tv_register.setOnClickListener {
                onItemClickListener.onRegisterClick()
            }

            isNotLogin.tv_login.setOnClickListener {
                onItemClickListener.onLoginClick()
            }
        }
    }

}
