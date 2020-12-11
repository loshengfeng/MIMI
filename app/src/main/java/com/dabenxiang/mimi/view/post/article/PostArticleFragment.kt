package com.dabenxiang.mimi.view.post.article

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.view.size
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ArticleItem
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.EDIT
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.MEMBER_DATA
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment.Companion.KEY_DATA
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
    }

    override fun setUI(item: MediaItem) {
        edt_content.setText(item.textContent)
        txt_contentCount.text = String.format(getString(R.string.typing_count, item.textContent.length, CONTENT_LIMIT))
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
        var isEdit = false
        var page = ""
        var searchPostItem: SearchPostItem? = null
        var memberClubItem: MemberClubItem? = null

        arguments?.let {
            isEdit = it.getBoolean(EDIT, false)
            page = it.getString(PAGE, "")
            val data = it.getSerializable(KEY_DATA)
            if (data != null) {
                if (data is SearchPostItem) {
                    searchPostItem = data
                } else if (data is MemberClubItem){
                    memberClubItem = data
                }
            }
        }

        val bundle = Bundle()
        bundle.putBoolean(UPLOAD_ARTICLE, true)
        bundle.putString(TITLE, title)
        bundle.putString(REQUEST, request)
        bundle.putStringArrayList(TAG, getTags())
        if (isEdit && page == MY_POST) {
            val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MEMBER_DATA, item)
            findNavController().navigate(R.id.action_postArticleFragment_to_myPostFragment, bundle)
        }  else if (isEdit && page == SEARCH) {
            val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MEMBER_DATA, item)
            bundle.putSerializable(KEY_DATA, searchPostItem)
            findNavController().navigate(R.id.action_postArticleFragment_to_searchPostFragment, bundle)
        } else if (isEdit && page == CLUB) {
            val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MEMBER_DATA, item)
            bundle.putSerializable(KEY_DATA, memberClubItem)
            findNavController().navigate(R.id.action_postArticleFragment_to_topicDetailFragment, bundle)
        } else if (isEdit && page == TAB) {
            val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MEMBER_DATA, item)
            findNavController().navigate(R.id.action_postArticleFragment_to_clubTabFragment, bundle)
        } else if (isEdit && page == TEXT) {
            val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(PictureDetailFragment.KEY_DATA, item)
            bundle.putSerializable(MEMBER_DATA, item)
            findNavController().navigate(R.id.action_postArticleFragment_to_clubTextFragment, bundle)
        } else if (isEdit && page == FAVORITE) {
            val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MEMBER_DATA, item)
            findNavController().navigate(R.id.action_postArticleFragment_to_myCollectionFragment, bundle)
        } else {
            findNavController().navigate(R.id.action_postArticleFragment_to_clubTabFragment, bundle)
        }
    }
}