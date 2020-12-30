package com.dabenxiang.mimi.view.post.article

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.MEMBER_DATA
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils.hideKeyboard
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_post_article.*
import kotlinx.android.synthetic.main.item_setting_bar.*


class PostArticleFragment : BasePostFragment() {

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_article
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txt_contentCount.text = String.format(getString(R.string.typing_count, edt_content.text.count(), CONTENT_LIMIT))

        viewModel.postDetailResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val item = it.result.content
                    val contentItem =
                        Gson().fromJson(item?.content, TextContentItem::class.java)
                    edt_content.setText(contentItem.text)

                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setUI(item: MediaItem, memberPostItem: MemberPostItem) {
        txt_contentCount.text = String.format(getString(R.string.typing_count, item.textContent.length, CONTENT_LIMIT))
        viewModel.getPostDetail(memberPostItem)
    }

    override fun setupListeners() {
        super.setupListeners()
        btn_tag_confirm.setOnClickListener { hashTagConfirm() }
        edt_content.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txt_contentCount.text = String.format(getString(R.string.typing_count, s?.length, CONTENT_LIMIT))
            }
        })

        tv_clean.setOnClickListener {

            hideKeyboard(requireActivity())
            
            val title = edt_title.text.toString()
            val content = edt_content.text.toString()

            if (checkFieldIsEmpty()) {
                return@setOnClickListener
            }

            if (content.isBlank()) {
                Toast.makeText(requireContext(), R.string.post_warning_content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!checkTagCountIsValid()) {
                return@setOnClickListener
            }

            val requestContent = ArticleItem(content)
            val jsonString = Gson().toJson(requestContent)

            navigation(title, jsonString)
        }
    }

    private fun navigation(title: String, request: String) {
        var memberPostItem: MemberPostItem? = null

        arguments?.let {
            if (it.containsKey(MEMBER_DATA)) {
                memberPostItem = it.getSerializable(MEMBER_DATA) as MemberPostItem
            }
        }

        val postClubItem = PostClubItem(PostType.TEXT.value, title, request, getTags(), memberPostItem = memberPostItem)

        val bundle = Bundle()
        bundle.putSerializable(POST_DATA, postClubItem)

        mainViewModel?.uploadData?.value = bundle
        findNavController().navigateUp()
    }
}