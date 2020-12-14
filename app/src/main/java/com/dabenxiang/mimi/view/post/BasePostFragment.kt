package com.dabenxiang.mimi.view.post

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDialogListener
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_post_article.*
import kotlinx.android.synthetic.main.item_setting_bar.*

open class BasePostFragment : BaseFragment() {

    val viewModel: PostViewModel by viewModels()

    private var haveMainTag = false

    var postId: Long = 0

    companion object {
        const val CONTENT_LIMIT = 2000
        const val PHOTO_LIMIT = 10
        const val RECORD_LIMIT_TIME = 15

        const val INTENT_SELECT_IMG = 10001
        const val REQUEST_VIDEO_CAPTURE = 10002

        private const val TITLE_LIMIT = 60
        private const val HASHTAG_LIMIT = 20
        private const val HASHTAG_TEXT_LIMIT = 10
        private const val INIT_VALUE = 0

        const val TAG = "tag"
        const val REQUEST = "request"
        const val TITLE = "title"
        const val UPLOAD_ARTICLE = "upload_article"
        const val POST_ID = "post_id"
        const val BUNDLE_PIC_URI = "bundle_pic_uri"
        const val UPLOAD_PIC = "upload_pic"
        const val MEMBER_REQUEST = "member_request"
        const val PIC_URI = "pic_uri"
        const val DELETE_ATTACHMENT = "delete_attachment"
        const val UPLOAD_VIDEO = "upload_video"
        const val VIDEO_DATA = "video_data"
        const val BUNDLE_TRIMMER_URI = "bundle_trimmer_uri"
        const val BUNDLE_COVER_URI = "bundle_cover_uri"
        const val PAGE = "page"
        const val ADULT = "adult"
        const val MY_POST = "my_post"
        const val SEARCH = "search"
        const val CLUB = "club"
        const val TAB = "tab"
        const val VIDEO = "video"
        const val TEXT = "text"
        const val PIC = "pic"
        const val FAVORITE = "favorite"
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSettings()
    }

    override fun getLayoutId() = 0

    override fun setupObservers() {
        viewModel.clubItemResult.observe(viewLifecycleOwner, {
            when (it) {
                is Success -> {
                    txt_clubName.text = it.result.first().title
                    txt_hashtagName.text = it.result.first().tag

                    txt_placeholder.visibility = View.GONE
                    txt_clubName.visibility = View.VISIBLE
                    txt_hashtagName.visibility = View.VISIBLE

                    viewModel.loadImage(
                        it.result.first().avatarAttachmentId,
                        iv_avatar,
                        LoadImageType.CLUB
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

        requireActivity().onBackPressedDispatcher.addCallback(
            owner = viewLifecycleOwner,
            onBackPressed = { handleBackEvent() }
        )

        edt_title.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txt_titleCount.text = String.format(
                    getString(
                        R.string.typing_count, s?.length,
                        TITLE_LIMIT
                    )
                )
            }
        })

        clubLayout.setOnClickListener {
            ChooseClubDialogFragment.newInstance(chooseClubDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    ChooseUploadMethodDialogFragment::class.java.simpleName
                )
            }
        }

        tv_back.setOnClickListener {
            handleBackEvent()
        }

        edt_hashtag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hashTagConfirm()
            }
            false
        }
    }

    fun hashTagConfirm(){
        if (chipGroup.size >= HASHTAG_LIMIT) {
            Toast.makeText(
                    requireContext(),
                    R.string.post_warning_tag_limit,
                    Toast.LENGTH_SHORT
            ).show()
        } else {
            val tag = edt_hashtag.text.toString()
            if (tag.isBlank()) {
                return 
            }
            if (isTagExist(tag)) {
                Toast.makeText(
                        requireContext(),
                        R.string.post_tag_already_have,
                        Toast.LENGTH_SHORT
                ).show()
            } else {
                addTag(tag)
                edt_hashtag.text.clear()
            }
        }
    }

    override fun initSettings() {

    }

    override fun setupFirstTime() {
        val isEdit = arguments?.getBoolean(MyPostFragment.EDIT, false)

        tv_clean.visibility = View.VISIBLE
        tv_clean.text = getString(R.string.btn_send)
        tv_clean.isEnabled = true

        val img = requireContext().getDrawable(R.drawable.btn_close_n)
        tv_back.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)

        txt_titleCount.text =
            String.format(getString(R.string.typing_count, INIT_VALUE, TITLE_LIMIT))
        txt_hashtagCount.text =
            String.format(getString(R.string.typing_count, INIT_VALUE, HASHTAG_LIMIT))

        if (isEdit != null && isEdit) {
            tv_title.text = getString(R.string.edit_post_title)
            setUI()
        } else {
            tv_title.text = getString(R.string.post_title)
            handlePic()
            handleVideo()
        }

        useAdultTheme(false)
    }

    open fun handlePic() {
    }

    open fun handleVideo() {

    }

    private fun setUI() {
        val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
        val contentItem = Gson().fromJson(item.content, MediaItem::class.java)

        postId = item.id

        edt_title.setText(item.title)

        for (tag in item.tags!!) {
            addEditTag(tag)
        }

        txt_titleCount.text =
            String.format(getString(R.string.typing_count, item.title.length, TITLE_LIMIT))
        txt_hashtagCount.text =
            String.format(getString(R.string.typing_count, item.tags?.size, HASHTAG_LIMIT))

        haveMainTag = true

        setUI(contentItem, item)
    }

    open fun setUI(item: MediaItem, memberPostItem: MemberPostItem ) {

    }

    private fun addEditTag(tag: String) {
        val chip = LayoutInflater.from(requireContext())
            .inflate(R.layout.chip_item, chipGroup, false) as Chip
        chip.text = tag
        chip.setTextColor(chip.context.getColor(R.color.color_black_1_50))
        chip.chipBackgroundColor =
            ColorStateList.valueOf(chip.context.getColor(R.color.color_black_1_10))

        if (chipGroup.size >= 1) {
            chip.closeIcon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.btn_close_circle_small_black_n
            )
            chip.isCloseIconVisible = true
            chip.setCloseIconSizeResource(R.dimen.dp_24)
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(it)
                setTagCount()
            }
        } else {
            viewModel.getClub(tag)
        }

        chipGroup.addView(chip)
        setTagCount()
    }

    private fun setTagCount() {
        txt_hashtagCount.text =
            String.format(getString(R.string.typing_count, chipGroup.size, HASHTAG_LIMIT))

        if (chipGroup.size >= HASHTAG_LIMIT) {
            btn_tag_confirm.visibility = GONE
        } else {
            btn_tag_confirm.visibility = VISIBLE
        }
    }

    private fun addTag(tag: String, isMainTag: Boolean = false) {
        val chip = LayoutInflater.from(requireContext())
            .inflate(R.layout.chip_item, chipGroup, false) as Chip
        chip.text = tag
        chip.setTextColor(chip.context.getColor(R.color.color_black_1_50))
        chip.chipBackgroundColor =
            ColorStateList.valueOf(chip.context.getColor(R.color.color_black_1_10))

        if (isMainTag) {
            if (haveMainTag) {
                val mainTag = chipGroup[0] as Chip
                mainTag.text = tag
            } else {
                haveMainTag = true

                if (chipGroup.isEmpty()) {
                    chipGroup.addView(chip)
                    setTagCount()
                } else {
                    val chipList = arrayListOf<Chip>()
                    for (i in 0 until chipGroup.size) {
                        val chipItem = chipGroup[i] as Chip
                        chipList.add(chipItem)
                    }

                    chipGroup.removeAllViews()
                    chipGroup.addView(chip)
                    for (tagItem in chipList) {
                        chipGroup.addView(tagItem)
                    }
                    setTagCount()
                }
            }
        } else {
            chip.closeIcon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.btn_close_circle_small_black_n
            )
            chip.isCloseIconVisible = true
            chip.setCloseIconSizeResource(R.dimen.dp_24)
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(it)
                setTagCount()
                enableHastEditText()
            }
            chipGroup.addView(chip)
            setTagCount()
        }

        enableHastEditText()
    }

    private fun enableHastEditText() {
        if (chipGroup.size >= HASHTAG_LIMIT) {
            edt_hashtag.isEnabled = false
            edt_hashtag.hint = getString(R.string.post_tag_full)
            hashTagLayout.background  = ContextCompat.getDrawable(requireContext(), R.drawable.post_text_rectangle_tag_full)
        } else {
            edt_hashtag.isEnabled = true
            edt_hashtag.hint = getString(R.string.post_hint_tag)
            hashTagLayout.background  = ContextCompat.getDrawable(requireContext(), R.drawable.post_text_rectangle)
        }
    }

    private fun handleBackEvent() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.whether_to_discard_content,
                messageIcon = R.drawable.ico_default_photo,
                firstBtn = getString(R.string.btn_cancel),
                secondBtn = getString(R.string.btn_confirm),
                isMessageIcon = false,
                secondBlock = {
                    findNavController().navigateUp()
                }
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private val chooseClubDialogListener = object : ChooseClubDialogListener {
        override fun onChooseClub(item: MemberClubItem) {
            txt_clubName.text = item.title
            txt_hashtagName.text = item.tag

            viewModel.loadImage(item.avatarAttachmentId, iv_avatar, LoadImageType.CLUB)

            addTag(item.tag, true)
            txt_placeholder.visibility = View.GONE
            txt_clubName.visibility = View.VISIBLE
            txt_hashtagName.visibility = View.VISIBLE
        }
    }

    private fun isTagExist(tag: String): Boolean {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.text == tag) {
                return true
            }
        }
        return false
    }

    fun getTags(): ArrayList<String> {
        val tags = arrayListOf<String>()

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i)
            chip as Chip
            tags.add(chip.text.toString())
        }

        return tags
    }

    fun checkFieldIsEmpty(): Boolean {
        val title = edt_title.text.toString()

        if (title.isBlank()) {
            Toast.makeText(requireContext(), R.string.post_warning_title, Toast.LENGTH_SHORT).show()
            return true
        }

        if (chipGroup.childCount == 0) {
            Toast.makeText(requireContext(), R.string.post_warning_tag, Toast.LENGTH_SHORT).show()
            return true
        }

        return false
    }

    fun getRequest(title: String, type: Int): PostMemberRequest {
        return PostMemberRequest(
            title = title,
            type = type,
            tags = getTags()
        )
    }

    fun checkTagCountIsValid(): Boolean {
        return if (chipGroup.size > HASHTAG_LIMIT) {
            Toast.makeText(
                requireContext(),
                R.string.post_warning_tag_limit,
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            true
        }
    }
}