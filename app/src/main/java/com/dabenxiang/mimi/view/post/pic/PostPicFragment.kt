package com.dabenxiang.mimi.view.post.pic

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.PostPicItemListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.adapter.ScrollPicAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDialogListener
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.video.PostVideoFragment
import com.dabenxiang.mimi.view.post.viewer.PostViewerFragment.Companion.VIEWER_DATA
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_post_article.*
import kotlinx.android.synthetic.main.fragment_post_article.chipGroup
import kotlinx.android.synthetic.main.fragment_post_article.clubLayout
import kotlinx.android.synthetic.main.fragment_post_article.edt_hashtag
import kotlinx.android.synthetic.main.fragment_post_article.edt_title
import kotlinx.android.synthetic.main.fragment_post_article.iv_avatar
import kotlinx.android.synthetic.main.fragment_post_article.txt_hashtagCount
import kotlinx.android.synthetic.main.fragment_post_article.txt_titleCount
import kotlinx.android.synthetic.main.fragment_post_pic.*
import kotlinx.android.synthetic.main.fragment_post_pic.txt_clubName
import kotlinx.android.synthetic.main.fragment_post_pic.txt_hashtagName
import kotlinx.android.synthetic.main.fragment_post_pic.txt_placeholder
import kotlinx.android.synthetic.main.item_setting_bar.*


class PostPicFragment : BaseFragment() {

    private var haveMainTag = false

    companion object {
        const val BUNDLE_PIC_URI = "bundle_pic_uri"
        const val UPLOAD_PIC = "upload_pic"
        const val MEMBER_REQUEST = "member_request"
        const val PIC_URI = "pic_uri"
        const val DELETE_ATTACHMENT = "delete_attachment"

        const val POST_ID = "post_id"

        private const val TITLE_LIMIT = 60
        private const val HASHTAG_LIMIT = 20
        private const val HASHTAG_TEXT_LIMIT = 10
        private const val INIT_VALUE = 0
        private const val PHOTO_LIMIT = 20

        private const val REQUEST_MUTLI_PHOTO = 1001
        private const val INTENT_SELECT_IMG = 10001
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_pic
    }

    private var attachmentList = arrayListOf<PostAttachmentItem>()
    private var deletePicList = arrayListOf<String>()

    private var postId: Long = 0

    private lateinit var adapter: ScrollPicAdapter

    private val viewModel: PostPicViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSettings()
    }

    override fun initSettings() {
        adapter = ScrollPicAdapter(postPicItemListener)
        adapter.submitList(attachmentList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter

        tv_clean.isEnabled = true
        val img = requireContext().getDrawable(R.drawable.btn_close_n)
        tv_back.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)

        edt_hashtag.imeOptions = EditorInfo.IME_ACTION_DONE

        useAdultTheme(false)
    }

    override fun setupObservers() {
        viewModel.clubItemResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    txt_clubName.text = it.result.first().title
                    txt_hashtagName.text = it.result.first().tag

                    txt_placeholder.visibility = View.GONE
                    txt_clubName.visibility = View.VISIBLE
                    txt_hashtagName.visibility = View.VISIBLE

                    if (LruCacheUtils.getLruCache(it.result.first().avatarAttachmentId.toString()) == null) {
                        viewModel.getBitmapForClub(it.result.first().avatarAttachmentId.toString())
                    } else {
                        val bitmap = LruCacheUtils.getLruCache(it.result.first().avatarAttachmentId.toString())
                        Glide.with(requireContext()).load(bitmap).circleCrop().into(iv_avatar)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.bitmapResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    val bitmap = LruCacheUtils.getLruCache(it.result)
                    Glide.with(requireContext()).load(bitmap).circleCrop().into(iv_avatar)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        edt_title.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > TITLE_LIMIT) {
                        val content = it.toString().dropLast(1)
                        edt_title.setText(content)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txt_titleCount.text = String.format(getString(R.string.typing_count, s?.length,
                    TITLE_LIMIT
                ))
            }
        })

        edt_hashtag.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > HASHTAG_TEXT_LIMIT) {
                        val content = it.toString().dropLast(1)
                        edt_hashtag.setText(content)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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

        edt_hashtag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                if (chipGroup.size == HASHTAG_LIMIT) {
                    Toast.makeText(requireContext(), R.string.post_warning_tag_limit, Toast.LENGTH_SHORT).show()
                } else {
                    val tag = edt_hashtag.text.toString()
                    if (isTagExist(tag)) {
                        Toast.makeText(requireContext(), R.string.post_tag_already_have, Toast.LENGTH_SHORT).show()
                    } else {
                        addTag(tag)
                        edt_hashtag.text.clear()
                    }
                }
            }
            false
        }

        tv_back.setOnClickListener {
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

        tv_clean.setOnClickListener {
            val isEdit = arguments?.getBoolean(MyPostFragment.EDIT)

            val title = edt_title.text.toString()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), R.string.post_warning_title, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (chipGroup.childCount == 0) {
                Toast.makeText(requireContext(), R.string.post_warning_tag, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (adapter.getData().isEmpty()) {
                Toast.makeText(requireContext(), R.string.post_warning_pic, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tags = arrayListOf<String>()

            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i)
                chip as Chip
                tags.add(chip.text.toString())
            }

            val request = PostMemberRequest(
                title = title,
                type = PostType.IMAGE.value,
                tags = tags
            )

            val bundle = Bundle()
            bundle.putBoolean(UPLOAD_PIC, true)
            bundle.putParcelable(MEMBER_REQUEST, request)
            bundle.putParcelableArrayList(PIC_URI, adapter.getData())
            bundle.putStringArrayList(DELETE_ATTACHMENT, deletePicList)
            bundle.putLong(POST_ID, postId)

            if (isEdit != null && isEdit) {
                val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                findNavController().navigate(R.id.action_postPicFragment_to_myPostFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_postPicFragment_to_adultHomeFragment, bundle)
            }
        }
    }

    override fun setupFirstTime() {
        super.setupFirstTime()

        val isEdit = arguments?.getBoolean(MyPostFragment.EDIT)
        tv_clean.visibility = View.VISIBLE
        tv_clean.text = getString(R.string.btn_send)

        txt_titleCount.text = String.format(getString(R.string.typing_count,
            INIT_VALUE,
            TITLE_LIMIT
        ))
        txt_hashtagCount.text = String.format(getString(R.string.typing_count,
            INIT_VALUE,
            HASHTAG_LIMIT
        ))
        txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
            PHOTO_LIMIT
        ))

        if (isEdit!!) {
            tv_title.text = getString(R.string.edit_post_title)
            setUI()
        } else {
            tv_title.text = getString(R.string.post_title)
            val uri = arguments?.getString(BUNDLE_PIC_URI)
            val postAttachmentItem = PostAttachmentItem(uri = uri!!)
            attachmentList.add(postAttachmentItem)
            txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
                PHOTO_LIMIT
            ))
        }
    }

    private fun setUI() {
        val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
        val mediaItem = Gson().fromJson(item.content, MediaItem::class.java)

        postId = item.id

        edt_title.setText(item.title)

        for (tag in item.tags!!) {
            addEditTag(tag)
        }

        txt_titleCount.text = String.format(getString(R.string.typing_count,
            item.title.length,
            TITLE_LIMIT
        ))
        txt_hashtagCount.text = String.format(getString(R.string.typing_count,
            item.tags?.size,
            HASHTAG_LIMIT
        ))

        for (pic in mediaItem.picParameter) {
            val postAttachmentItem = PostAttachmentItem()
            postAttachmentItem.attachmentId = pic.id
            postAttachmentItem.ext = pic.ext
            attachmentList.add(postAttachmentItem)
        }

        txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
            PHOTO_LIMIT
        ))
    }

    private val chooseClubDialogListener = object : ChooseClubDialogListener {
        override fun onChooseClub(item: MemberClubItem) {
            txt_clubName.text = item.title
            txt_hashtagName.text = item.tag

            val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())
            Glide.with(requireContext())
                .load(bitmap)
                .circleCrop()
                .into(iv_avatar)

            if (chipGroup.size == HASHTAG_LIMIT) {
                Toast.makeText(requireContext(), R.string.post_warning_tag_limit, Toast.LENGTH_SHORT).show()
            } else {
                addTag(item.tag, true)
                txt_placeholder.visibility = View.GONE
                txt_clubName.visibility = View.VISIBLE
                txt_hashtagName.visibility = View.VISIBLE
            }
        }
    }

    private fun addEditTag(tag: String) {
        val chip = LayoutInflater.from(requireContext()).inflate(R.layout.chip_item, chipGroup, false) as Chip
        chip.text = tag
        chip.setTextColor(chip.context.getColor(R.color.color_black_1_50))
        chip.chipBackgroundColor =
            ColorStateList.valueOf(chip.context.getColor(R.color.color_black_1_10))

        if (chipGroup.size >= 1) {
            chip.closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.btn_close_circle_small_black_n)
            chip.isCloseIconVisible = true
            chip.setCloseIconSizeResource(R.dimen.dp_24)
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(it)
            }
        } else {
            viewModel.getClub(tag)
        }

        chipGroup.addView(chip)
        setTagCount()
    }

    private fun addTag(tag: String, isMainTag: Boolean = false) {
        val chip = LayoutInflater.from(requireContext()).inflate(R.layout.chip_item, chipGroup, false) as Chip
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
                }
            }
        } else {
            chip.closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.btn_close_circle_small_black_n)
            chip.isCloseIconVisible = true
            chip.setCloseIconSizeResource(R.dimen.dp_24)
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(it)
            }
            chipGroup.addView(chip)
            setTagCount()
        }
    }

    private fun setTagCount() {
        txt_hashtagCount.text = String.format(getString(R.string.typing_count, chipGroup.size,
            HASHTAG_LIMIT
        ))
    }

    private fun updateCountPicView() {
        attachmentList.clear()
        attachmentList.addAll(adapter.getData())
        adapter.notifyDataSetChanged()
        txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
            PHOTO_LIMIT
        ))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val clipData = data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val item = clipData.getItemAt(i)
                    val uri = item.uri
                    val uriDataList = adapter.getData()
                    val postAttachmentItem = PostAttachmentItem(uri = uri.toString())
                    uriDataList.add(postAttachmentItem)
                }
                updateCountPicView()
            } else {
                val uri = if (data?.data == null) {
                    val extras = data?.extras
                    val imageBitmap = extras!!["data"] as Bitmap?
                    Uri.parse(MediaStore.Images.Media.insertImage(requireContext().contentResolver, imageBitmap, null,null))
                } else {
                    data.data!!
                }

                val uriDataList = adapter.getData()
                val postAttachmentItem = PostAttachmentItem(uri = uri.toString())
                uriDataList.add(postAttachmentItem)
                updateCountPicView()
            }
        }
    }

    private val postPicItemListener by lazy {
        PostPicItemListener(
            { id, function -> getBitmap(id, function) },
            { item -> handleDeletePic(item) },
            { updateCountPicView() },
            { addPic() },
            { viewerItem -> openViewerPage(viewerItem) }
        )
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
    }

    private fun handleDeletePic(item: PostAttachmentItem) {
        for (data in attachmentList) {
            if (item.attachmentId == data.attachmentId) {
                deletePicList.add(item.attachmentId)
            }
        }
    }

    private fun addPic() {
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        galleryIntent.action = Intent.ACTION_GET_CONTENT

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent)
        chooser.putExtra(Intent.EXTRA_TITLE, requireContext().getString(R.string.post_select_pic))

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        startActivityForResult(chooser, INTENT_SELECT_IMG)
    }

    private fun openViewerPage(viewerItem: ViewerItem) {
        val bundle = Bundle()
        bundle.putSerializable(VIEWER_DATA, viewerItem)
        findNavController().navigate(R.id.action_postPicFragment_to_postViewerFragment, bundle)
    }

    private fun isTagExist(tag: String): Boolean  {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.text == tag) {
                return true
            }
        }
        return false
    }
}