package com.dabenxiang.mimi.view.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.choosepicker.ChoosePickerDialogFragment
import com.dabenxiang.mimi.view.dialog.choosepicker.OnChoosePickerDialogListener
import com.dabenxiang.mimi.view.dialog.editor.InvitationEditorDialog
import com.dabenxiang.mimi.view.listener.OnSimpleEditorDialogListener
import com.dabenxiang.mimi.view.updateprofile.UpdateProfileFragment
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_setting_v2.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber
import java.io.File

class SettingFragment : BaseFragment() {

    companion object {
        private const val REQUEST_CODE_CAMERA = 100
        private const val REQUEST_CODE_ALBUM = 200
    }

    private val viewModel: SettingViewModel by viewModels()

    val file: File = FileUtil.getAvatarFile()

    private var avatarId: Long? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_setting_v2
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.profileItem.observe(this, {
            when (it) {
                is Success -> {
                    if(viewModel.isBindPhone()) {
                        tv_account_phone.text = viewModel.profileData?.username
                        tv_account_phone.setTextColor(context.getColor(R.color.color_black_1))
                    } else {
                        tv_account_phone.text = getString(R.string.setting_bind_phone)
                        tv_account_phone.setTextColor(context.getColor(R.color.color_blue_2))
                    }
                    tv_name.text = viewModel.profileData?.friendlyName
                    tv_account_phone.visibility =View.VISIBLE
                    gender_info.text = getString(viewModel.profileData!!.getGenderRes())

                    val birthday = viewModel.profileData?.birthday ?: ""
                    if (!TextUtils.isEmpty(birthday)) {
                        birthday_info.text = birthday.split("T")[0]
                    }

                    viewModel.loadImage(
                            viewModel.profileData?.avatarAttachmentId,
                            iv_photo,
                            LoadImageType.AVATAR
                    )

                    avatarId = viewModel.profileData?.avatarAttachmentId
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }
    override fun setupObservers() {


        viewModel.resendResult.observe(viewLifecycleOwner, {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(
                        requireContext(),
                        getString(R.string.resend_mail_success)
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.updateResult.observe(viewLifecycleOwner, {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(
                        requireContext(),
                        getString(R.string.gender_success)
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.postResult.observe(viewLifecycleOwner, {
            when (it) {
                is Success -> viewModel.putAvatar(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.putResult.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    viewModel.loadImage(it.result, iv_photo, LoadImageType.AVATAR)

                    avatarId?.let {
                        viewModel.deleteAttachment(it.toString())
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.isBinding.observe(this.viewLifecycleOwner, { success ->
            val result = if (success) getString(R.string.setting_binding_success)
            else getString(R.string.setting_binding_failed)
            GeneralUtils.showToast(requireContext(), result)
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> {
                    if (mainViewModel?.isFromPlayer == true) activity?.onBackPressed()
                    else navigateTo(NavigateItem.Up)
                }
                R.id.view_field_photo,
                R.id.btn_photo -> {
                    ChoosePickerDialogFragment.newInstance(onChoosePickerDialogListener).also {
                        it.show(
                            requireActivity().supportFragmentManager,
                            ChoosePickerDialogFragment::class.java.simpleName
                        )
                    }
                }
                R.id.view_field_name,
                R.id.btn_name -> {
                    navigateTo(
                        NavigateItem.Destination(R.id.updateProfileFragment,
                            viewModel.profileData?.let {
                                UpdateProfileFragment.createBundle(
                                    UpdateProfileFragment.TYPE_NAME,
                                    it
                                )
                            })
                    )
                }
                R.id.btn_chang_pw -> navigateTo(NavigateItem.Destination(R.id.action_settingFragment_to_changePasswordFragment))

                R.id.view_field_gender,
                R.id.btn_gender -> {
                    navigateTo(
                        NavigateItem.Destination(R.id.updateProfileFragment,
                            viewModel.profileData?.let {
                                UpdateProfileFragment.createBundle(
                                    UpdateProfileFragment.TYPE_GEN,
                                    it
                                )
                            })
                    )
                }

                R.id.view_field_birthday,
                R.id.btn_birthday -> {
                    navigateTo(
                        NavigateItem.Destination(R.id.updateProfileFragment,
                            viewModel.profileData?.let {
                                UpdateProfileFragment.createBundle(
                                    UpdateProfileFragment.TYPE_BIRTHDAY,
                                    it
                                )
                            })
                    )
                }
                R.id.tv_account_phone -> {
                    if(!viewModel.isBindPhone()) {
                        navigateTo(NavigateItem.Destination(R.id.action_to_loginFragment))
                    }
                }
//                R.id.btn_binding_invitation -> {
//                    showInvitationEditorDialog(requireContext())
//                }
            }
        }.also {
            tv_back.setOnClickListener(it)
            view_field_photo.setOnClickListener(it)
            btn_photo.setOnClickListener(it)
            btn_name.setOnClickListener(it)
            btn_chang_pw.setOnClickListener(it)
            btn_gender.setOnClickListener(it)
            btn_birthday.setOnClickListener(it)
            view_field_name.setOnClickListener(it)
            view_field_birthday.setOnClickListener(it)
            view_field_gender.setOnClickListener(it)
            tv_account_phone.setOnClickListener(it)
//            btn_binding_invitation.setOnClickListener(it)
        }

    }

    override fun initSettings() {
        Glide.with(this).load(R.drawable.default_profile_picture).into(iv_photo)
        viewModel.getProfile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && (requestCode == REQUEST_CODE_CAMERA || requestCode == REQUEST_CODE_ALBUM)) {
            viewModel.bitmap = when (requestCode) {
                REQUEST_CODE_CAMERA -> {
                    rotateImage(BitmapFactory.decodeFile(file.absolutePath))
                }
                REQUEST_CODE_ALBUM -> {
                    val type = data?.data?.let { requireActivity().contentResolver.getType(it) }
                    data?.data?.takeIf { type?.startsWith("image") == true }?.let {
                        if (android.os.Build.VERSION.SDK_INT >= 29) {
                            val source =
                                ImageDecoder.createSource(requireContext().contentResolver, it)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
                        }
                    }
                }
                else -> null
            }
            viewModel.bitmap?.also {
                val fileName = viewModel.getFileName()
                val tempImagePath = App.self.getExternalFilesDir(null)?.path
                    .plus(StringBuffer("/").append(fileName))
                viewModel.postAttachment(fileName, tempImagePath)
            }
        }
    }

    private val onChoosePickerDialogListener = object : OnChoosePickerDialogListener {
        override fun onPickFromCamera() {
            requestPermissions(PERMISSION_CAMERA_REQUEST_CODE)
        }

        override fun onPickFromAlbum() {
            requestPermissions(PERMISSION_EXTERNAL_REQUEST_CODE)
        }
    }

    private fun requestPermissions(type: Int) {
        val requestList = getNotGrantedPermissions(
            if (type == PERMISSION_CAMERA_REQUEST_CODE) cameraPermissions
            else externalPermissions
        )

        if (requestList.size > 0) {
            requestPermissions(requestList.toTypedArray(), type)
        } else {
            if (type == PERMISSION_CAMERA_REQUEST_CODE) openCamera()
            else openAlbum()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE
            && getNotGrantedPermissions(cameraPermissions).isEmpty()
        ) {
            openCamera()
        } else if (requestCode == PERMISSION_EXTERNAL_REQUEST_CODE
            && getNotGrantedPermissions(externalPermissions).isEmpty()
        ) {
            openAlbum()
        }
    }


    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileProvider",
                    file
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA)
            }
        }
    }

    private fun openAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.resolveActivity(requireContext().packageManager)?.also {
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    getString(R.string.pls_choose_photo)
                ), REQUEST_CODE_ALBUM
            )
        }
    }

    private fun setupPhoto(bitmap: Bitmap) {
        Glide.with(this)
            .load(bitmap)
            .circleCrop()
            .placeholder(R.drawable.default_profile_picture)
            .error(R.drawable.default_profile_picture)
            .priority(Priority.NORMAL)
            .into(iv_photo)
    }

    private fun showInvitationEditorDialog(context: Context) {
        InvitationEditorDialog(
            context,
            R.string.setting_enter_invitation,
            R.string.btn_confirm,
            R.string.btn_cancel,
            object : OnSimpleEditorDialogListener {
                override fun onConfirm(text: String) {
                    viewModel.bindingInvitationCodes(context, text)
                }

                override fun onCancel() {}

            }
        ).show()
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
