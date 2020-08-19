package com.dabenxiang.mimi.view.post.video

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.PostVideoItemListener
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ScrollVideoAdapter
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.viewer.PostViewerFragment
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_post_article.chipGroup
import kotlinx.android.synthetic.main.fragment_post_article.edt_hashtag
import kotlinx.android.synthetic.main.fragment_post_article.edt_title
import kotlinx.android.synthetic.main.fragment_post_pic.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import java.io.File
import java.util.concurrent.TimeUnit


class PostVideoFragment : BasePostFragment() {

    private var haveMainTag = false

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_video
    }

    private val videoAttachmentList = arrayListOf<PostVideoAttachment>()
    private val deleteVideoList = arrayListOf<PostVideoAttachment>()

    private lateinit var adapter: ScrollVideoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSettings()
    }

    override fun initSettings() {
        adapter = ScrollVideoAdapter(postPicItemListener)
        adapter.submitList(videoAttachmentList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter

        tv_clean.isEnabled = true
        val img = requireContext().getDrawable(R.drawable.btn_close_n)
        tv_back.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)

        edt_hashtag.imeOptions = EditorInfo.IME_ACTION_DONE

        useAdultTheme(false)
    }

    override fun setupListeners() {
        super.setupListeners()

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
                val timeInMillisec: Long = time!!.toLong()
                retriever.release()
                val length = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(timeInMillisec),
                    TimeUnit.MILLISECONDS.toMinutes(timeInMillisec) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMillisec)),
                    TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec)))
                videoAttachmentList[0].length = length
            }

            val bundle = Bundle()
            bundle.putBoolean(UPLOAD_VIDEO, true)
            bundle.putParcelable(MEMBER_REQUEST, request)
            bundle.putParcelableArrayList(VIDEO_DATA, videoAttachmentList)
            bundle.putParcelableArrayList(DELETE_ATTACHMENT, deleteVideoList)
            bundle.putLong(POST_ID, postId)

            if (isEdit != null && isEdit) {
                val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                findNavController().navigate(R.id.action_postVideoFragment_to_myPostFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_postVideoFragment_to_adultHomeFragment, bundle)
            }
        }
    }


    override fun handleVideo() {
        val trimmerUri = arguments?.getString(BUNDLE_TRIMMER_URI)
        val picUri = arguments?.getString(BUNDLE_COVER_URI)
        val postVideoAttachment = PostVideoAttachment(videoUrl = trimmerUri!!, picUrl = picUri!!)
        videoAttachmentList.add(postVideoAttachment)
    }

    override fun setUI(item: MediaItem) {
        val trimmerUri = arguments?.getString(BUNDLE_TRIMMER_URI, "")
        val picUri = arguments?.getString(BUNDLE_COVER_URI, "")

        if (trimmerUri!!.isBlank()) {
            val postVideoAttachment = PostVideoAttachment(
                videoAttachmentId = item.videoParameter.id,
                length = item.videoParameter.length,
                picAttachmentId = item.picParameter[0].id,
                ext = item.picParameter[0].ext
            )
            videoAttachmentList.add(postVideoAttachment)

            haveMainTag = true
        } else {
            val postVideoAttachment = PostVideoAttachment(videoUrl = trimmerUri, picUrl = picUri!!)
            videoAttachmentList.add(postVideoAttachment)
        }
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

                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(requireContext(), myUri)
                    val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val timeInMillisec = time!!.toLong()

                    if (timeInMillisec > 3001) {
                        val isEdit = arguments?.getBoolean(MyPostFragment.EDIT)

                        if (isEdit != null && isEdit) {
                            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem

                            bundle.putBoolean(MyPostFragment.EDIT, true)
                            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                            findNavController().navigate(R.id.action_postVideoFragment_to_editVideoFragment2, bundle)

                        } else {
                            findNavController().navigate(R.id.action_postVideoFragment_to_editVideoFragment, bundle)
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.post_video_length_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
    }

    private fun openRecorder() {
        val galleryIntent = Intent()
        galleryIntent.type = "video/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT

        val cameraIntent = Intent()
        cameraIntent.action = MediaStore.ACTION_VIDEO_CAPTURE
        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, RECORD_LIMIT_TIME)
        cameraIntent.resolveActivity(requireContext().packageManager)

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent)
        chooser.putExtra(Intent.EXTRA_TITLE, requireContext().getString(R.string.post_select_pic))

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        startActivityForResult(chooser, REQUEST_VIDEO_CAPTURE)
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