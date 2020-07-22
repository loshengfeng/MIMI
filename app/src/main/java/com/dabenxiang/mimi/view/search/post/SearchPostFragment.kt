package com.dabenxiang.mimi.view.search.post

import android.os.Bundle
import com.dabenxiang.mimi.view.base.BaseFragment

class SearchPostFragment : BaseFragment() {

    companion object {
        private const val KEY_DATA_TITLE = "title"
        private const val KEY_DATA_TAG = "tag"

        fun createBundle(title: String = "", tag: String = ""): Bundle {
            return Bundle().also {
                it.putString(KEY_DATA_TITLE, title)
                it.putString(KEY_DATA_TAG, tag)
            }
        }
    }

    override fun getLayoutId(): Int {
        TODO("Not yet implemented")
    }

    override fun setupObservers() {
        TODO("Not yet implemented")
    }

    override fun setupListeners() {
        TODO("Not yet implemented")
    }

}