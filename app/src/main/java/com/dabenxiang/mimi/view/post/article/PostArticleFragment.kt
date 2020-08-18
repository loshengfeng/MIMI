package com.dabenxiang.mimi.view.post.article

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ArticleItem
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.EDIT
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.MEMBER_DATA
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_post_article.*
import kotlinx.android.synthetic.main.item_setting_bar.*


class PostArticleFragment : BasePostFragment() {

    companion object {
        const val UPLOAD_ARTICLE = "upload_article"
        const val TITLE = "title"
        const val REQUEST = "request"
        const val TAG = "tag"
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_article
    }

    override fun setUI(item: MediaItem) {
        edt_content.setText(item.textContent)
        txt_contentCount.text = String.format(getString(R.string.typing_count, item.textContent.length, CONTENT_LIMIT))

    }

    override fun setupListeners() {
        super.setupListeners()

        edt_content.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > CONTENT_LIMIT) {
                        val content = it.toString().dropLast(1)
                        edt_title.setText(content)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txt_contentCount.text = String.format(getString(R.string.typing_count, s?.length, CONTENT_LIMIT))
            }
        })

        tv_clean.setOnClickListener {
            val isEdit = arguments?.getBoolean(EDIT)

            val title = edt_title.text.toString()
            val content = edt_content.text.toString()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), R.string.post_warning_title, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (content.isBlank()) {
                Toast.makeText(requireContext(), R.string.post_warning_content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (chipGroup.childCount == 0) {
                Toast.makeText(requireContext(), R.string.post_warning_tag, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestContent = ArticleItem(content)

            val tags = arrayListOf<String>()

            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i)
                chip as Chip
                tags.add(chip.text.toString())
            }

            val jsonString = Gson().toJson(requestContent)

            val bundle = Bundle()
            bundle.putBoolean(UPLOAD_ARTICLE, true)
            bundle.putString(TITLE, title)
            bundle.putString(REQUEST, jsonString)
            bundle.putStringArrayList(TAG, tags)
            if (isEdit != null) {
                val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
                bundle.putSerializable(MEMBER_DATA, item)
                findNavController().navigate(R.id.action_postArticleFragment_to_myPostFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_postArticleFragment_to_adultHomeFragment, bundle)
            }
        }
    }
}