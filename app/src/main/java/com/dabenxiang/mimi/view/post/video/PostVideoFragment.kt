package com.dabenxiang.mimi.view.post.video

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.ColorStateList
import android.media.MediaMetadataRetriever
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
import com.dabenxiang.mimi.callback.PostVideoItemListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ScrollVideoAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDialogListener
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.viewer.PostViewerFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.chip.Chip
import com.google.gson.Gson
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
import kotlinx.android.synthetic.main.item_setting_bar.*
import java.io.File
import java.util.concurrent.TimeUnit


class PostVideoFragment : BaseFragment() {

    private var haveMainTag = false

    companion object {
        private const val REQUEST_VIDEO_CAPTURE = 10001
        const val BUNDLE_TRIMMER_URI = "bundle_trimmer_uri"
        const val BUNDLE_COVER_URI = "bundle_cover_uri"

        private const val TITLE_LIMIT = 60
        private const val CONTENT_LIMIT = 2000
        private const val HASHTAG_LIMIT = 20
        private const val INIT_VALUE = 0
        const val POST_ID = "post_id"

        const val UPLOAD_VIDEO = "upload_video"
        const val MEMBER_REQUEST = "member_request"
        const val VIDEO_DATA = "video_data"
        const val DELETE_ATTACHMENT = "delete_attachment"
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_video
    }

    private val videoAttachmentList = arrayListOf<PostVideoAttachment>()
    private val deleteVideoList = arrayListOf<PostVideoAttachment>()

    private lateinit var adapter: ScrollVideoAdapter

    private var postId: Long = 0

    private val viewModel: PostVideoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSettings()

        adapter = ScrollVideoAdapter(postPicItemListener)
        adapter.submitList(videoAttachmentList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter
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
                    addTag(edt_hashtag.text.toString())
                    edt_hashtag.text.clear()
                }
            }
            false
        }

        tv_back.setOnClickListener {
            findNavController().navigate(R.id.action_postVideoFragment_to_adultHomeFragment)
        }

        tv_clean.setOnClickListener {
            val title = edt_title.text.toString()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), R.string.post_warning_title, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (chipGroup.childCount == 0) {
                Toast.makeText(requireContext(), R.string.post_warning_tag, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (videoAttachmentList.isEmpty()) {
                Toast.makeText(requireContext(), R.string.post_warning_video, Toast.LENGTH_SHORT).show()
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
                type = PostType.VIDEO.value,
                tags = tags
            )

            if (videoAttachmentList[0].videoAttachmentId.isBlank()) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.parse(videoAttachmentList[0].videoUrl))
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val timeInMillisec: Long = time.toLong()
                retriever.release()
                val length = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(timeInMillisec),
                    TimeUnit.MILLISECONDS.toMinutes(timeInMillisec) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMillisec)),
                    TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec)));
                videoAttachmentList[0].length = length
            }

            val bundle = Bundle()
            bundle.putBoolean(UPLOAD_VIDEO, true)
            bundle.putParcelable(MEMBER_REQUEST, request)
            bundle.putParcelableArrayList(VIDEO_DATA, videoAttachmentList)
            bundle.putParcelableArrayList(DELETE_ATTACHMENT, deleteVideoList)
            bundle.putLong(POST_ID, postId)
            findNavController().navigate(R.id.action_postVideoFragment_to_adultHomeFragment, bundle)
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

        if (isEdit!!) {
            tv_title.text = getString(R.string.edit_post_title)
            setUI()
        } else {
            tv_title.text = getString(R.string.post_title)
            val trimmerUri = arguments?.getString(BUNDLE_TRIMMER_URI)
            val picUri = arguments?.getString(BUNDLE_COVER_URI)
            val postVideoAttachment = PostVideoAttachment(videoUrl = trimmerUri!!, picUrl = picUri!!)
            videoAttachmentList.add(postVideoAttachment)
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
            item.tags.size,
            HASHTAG_LIMIT
        ))

        val postVideoAttachment = PostVideoAttachment(
            videoAttachmentId = mediaItem.videoParameter.id,
            length = mediaItem.videoParameter.length,
            picAttachmentId = mediaItem.picParameter[0].id,
            ext = mediaItem.picParameter[0].ext
        )
        videoAttachmentList.add(postVideoAttachment)
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

            addTag(item.tag)

            txt_placeholder.visibility = View.GONE
            txt_clubName.visibility = View.VISIBLE
            txt_hashtagName.visibility = View.VISIBLE
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_VIDEO_CAPTURE -> {
                    val videoUri: Uri? = data?.data
                    val myUri = Uri.fromFile(File(UriUtils.getPath(requireContext(), videoUri!!)))
                    val bundle = Bundle()
                    bundle.putString(EditVideoFragment.BUNDLE_VIDEO_URI, myUri.toString())
                    findNavController().navigate(R.id.action_postVideoFragment_to_editVideoFragment, bundle)
                }
            }
        }
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
    }

    private fun openRecorder() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(requireContext().packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }

    private fun deleteVideo(item: PostVideoAttachment) {
        deleteVideoList.clear()
        if (item.videoAttachmentId ==  videoAttachmentList[0].videoAttachmentId) {
            deleteVideoList.add(item)
        }

        videoAttachmentList.clear()
    }

    private val postPicItemListener by lazy {
        PostVideoItemListener(
            { id, function -> getBitmap(id, function) },
            { openRecorder() },
            { item -> deleteVideo(item) },
            { viewerItem -> openViewerPage(viewerItem) }
        )
    }

    private fun openViewerPage(viewerItem: ViewerItem) {
        val bundle = Bundle()
        bundle.putSerializable(PostViewerFragment.VIEWER_DATA, viewerItem)
        findNavController().navigate(R.id.action_postVideoFragment_to_postViewerFragment, bundle)
    }
}