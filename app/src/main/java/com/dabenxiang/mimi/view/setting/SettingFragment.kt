package com.dabenxiang.mimi.view.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.choosepicker.ChoosePickerDialogFragment
import com.dabenxiang.mimi.view.dialog.choosepicker.OnChoosePickerDialogListener
import com.dabenxiang.mimi.view.dialog.editor.InvitationEditorDialog
import com.dabenxiang.mimi.view.listener.OnSimpleEditorDialogListener
import com.dabenxiang.mimi.view.updateprofile.UpdateProfileFragment
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import java.io.File

class SettingFragment : BaseFragment() {

    private val viewModel: SettingViewModel by viewModels()

    val file: File = FileUtil.getAvatarFile()

    companion object {
        private const val REQUEST_CODE_CAMERA = 100
        private const val REQUEST_CODE_ALBUM = 200
        private const val KEY_PHOTO = "PHOTO"
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_setting
    }

    override fun setupObservers() {
        viewModel.profileItem.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    tv_name.text = viewModel.profileData?.friendlyName
                    tv_email.text = viewModel.profileData?.email
                    tv_account.text = viewModel.profileData?.username
                    gender_info.text = getString(viewModel.profileData!!.getGenderRes())

                    val birthday = viewModel.profileData?.birthday ?: ""
                    if (!TextUtils.isEmpty(birthday)) {
                        birthday_info.text = birthday.split("T")[0]
                    }

                    var img: Drawable? = null

                    if (viewModel.isEmailConfirmed()) {
                        img = requireContext().resources.getDrawable(R.drawable.ico_checked)
                        btn_resend.visibility = View.GONE
                    } else {
                        btn_resend.visibility = View.VISIBLE
                    }

                    tv_email.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.imageBitmap.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    val options: RequestOptions = RequestOptions()
                        .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .priority(Priority.NORMAL)
                    Glide.with(this).load(it.result)
                        .apply(options)
                        .into(iv_photo)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.resendResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    GeneralUtils.showToast(
                        requireContext(),
                        getString(R.string.resend_mail_success)
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.updateResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    GeneralUtils.showToast(
                        requireContext(),
                        getString(R.string.gender_success)
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.postResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> viewModel.putAvatar(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.putResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.isBinding.observe(this.viewLifecycleOwner, Observer { success ->
            GeneralUtils.showToast(
                requireContext(), if (success)
                    getString(R.string.setting_binding_success) else
                    getString(R.string.setting_binding_failed)
            )
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> {
                    if (mainViewModel?.isFromPlayer == true)
                        activity?.onBackPressed()
                    else navigateTo(NavigateItem.Up)
                }
                R.id.btn_photo -> {
                    ChoosePickerDialogFragment.newInstance(onChoosePickerDialogListener).also {
                        it.show(
                            requireActivity().supportFragmentManager,
                            ChoosePickerDialogFragment::class.java.simpleName
                        )
                    }
                }
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
                R.id.btn_email -> {
                    navigateTo(
                        NavigateItem.Destination(R.id.updateProfileFragment,
                            viewModel.profileData?.let {
                                UpdateProfileFragment.createBundle(
                                    UpdateProfileFragment.TYPE_EMAIL,
                                    it
                                )
                            })
                    )
                }
                R.id.btn_resend -> viewModel.resendEmail()
                R.id.btn_chang_pw -> navigateTo(NavigateItem.Destination(R.id.action_settingFragment_to_changePasswordFragment))
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
                R.id.btn_binding_invitation -> {
                    showInvitationEditorDialog(requireContext())
                }
            }
        }.also {
            tv_back.setOnClickListener(it)
            btn_photo.setOnClickListener(it)
            btn_name.setOnClickListener(it)
            btn_email.setOnClickListener(it)
            btn_resend.setOnClickListener(it)
            btn_chang_pw.setOnClickListener(it)
            btn_gender.setOnClickListener(it)
            btn_birthday.setOnClickListener(it)
            btn_binding_invitation.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        Glide.with(this).load(R.drawable.default_profile_picture)
            .into(iv_photo)
        useAdultTheme(false)
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
            viewModel.bitmap?.also { viewModel.postAttachment() }
        }
    }

    private val onChoosePickerDialogListener = object : OnChoosePickerDialogListener {
        override fun onPickFromCamera() {
            openCamera()
        }

        override fun onPickFromAlbum() {
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
