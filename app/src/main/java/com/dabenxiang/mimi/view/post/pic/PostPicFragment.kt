package com.dabenxiang.mimi.view.post.pic

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.PostPicItemListener
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.adapter.ScrollPicAdapter
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.viewer.PostViewerFragment.Companion.VIEWER_DATA
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_post_article.chipGroup
import kotlinx.android.synthetic.main.fragment_post_article.edt_hashtag
import kotlinx.android.synthetic.main.fragment_post_article.edt_title
import kotlinx.android.synthetic.main.fragment_post_pic.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import java.io.File


class PostPicFragment : BasePostFragment() {

    private var file = File("")

    companion object {
        const val BUNDLE_PIC_URI = "bundle_pic_uri"
        const val UPLOAD_PIC = "upload_pic"
        const val MEMBER_REQUEST = "member_request"
        const val PIC_URI = "pic_uri"
        const val DELETE_ATTACHMENT = "delete_attachment"

        const val POST_ID = "post_id"

        private const val PHOTO_LIMIT = 20

        private const val INTENT_SELECT_IMG = 10001
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_pic
    }

    private var attachmentList = arrayListOf<PostAttachmentItem>()
    private var deletePicList = arrayListOf<String>()

    private lateinit var adapter: ScrollPicAdapter

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

    override fun handlePic() {
        val uriList = arguments?.getStringArrayList(BUNDLE_PIC_URI)!!
        for (uri in uriList) {
            val postAttachmentItem = PostAttachmentItem(uri = uri!!)
            attachmentList.add(postAttachmentItem)
        }
        txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
            PHOTO_LIMIT
        ))
    }

    override fun setUI(item: MediaItem) {
        for (pic in item.picParameter) {
            val postAttachmentItem = PostAttachmentItem()
            postAttachmentItem.attachmentId = pic.id
            postAttachmentItem.ext = pic.ext
            attachmentList.add(postAttachmentItem)
        }

        txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
            PHOTO_LIMIT
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
                    val uri = UriUtils.getPath(requireContext(), item.uri)
                    val uriDataList = adapter.getData()
                    val postAttachmentItem = PostAttachmentItem(uri = uri!!)
                    uriDataList.add(postAttachmentItem)
                }
                updateCountPicView()
            } else {
                val postAttachmentItem = PostAttachmentItem()
                var uri = Uri.parse("")

                if (data?.data == null) {
                    val extras = data?.extras

                    if (extras == null) {
                        rotateImage(BitmapFactory.decodeFile(file.absolutePath))
                        postAttachmentItem.uri = file.absolutePath
                    } else {
                        val extrasData = extras["data"]
                        val imageBitmap = extrasData as Bitmap?
                        uri = Uri.parse(MediaStore.Images.Media.insertImage(requireContext().contentResolver, imageBitmap, null,null))
                    }
                } else {
                    uri = data.data!!
                }

                if (uri.path!!.isNotBlank()) {
                    postAttachmentItem.uri = UriUtils.getPath(requireContext(), uri)!!
                }

                val uriDataList = adapter.getData()
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
        file = FileUtil.getTest(System.currentTimeMillis().toString() + ".jpg")

        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        galleryIntent.action = Intent.ACTION_GET_CONTENT

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".fileProvider", file)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

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

    private fun rotateImage(bitmap: Bitmap): Bitmap? {
        val ei = ExifInterface(file.absolutePath)

        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val rotatedBitmap: Bitmap?
        rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }

        return rotatedBitmap
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }
}