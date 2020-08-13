package com.dabenxiang.mimi.view.home

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.*
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.club.ClubFuncItem
import com.dabenxiang.mimi.view.club.ClubMemberAdapter
import com.dabenxiang.mimi.view.club.MiMiLinearLayoutManager
import com.dabenxiang.mimi.view.clubdetail.ClubDetailFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.OnChooseUploadMethodDialogListener
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.home.HomeViewModel.Companion.TYPE_COVER
import com.dabenxiang.mimi.view.home.HomeViewModel.Companion.TYPE_PIC
import com.dabenxiang.mimi.view.home.HomeViewModel.Companion.TYPE_VIDEO
import com.dabenxiang.mimi.view.home.category.CategoriesFragment
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.post.article.PostArticleFragment
import com.dabenxiang.mimi.view.post.pic.PostPicFragment
import com.dabenxiang.mimi.view.post.pic.PostPicFragment.Companion.BUNDLE_PIC_URI
import com.dabenxiang.mimi.view.post.video.EditVideoFragment.Companion.BUNDLE_VIDEO_URI
import com.dabenxiang.mimi.view.post.video.PostVideoFragment
import com.dabenxiang.mimi.view.ranking.RankingFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.video.trimmer.utils.RealPathUtil
import com.vincent.videocompressor.VideoCompress
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class AdultHomeFragment : BaseFragment() {

    private var lastPosition = 0
    private var uploadCurrentPicPosition = 0
    private var postType = PostType.TEXT

    private val viewModel: HomeViewModel by viewModels()

    private val homeBannerViewHolderMap = hashMapOf<Int, HomeBannerViewHolder>()
    private val homeCarouselViewHolderMap = hashMapOf<Int, HomeCarouselViewHolder>()
    private val carouselMap = hashMapOf<Int, HomeTemplate.Carousel>()

    private val homeStatisticsViewHolderMap = hashMapOf<Int, HomeStatisticsViewHolder>()
    private val statisticsMap = hashMapOf<Int, HomeTemplate.Statistics>()

    private val homeClipViewHolderMap = hashMapOf<Int, HomeClipViewHolder>()
    private val homePictureViewHolderMap = hashMapOf<Int, HomePictureViewHolder>()
    private val homeClubViewHolderMap = hashMapOf<Int, HomeClubViewHolder>()

    private var moreDialog: MoreDialogFragment? = null

    private var interactionListener: InteractionListener? = null
    private var uploadPicItem = arrayListOf<UploadPicItem>()
    private var uploadPicUri = arrayListOf<PostAttachmentItem>()
    private var uploadVideoUri = arrayListOf<PostVideoAttachment>()
    private var postMemberRequest = PostMemberRequest()
    private val memberPostItem = MemberPostItem()

    private var snackBar: Snackbar? = null
    private var picParameter = PicParameter()

    val accountManager: AccountManager by inject()

    companion object {
        private const val REQUEST_PHOTO = 10001
        private const val REQUEST_VIDEO_CAPTURE = 10002
        private const val REQUEST_LOGIN = 10003

        private const val RECORD_LIMIT_TIME = 15
    }

    override fun getLayoutId() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackStackData()
        requireActivity().onBackPressedDispatcher.addCallback {
            interactionListener?.changeNavigationPosition(
                R.id.navigation_home
            )
        }

        useAdultTheme(true)
    }

    override fun setupFirstTime() {
        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (GeneralUtils.getScreenSize(requireActivity()).second * 0.0245).toInt()

        setupUI()

        if (mainViewModel?.adult == null) {
            mainViewModel?.getHomeCategories()
        }
    }

    private fun handleBackStackData() {
        val isNeedPicUpload = arguments?.getBoolean(PostPicFragment.UPLOAD_PIC)
        val isNeedVideoUpload = arguments?.getBoolean(PostVideoFragment.UPLOAD_VIDEO)
        val isNeedArticleUpload = arguments?.getBoolean(PostArticleFragment.UPLOAD_ARTICLE)

        if (isNeedPicUpload != null && isNeedPicUpload) {
            arguments?.remove(PostPicFragment.UPLOAD_PIC)
            showSnackBar()

            val memberRequest =
                arguments?.getParcelable<PostMemberRequest>(PostPicFragment.MEMBER_REQUEST)
            val picUriList =
                arguments?.getParcelableArrayList<PostAttachmentItem>(PostPicFragment.PIC_URI)

            postMemberRequest = memberRequest!!

            uploadPicUri.addAll(picUriList!!)
            val pic = uploadPicUri[uploadCurrentPicPosition]
            viewModel.postAttachment(pic.uri, requireContext(), TYPE_PIC)

            memberPostItem.title = memberRequest.title
            memberPostItem.tags = memberRequest.tags
            postType = PostType.IMAGE
        } else if (isNeedVideoUpload != null && isNeedVideoUpload) {
            arguments?.remove(PostVideoFragment.UPLOAD_VIDEO)
            showSnackBar()

            val memberRequest =
                arguments?.getParcelable<PostMemberRequest>(PostVideoFragment.MEMBER_REQUEST)
            uploadVideoUri = arguments?.getParcelableArrayList(PostVideoFragment.VIDEO_DATA)!!
            postMemberRequest = memberRequest!!
            memberPostItem.title = memberRequest.title
            memberPostItem.tags = memberRequest.tags
            viewModel.postAttachment(uploadVideoUri[0].picUrl, requireContext(), TYPE_COVER)
        } else if (isNeedArticleUpload != null && isNeedArticleUpload) {
            arguments?.remove(PostArticleFragment.UPLOAD_ARTICLE)
            showSnackBar()

            val title = arguments?.getString(PostArticleFragment.TITLE)
            val request = arguments?.getString(PostArticleFragment.REQUEST)
            val tags = arguments?.getStringArrayList(PostArticleFragment.TAG)

            memberPostItem.title = title!!
            memberPostItem.content = request!!
            memberPostItem.tags = tags

            viewModel.postArticle(title, request, tags!!)
        }
    }

    private fun showSnackBar() {
        snackBar = Snackbar.make(snackBarLayout, "", Snackbar.LENGTH_INDEFINITE)
        val snackBarLayout: Snackbar.SnackbarLayout = snackBar?.view as Snackbar.SnackbarLayout
        val textView = snackBarLayout.findViewById(R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE

        val snackView: View = layoutInflater.inflate(R.layout.snackbar_upload, null)
        snackBarLayout.addView(snackView, 0)
        snackBarLayout.setPadding(15, 0, 15, 0)
        snackBarLayout.setBackgroundColor(Color.TRANSPARENT)
        snackBar?.show()

        val imgCancel = snackBarLayout.findViewById(R.id.iv_cancel) as ImageView
        val txtCancel = snackBarLayout.findViewById(R.id.txt_cancel) as TextView

        txtCancel.setOnClickListener {
            cancelDialog()
        }

        imgCancel.setOnClickListener {
            cancelDialog()
        }
    }

    private fun cancelDialog() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.whether_to_discard_content,
                messageIcon = R.drawable.ico_default_photo,
                firstBtn = getString(R.string.btn_cancel),
                secondBtn = getString(R.string.btn_confirm),
                isMessageIcon = false,
                secondBlock = {
                    resetAndCancelJob()
                }
            )
        ).show(requireActivity().supportFragmentManager)
    }

    override fun setupObservers() {
        mainViewModel?.categoriesData?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> refresh.isRefreshing = true
                is Loaded -> refresh.isRefreshing = false
                is Success -> {
                    val list = mutableListOf<String>()
                    list.add(getString(R.string.home))
                    mainViewModel?.setupAdultCategoriesItem(it.result.content?.getAdult())
                    mainViewModel?.adult?.categories?.also { level2 ->
                        for (i in 0 until level2.count()) {
                            val detail = level2[i]
                            list.add(detail.name)
                        }
                        tabAdapter.submitList(list, lastPosition)
                        setupHomeData(mainViewModel?.adult)
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.getAdHomeResult?.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeBannerViewHolderMap[it.first]
                    viewHolder?.updateItem(response.result)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.showProgress.observe(viewLifecycleOwner, Observer { showProgress ->
            showProgress?.takeUnless { it }?.also { refresh.isRefreshing = it }
        })

        viewModel.videoList.observe(viewLifecycleOwner, Observer {
            videoListAdapter.submitList(it)
        })

        viewModel.carouselResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeCarouselViewHolderMap[it.first]
                    val carousel = carouselMap[it.first]
                    val carouselHolderItems =
                        response.result.content?.statisticsItemToCarouselHolderItem(carousel!!.isAdult)
                    viewHolder?.submitList(carouselHolderItems)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.videosResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Loaded -> {
                    val viewHolder = homeStatisticsViewHolderMap[it.first]
                    viewHolder?.hideProgressBar()
                }
                is Success -> {
                    val viewHolder = homeStatisticsViewHolderMap[it.first]
                    val statistics = statisticsMap[it.first]
                    val videoHolderItems =
                        response.result.content?.statisticsItemToVideoItem(statistics!!.isAdult)
                    viewHolder?.submitList(videoHolderItems)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.clipsResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Loaded -> {
                    val viewHolder = homeClipViewHolderMap[it.first]
                    viewHolder?.hideProgressBar()
                }
                is Success -> {
                    val viewHolder = homeClipViewHolderMap[it.first]
                    val memberPostItems = response.result.content ?: arrayListOf()
                    viewHolder?.submitList(memberPostItems)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.pictureResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Loaded -> {
                    val viewHolder = homePictureViewHolderMap[it.first]
                    viewHolder?.hideProgressBar()
                }
                is Success -> {
                    val viewHolder = homePictureViewHolderMap[it.first]
                    val memberPostItems = response.result.content ?: arrayListOf()
                    viewHolder?.submitList(memberPostItems)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.clubResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Loaded -> {
                    val viewHolder = homeClubViewHolderMap[it.first]
                    viewHolder?.hideProgressBar()
                }
                is Success -> {
                    val viewHolder = homeClubViewHolderMap[it.first]
                    val memberClubItems = response.result.content ?: arrayListOf()
                    viewHolder?.submitList(memberClubItems)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.postFollowItemListResult.observe(viewLifecycleOwner, Observer {
            followPostPagedAdapter.submitList(it)
        })

        viewModel.clipPostItemListResult.observe(viewLifecycleOwner, Observer {
            clipPostPagedAdapter.submitList(it)
        })

        viewModel.picturePostItemListResult.observe(viewLifecycleOwner, Observer {
            picturePostPagedAdapter.submitList(it)
        })

        viewModel.textPostItemListResult.observe(viewLifecycleOwner, Observer {
            textPostPagedAdapter.submitList(it)
        })

        viewModel.clubItemListResult.observe(viewLifecycleOwner, Observer {
            clubMemberAdapter.submitList(it)
        })

        viewModel.postPicResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    uploadPicItem[uploadCurrentPicPosition].id = it.result
                    uploadCurrentPicPosition += 1

                    if (uploadCurrentPicPosition > uploadPicUri.size - 1) {

                        val mediaItem = MediaItem()
                        mediaItem.textContent = postMemberRequest.content

                        for (item in uploadPicItem) {
                            mediaItem.picParameter.add(
                                PicParameter(
                                    id = item.id.toString(),
                                    ext = item.ext
                                )
                            )
                        }

                        val content = Gson().toJson(mediaItem)
                        Timber.d("Post pic content item : $content")
                        memberPostItem.content = content
                        viewModel.clearLiveDataValue()
                        viewModel.postPic(postMemberRequest, content)
                    } else {
                        val pic = uploadPicUri[uploadCurrentPicPosition]
                        viewModel.postAttachment(pic.uri, requireContext(), TYPE_PIC)
                    }
                }
                is Error -> {
                    resetAndCancelJob(it.throwable, getString(R.string.post_error))
                }
            }
        })

        viewModel.postCoverResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    picParameter.id = it.result.toString()
                    viewModel.clearLiveDataValue()
                    val realPath = UriUtils.getPath(requireContext(), Uri.parse(uploadVideoUri[0].videoUrl))
                    uploadVideoUri[0].videoUrl = realPath!!

                    val videoUri = Uri.parse(uploadVideoUri[0].videoUrl)
                    val file = File(videoUri.path ?: "")
                    val destinationPath = Environment.getExternalStorageDirectory().toString() + File.separator + "temp" + File.separator + "Videos" + File.separator
                    val root = File(destinationPath)
                    val outputFileUri = Uri.fromFile(File(root, "t_${Calendar.getInstance().timeInMillis}_" + file.nameWithoutExtension + ".mp4"))
                    val outPutPath = RealPathUtil.realPathFromUriApi19(requireContext(), outputFileUri)
                        ?: File(root, "t_${Calendar.getInstance().timeInMillis}_" + videoUri.path?.substring(videoUri.path!!.lastIndexOf("/") + 1)).absolutePath

                    VideoCompress.compressVideoLow(realPath, outPutPath , object : VideoCompress.CompressListener {
                        override fun onStart() {
                            Timber.d("Start compress")
                        }

                        override fun onSuccess() {
                            Timber.d("Compress success")
                            uploadVideoUri[0].videoUrl = outPutPath
                            viewModel.postAttachment(
                                uploadVideoUri[0].videoUrl,
                                requireContext(),
                                TYPE_VIDEO
                            )

                        }

                        override fun onFail() {
                            Timber.d("Compress fail")
                            resetAndCancelJob(Throwable(), getString(R.string.post_error))
                        }

                        override fun onProgress(percent: Float) {
                            Timber.d("Compress progress : $percent")
                        }
                    })
                }
                is Error -> {
                    resetAndCancelJob(it.throwable, getString(R.string.post_error))
                }
            }
        })

        viewModel.postVideoResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val mediaItem = MediaItem()
                    val videoParameter = VideoParameter(
                        id = it.result.toString(),
                        length = uploadVideoUri[0].length
                    )
                    mediaItem.picParameter.add(picParameter)
                    mediaItem.videoParameter = videoParameter
                    mediaItem.textContent = postMemberRequest.content
                    val content = Gson().toJson(mediaItem)
                    memberPostItem.content = content
                    Timber.d("Post video content item : $content")
                    viewModel.clearLiveDataValue()
                    viewModel.postPic(postMemberRequest, content)

                    postType = PostType.VIDEO
                }
                is Error -> {
                    resetAndCancelJob(it.throwable, getString(R.string.post_error))
                }
            }
        })

        viewModel.uploadPicItem.observe(viewLifecycleOwner, Observer {
            uploadPicItem.add(it)
        })

        viewModel.postVideoMemberResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> setSnackBarPostStatus(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.uploadCoverItem.observe(viewLifecycleOwner, Observer {
            picParameter = it
        })

        viewModel.totalCountResult.observe(viewLifecycleOwner, Observer {
            it?.also { totalCount ->
                cl_no_data.visibility = takeIf { totalCount > 0 }?.let { View.GONE } ?: let { View.VISIBLE }
                takeIf { rv_sixth.visibility == View.VISIBLE }?.also {
                    clubMemberAdapter.totalCount = totalCount
                }
                viewModel.clearLiveDataValue()
            }
        })

        viewModel.postArticleResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    postType = PostType.TEXT
                    setSnackBarPostStatus(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    private fun setSnackBarPostStatus(postId: Long = 0) {
        val snackBarLayout: Snackbar.SnackbarLayout = snackBar?.view as Snackbar.SnackbarLayout
        val progressBar =
            snackBarLayout.findViewById(R.id.contentLoadingProgressBar) as ContentLoadingProgressBar
        val imgSuccess = snackBarLayout.findViewById(R.id.iv_success) as ImageView

        val txtSuccess = snackBarLayout.findViewById(R.id.txt_postSuccess) as TextView
        val txtUploading = snackBarLayout.findViewById(R.id.txt_uploading) as TextView

        val imgCancel = snackBarLayout.findViewById(R.id.iv_cancel) as ImageView
        val txtCancel = snackBarLayout.findViewById(R.id.txt_cancel) as TextView
        val imgPost = snackBarLayout.findViewById(R.id.iv_viewPost) as ImageView
        val txtPost = snackBarLayout.findViewById(R.id.txt_viewPost) as TextView

        progressBar.visibility = View.GONE
        imgSuccess.visibility = View.VISIBLE

        txtSuccess.visibility = View.VISIBLE
        txtUploading.visibility = View.GONE

        imgCancel.visibility = View.GONE
        txtCancel.visibility = View.GONE

        imgPost.visibility = View.VISIBLE
        txtPost.visibility = View.VISIBLE

        imgPost.setOnClickListener {
            findNavController().navigate(R.id.action_adultHomeFragment_to_myPostFragment)
        }

        txtPost.setOnClickListener {
            when (postType) {
                PostType.TEXT -> {
                    memberPostItem.id = postId
                    val bundle = TextDetailFragment.createBundle(memberPostItem, -1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_textDetailFragment,
                            bundle
                        )
                    )
                }
                PostType.IMAGE -> {
                    memberPostItem.id = postId
                    memberPostItem.postFriendlyName = viewModel.pref.profileItem.account
                    memberPostItem.avatarAttachmentId = viewModel.pref.profileItem.avatarAttachmentId
                    val bundle = PictureDetailFragment.createBundle(memberPostItem, -1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                PostType.VIDEO -> {
                    memberPostItem.postFriendlyName = viewModel.pref.profileItem.account
                    val bundle = ClipFragment.createBundle(arrayListOf(memberPostItem), -1, false)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_clipFragment,
                            bundle
                        )
                    )
                }
            }
        }

        uploadCurrentPicPosition = 0
        uploadPicUri.clear()

        Handler().postDelayed({
            snackBar?.dismiss()
        }, 3000)
    }

    override fun setupListeners() {
        refresh.setOnRefreshListener {
            viewModel.lastListIndex = 0
            refresh.isRefreshing = true
            getData(lastPosition)
        }

        iv_bg_search.setOnClickListener {
            if (lastPosition == 0 || lastPosition == 1) {
                val bundle = SearchVideoFragment.createBundle()
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_homeFragment_to_searchVideoFragment,
                        bundle
                    )
                )
            } else {
                val item: SearchPostItem = when (lastPosition) {
                    2 -> SearchPostItem(isPostFollow = true)
                    3 -> SearchPostItem(type = PostType.VIDEO)
                    4 -> SearchPostItem(type = PostType.IMAGE)
                    5 -> SearchPostItem(type = PostType.TEXT)
                    else -> SearchPostItem(isClub = true)
                }
                val bundle = SearchPostFragment.createBundle(item)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_homeFragment_to_searchPostFragment,
                        bundle
                    )
                )
            }
        }

        iv_post.setOnClickListener {
            checkStatus {
                ChooseUploadMethodDialogFragment.newInstance(onChooseUploadMethodDialogListener)
                    .also {
                        it.show(
                            requireActivity().supportFragmentManager,
                            ChooseUploadMethodDialogFragment::class.java.simpleName
                        )
                    }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            interactionListener = context as InteractionListener
        } catch (e: ClassCastException) {
            Timber.e("AdultHomeFragment interaction listener can't cast")
        }
    }

    private fun setupUI() {
        layout_top.background = requireActivity().getDrawable(R.color.adult_color_status_bar)

        layout_search_bar.background = requireActivity().getDrawable(R.color.adult_color_background)
        iv_bg_search.setBtnSolidColor(requireActivity().getColor(R.color.adult_color_search_bar))

        iv_search.setImageResource(R.drawable.adult_btn_search)
        tv_search.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))

        btn_filter.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))
        btn_filter.setBtnSolidColor(
            requireActivity().getColor(R.color.color_white_1_30),
            requireActivity().getColor(R.color.color_red_1),
            resources.getDimension(R.dimen.dp_6)
        )

        iv_post.visibility = View.VISIBLE
        btn_filter.visibility = View.GONE
        btn_ranking.visibility = View.VISIBLE

        btn_ranking.setOnClickListener {
            val bundle = RankingFragment.createBundle()
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_rankingFragment,
                    bundle
                )
            )
        }

        btn_filter.setOnClickListener {
            val category = mainViewModel?.adult?.categories?.get(0)
            category?.also {
                val bundle = CategoriesFragment.createBundle(it.name, it.name, category)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_homeFragment_to_categoriesFragment,
                        bundle
                    )
                )
            }
        }

        recyclerview_tab.adapter = tabAdapter
        setupRecyclerByPosition(0)

        refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
    }

    private fun setupRecyclerByPosition(position: Int) {
        cl_no_data.visibility = View.GONE

        rv_home.visibility = View.GONE
        rv_first.visibility = View.GONE
        rv_second.visibility = View.GONE
        rv_third.visibility = View.GONE
        rv_fourth.visibility = View.GONE
        rv_fifth.visibility = View.GONE
        rv_sixth.visibility = View.GONE

        btn_ranking.visibility = View.GONE
        btn_filter.visibility = View.GONE

        when (position) {
            0 -> {
                btn_ranking.visibility = View.VISIBLE
                rv_home.visibility = View.VISIBLE
                takeIf { rv_home.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_home.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_home.layoutManager = LinearLayoutManager(requireContext())
                    rv_home.adapter = homeAdapter
                    mainViewModel?.getHomeCategories()
                }
            }
            1 -> {
                btn_filter.visibility = View.VISIBLE
                rv_first.visibility = View.VISIBLE
                takeIf { rv_first.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_first.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_first.layoutManager = GridLayoutManager(requireContext(), 2)
                    rv_first.adapter = videoListAdapter
                    viewModel.getVideos(null, true)
                }
            }
            2 -> {
                rv_second.visibility = View.VISIBLE
                takeIf { rv_second.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_second.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_second.layoutManager = LinearLayoutManager(requireContext())
                    rv_second.adapter = followPostPagedAdapter
                    viewModel.getPostFollows()
                }?: run {
                    Timber.d("@@followPostPagedAdapter.itemCount ${followPostPagedAdapter.itemCount}")
                    cl_no_data.visibility = followPostPagedAdapter.currentList.takeUnless { isListEmpty(it) }?.let { View.GONE } ?: let { View.VISIBLE }
                }
            }
            3 -> {
                rv_third.visibility = View.VISIBLE
                takeIf { rv_third.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_third.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_third.layoutManager = LinearLayoutManager(requireContext())
                    rv_third.adapter = clipPostPagedAdapter
                    viewModel.getClipPosts()
                }?: run { cl_no_data.visibility = clipPostPagedAdapter.currentList.takeUnless { isListEmpty(it) }?.let { View.GONE } ?: let { View.VISIBLE } }
            }
            4 -> {
                rv_fourth.visibility = View.VISIBLE
                takeIf { rv_fourth.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_fourth.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_fourth.layoutManager = LinearLayoutManager(requireContext())
                    rv_fourth.adapter = picturePostPagedAdapter
                    viewModel.getPicturePosts()
                }?: run { cl_no_data.visibility = picturePostPagedAdapter.currentList.takeUnless { isListEmpty(it) }?.let { View.GONE } ?: let { View.VISIBLE } }
            }
            5 -> {
                rv_fifth.visibility = View.VISIBLE
                takeIf { rv_fifth.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_fifth.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_fifth.layoutManager = LinearLayoutManager(requireContext())
                    rv_fifth.adapter = textPostPagedAdapter
                    viewModel.getTextPosts()
                }?: run { cl_no_data.visibility = textPostPagedAdapter.currentList.takeUnless { isListEmpty(it) }?.let { View.GONE } ?: let { View.VISIBLE } }
            }
            else -> {
                rv_sixth.visibility = View.VISIBLE
                takeIf { rv_sixth.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_sixth.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_sixth.layoutManager = MiMiLinearLayoutManager(requireContext())
                    rv_sixth.adapter = clubMemberAdapter
                    viewModel.getClubs()
                }?: run { cl_no_data.visibility = clubMemberAdapter.currentList.takeUnless { isClubListEmpty(it) }?.let { View.GONE } ?: let { View.VISIBLE } }
            }
        }
    }

    private fun getData(position: Int) {
        when (position) {
            0 -> mainViewModel?.getHomeCategories()
            1 -> viewModel.getVideos(null, true)
            2 -> viewModel.getPostFollows()
            3 -> viewModel.getClipPosts()
            4 -> viewModel.getPicturePosts()
            5 -> viewModel.getTextPosts()
            6 -> viewModel.getClubs()
        }
    }

    private fun setupHomeData(root: CategoriesItem?) {
        val templateList = mutableListOf<HomeTemplate>()

        templateList.add(HomeTemplate.Banner(AdItem()))
        templateList.add(HomeTemplate.Carousel(true))

        if (root?.categories != null) {
            for (item in root.categories) {
                if (item.name == "关注" || item.name == "短文") continue
                templateList.add(HomeTemplate.Header(null, item.name, item.name))
                when (item.name) {
                    "蜜蜜影视" -> templateList.add(HomeTemplate.Statistics(item.name, null, true))
                    "短视频" -> templateList.add(HomeTemplate.Clip())
                    "图片" -> templateList.add(HomeTemplate.Picture())
                    "圈子" -> templateList.add(HomeTemplate.Club())
                }
            }
        }
        homeAdapter.submitList(templateList)
    }

    private fun setTab(index: Int) {
        lastPosition = index
        tabAdapter.setLastSelectedIndex(lastPosition)
        recyclerview_tab.scrollToPosition(index)
        setupRecyclerByPosition(index)
    }

//    private fun updateAd(item: AdItem) {
//        when (lastPosition) {
//            2 -> {
//                followPostPagedAdapter.setupAdItem(item)
//                followPostPagedAdapter.notifyItemChanged(0)
//            }
//            3 -> {
//                clipPostPagedAdapter.setupAdItem(item)
//                clipPostPagedAdapter.notifyItemChanged(0)
//            }
//            4 -> {
//                picturePostPagedAdapter.setupAdItem(item)
//                picturePostPagedAdapter.notifyItemChanged(0)
//            }
//            5 -> {
//                textPostPagedAdapter.setupAdItem(item)
//                textPostPagedAdapter.notifyItemChanged(0)
//            }
//            6 -> {
//                clubMemberAdapter.setupAdItem(item)
//                clubMemberAdapter.notifyItemChanged(0)
//            }
//        }
//    }

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTab(index)
            }
        }, true)
    }

    private val homeAdapter by lazy {
        HomeAdapter(
            requireContext(),
            adapterListener,
            true,
            memberPostFuncItem,
            clubFuncItem
        )
    }

    private val followPostPagedAdapter by lazy {
        MemberPostPagedAdapter(requireActivity(), adultListener, "", memberPostFuncItem)
    }

    private val clipPostPagedAdapter by lazy {
        MemberPostPagedAdapter(requireActivity(), adultListener, "", memberPostFuncItem, true)
    }

    private val picturePostPagedAdapter by lazy {
        MemberPostPagedAdapter(requireActivity(), adultListener, "", memberPostFuncItem)
    }

    private val textPostPagedAdapter by lazy {
        MemberPostPagedAdapter(requireActivity(), adultListener, "", memberPostFuncItem)
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
            {},
            { id, func -> getBitmap(id, func) },
            { item, isFollow, func -> followMember(item, isFollow, func) },
            { item, isLike, func -> likePost(item, isLike, func) },
            { item, isFavorite, func -> favoritePost(item, isFavorite, func) }
        )
    }

    private val clubFuncItem by lazy {
        ClubFuncItem(
            { item -> onItemClick(item) },
            { id, function -> getBitmap(id, function) },
            { item, isFollow, function -> clubFollow(item, isFollow, function) })
    }

    private val clubMemberAdapter by lazy {
        ClubMemberAdapter(
            requireContext(),
            clubFuncItem
        )
    }

    private val videoListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, true)
    }

    private val adultListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            //replace by closure
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            //replace by closure
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = PictureDetailFragment.createBundle(item, 2)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_adultHomeFragment_to_pictureDetailFragment,
                                bundle
                            )
                        )
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 2)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_adultHomeFragment_to_textDetailFragment,
                                bundle
                            )
                        )
                    }
                    AdultTabType.CLIP -> {
                        val bundle = ClipFragment.createBundle(arrayListOf(item), 0, true)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_adultHomeFragment_to_clipFragment,
                                bundle
                            )
                        )
                    }
                    else -> {
                    }
                }
            }
        }

        override fun onMoreClick(item: MemberPostItem) {
            moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    MoreDialogFragment::class.java.simpleName
                )
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_textDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipFragment.createBundle(arrayListOf(item), 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_clipFragment,
                            bundle
                        )
                    )
                }
                else -> {
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {

            val bundle = ClipFragment.createBundle(ArrayList(item.subList(1, item.size)), position-1)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(item), position, true)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_adultHomeFragment_to_clipFragment,
                        bundle
                    )
                )
            }
        }

        override fun onChipClick(type: PostType, tag: String) {
            val item = when (lastPosition) {
                2 -> SearchPostItem(type, tag, true)
                else -> SearchPostItem(type, tag)
            }

            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_searchPostFragment,
                    bundle
                )
            )
        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                userId, name,
                isAdult = true,
                isAdultTheme = true
            )
            navigateTo(NavigateItem.Destination(R.id.action_adultHomeFragment_to_navigation_my_post, bundle))
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem) {
            moreDialog?.dismiss()
            checkStatus { (requireActivity() as MainActivity).showReportDialog(item) }
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            val position = when (item.title) {
                "蜜蜜影视" -> 1
                "短视频" -> 3
                "图片" -> 4
                "圈子" -> 6
                else -> 1
            }
            setTab(position)
        }

        override fun onVideoClick(view: View, item: PlayerItem) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivityForResult(intent, REQUEST_LOGIN)
        }

        override fun onClipClick(view: View, item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(item), position)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onPictureClick(view: View, item: MemberPostItem) {
            val bundle = PictureDetailFragment.createBundle(item, 0)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_pictureDetailFragment,
                    bundle
                )
            )
        }

        override fun onClubClick(view: View, item: MemberClubItem) {

        }

        override fun onLoadBannerViewHolder(vh: HomeBannerViewHolder) {
            homeBannerViewHolderMap[vh.adapterPosition] = vh
            mainViewModel?.getAd(
                vh.adapterPosition,
                viewModel.adWidth,
                viewModel.adHeight
            )
        }

        override fun onLoadStatisticsViewHolder(
            vh: HomeStatisticsViewHolder,
            src: HomeTemplate.Statistics
        ) {
            homeStatisticsViewHolderMap[vh.adapterPosition] = vh
            statisticsMap[vh.adapterPosition] = src
            viewModel.loadNestedStatisticsList(vh.adapterPosition, src)
        }

        override fun onLoadCarouselViewHolder(
            vh: HomeCarouselViewHolder,
            src: HomeTemplate.Carousel
        ) {
            homeCarouselViewHolderMap[vh.adapterPosition] = vh
            carouselMap[vh.adapterPosition] = src
            viewModel.loadNestedStatisticsListForCarousel(vh.adapterPosition, src)
        }

        override fun onLoadClipViewHolder(vh: HomeClipViewHolder) {
            homeClipViewHolderMap[vh.adapterPosition] = vh
            viewModel.loadNestedClipList(vh.adapterPosition)
        }

        override fun onLoadPictureViewHolder(vh: HomePictureViewHolder) {
            homePictureViewHolderMap[vh.adapterPosition] = vh
            viewModel.loadNestedPictureList(vh.adapterPosition)
        }

        override fun onLoadClubViewHolder(vh: HomeClubViewHolder) {
            homeClubViewHolderMap[vh.adapterPosition] = vh
            viewModel.loadNestedClubList(vh.adapterPosition)
        }
    }

    private val onChooseUploadMethodDialogListener = object : OnChooseUploadMethodDialogListener {
        override fun onUploadVideo() {
            val galleryIntent = Intent()
            galleryIntent.type = "video/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT

            val cameraIntent = Intent()
            cameraIntent.action = MediaStore.ACTION_VIDEO_CAPTURE
            cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, RECORD_LIMIT_TIME)
            cameraIntent.resolveActivity(requireContext().packageManager)

            val chooser = Intent(Intent.ACTION_CHOOSER)
            chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent)
            chooser.putExtra(Intent.EXTRA_TITLE, requireContext().getString(R.string.post_select_video))

            val intentArray = arrayOf(cameraIntent)
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            startActivityForResult(chooser, REQUEST_VIDEO_CAPTURE)
        }

        override fun onUploadPic() {
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
            startActivityForResult(chooser, REQUEST_PHOTO)
        }

        override fun onUploadArticle() {
            findNavController().navigate(R.id.action_adultHomeFragment_to_postArticleFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_PHOTO -> {
                    val pciUri = arrayListOf<String>()

                    val clipData = data?.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val item = clipData.getItemAt(i)
                            val uri = item.uri
                            pciUri.add(uri.toString())
                        }
                    } else {
                        val uri = if (data?.data == null) {
                            val extras = data?.extras
                            val imageBitmap = extras!!["data"] as Bitmap?
                            Uri.parse(MediaStore.Images.Media.insertImage(requireContext().contentResolver, imageBitmap, null,null))
                        } else {
                            data.data!!
                        }

                        pciUri.add(uri.toString())
                    }
                        val bundle = Bundle()
                        bundle.putStringArrayList(BUNDLE_PIC_URI, pciUri)

                        findNavController().navigate(
                            R.id.action_adultHomeFragment_to_postPicFragment,
                            bundle
                        )
                }

                REQUEST_VIDEO_CAPTURE -> {

                    val videoUri: Uri? = data?.data
                    val myUri =
                        Uri.fromFile(File(UriUtils.getPath(requireContext(), videoUri!!) ?: ""))

                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(requireContext(), myUri)
                    val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val timeInMillisec = time.toLong()

                    if (timeInMillisec in 3001..149999) {
                        val bundle = Bundle()
                        bundle.putString(BUNDLE_VIDEO_URI, myUri.toString())
                        findNavController().navigate(R.id.action_adultHomeFragment_to_editVideoFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), R.string.post_video_length_error, Toast.LENGTH_SHORT).show()
                    }
                }

                REQUEST_LOGIN -> {
                    findNavController().navigate(R.id.action_to_loginFragment, data?.extras)
                }
            }
        }
    }

    private fun onItemClick(item: MemberClubItem) {
        val bundle = ClubDetailFragment.createBundle(item)
        findNavController().navigate(R.id.action_adultHomeFragment_to_clubDetailFragment, bundle)
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
    }

    private fun followMember(
        memberPostItem: MemberPostItem,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.followMember(memberPostItem, isFollow, update) }
    }

    private fun likePost(
        memberPostItem: MemberPostItem,
        isLike: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        checkStatus { viewModel.likePost(memberPostItem, isLike, update) }
    }

    private fun favoritePost(
        memberPostItem: MemberPostItem,
        isFavorite: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        checkStatus { viewModel.favoritePost(memberPostItem, isFavorite, update) }
    }

    private fun clubFollow(
        memberClubItem: MemberClubItem,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.clubFollow(memberClubItem, isFollow, update) }
    }

    private fun resetAndCancelJob(t: Throwable = Throwable(), msg: String = "") {
        onApiError(t)
        viewModel.cancelJob()
        snackBar?.dismiss()
        uploadCurrentPicPosition = 0
        uploadPicUri.clear()
        Timber.e(t)

        if (msg.isNotBlank()) {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun isListEmpty(list: PagedList<MemberPostItem>?): Boolean {
        return list == null || list.size == 0 || (list.size == 1 && list[0]?.adItem != null)
    }

    private fun isClubListEmpty(list: PagedList<MemberClubItem>?): Boolean {
        return list == null || list.size == 0 || (list.size == 1 && list[0]?.adItem != null)
    }
}