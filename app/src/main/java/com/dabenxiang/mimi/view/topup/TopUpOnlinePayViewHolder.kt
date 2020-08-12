package com.dabenxiang.mimi.view.topup

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_topup_online_pay.view.*

class TopUpOnlinePayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val orderPackageLayout: ConstraintLayout = itemView.layout_order_package
    val ivCheck: ImageView = itemView.iv_check
    val tvPackageName: TextView = itemView.tv_package_name
    val listPrice: TextView = itemView.tv_list_price
    val price: TextView = itemView.tv_price
}