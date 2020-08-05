package com.dabenxiang.mimi.view.setting

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ImageUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.FilterDialogFragment
import com.dabenxiang.mimi.view.dialog.choosepicker.ChoosePickerDialogFragment
import com.dabenxiang.mimi.view.dialog.choosepicker.OnChoosePickerDialogListener
import com.dabenxiang.mimi.view.listener.OnDialogListener
import com.dabenxiang.mimi.view.updateprofile.UpdateProfileFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class SettingFragment : BaseFragment() {

    private val viewModel: SettingViewModel by viewModels()

    companion object {
        private const val REQUEST_CODE_CAMERA = 100
        private const val REQUEST_CODE_ALBUM = 200
        private const val KEY_PHOTO = "PHOTO"

        fun createBundle(byteArray: ByteArray) = Bundle().also {
            it.putSerializable(KEY_PHOTO, byteArray)
        }
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                is Empty -> viewModel.bitmap?.also { bitmap -> setupPhoto(bitmap) }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.isBinding.observe(this.viewLifecycleOwner, Observer { succeed ->




        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
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
                    showFilterDialog(
                        R.string.setting_choose,
                        R.array.filter_gender,
                        R.array.filter_gender_value,
                        viewModel.profileData?.gender ?: 0,
                        onDialogListener
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
        }
    }

    override fun initSettings() {
        viewModel.getProfile()
        arguments?.also { it ->
            val byteArray = it.getSerializable(KEY_PHOTO) as ByteArray
            byteArray.also {
                val bitmap = ImageUtils.bytes2Bitmap(it)
                setupPhoto(bitmap)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && (requestCode == REQUEST_CODE_CAMERA || requestCode == REQUEST_CODE_ALBUM)) {
            viewModel.bitmap = when (requestCode) {
                REQUEST_CODE_CAMERA -> data?.extras?.get("data") as Bitmap
                REQUEST_CODE_ALBUM -> {
                    val type = data?.data?.let { requireActivity().contentResolver.getType(it) }
                    if (type != null && type.startsWith("image")) {
                        MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            data.data
                        )
                    } else null
                }
                else -> null
            }
            viewModel.bitmap?.also { viewModel.postAttachment() }
        }
    }

    private fun showFilterDialog(
        titleId: Int,
        textArrayId: Int,
        valueArrayId: Int,
        selectedValue: Int,
        dialogListener: OnDialogListener
    ) {
        val dialog = FilterDialogFragment.newInstance(
            FilterDialogFragment.Content(
                titleId,
                textArrayId,
                valueArrayId,
                dialogListener,
                selectedValue
            )
        )
        dialog.show(
            requireActivity().supportFragmentManager,
            FilterDialogFragment::class.java.simpleName
        )
    }

    private val onDialogListener = object : OnDialogListener {
        override fun onItemSelected(value: Int, text: String) {
            viewModel.profileData?.gender = value
            viewModel.updateProfile()
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
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
        }
    }

    private fun openAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    getString(R.string.pls_choose_photo)
                ), REQUEST_CODE_ALBUM
            )
        }
    }

    private fun setupPhoto(bitmap: Bitmap) {
        val options: RequestOptions = RequestOptions()
            .transform(MultiTransformation(CenterCrop(), CircleCrop()))
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .priority(Priority.NORMAL)

        Glide.with(this).load(bitmap)
            .apply(options)
            .into(iv_photo)
    }
}