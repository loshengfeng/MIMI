package com.dabenxiang.mimi.view.home

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.holder.statisticsItemToCarouselHolderItem
import com.dabenxiang.mimi.model.holder.statisticsItemToVideoItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.model.serializable.SearchPostItem
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.model.vo.UploadPicItem
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
import com.dabenxiang.mimi.view.dialog.*
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.OnChooseUploadMethodDialogListener
import com.dabenxiang.mimi.view.home.HomeViewModel.Companion.TYPE_COVER
import com.dabenxiang.mimi.view.home.HomeViewModel.Companion.TYPE_PIC
import com.dabenxiang.mimi.view.home.HomeViewModel.Companion.TYPE_VIDEO
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
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
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import java.io.File

class AdultHomeFragment : BaseFragment() {

    private var lastPosition = 0
    private var uploadCurrentPicPosition = 0

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
    private var reportDialog: ReportDialogFragment? = null

    private var interactionListener: InteractionListener? = null
    private var uploadPicItem = arrayListOf<UploadPicItem>()
    private var uploadPicUri = arrayListOf<PostAttachmentItem>()
    private var uploadVideoUri = arrayListOf<PostVideoAttachment>()
    private var postMemberRequest = PostMemberRequest()

    private var snackBar: Snackbar? = null
    private var picParameter = PicParameter()

    companion object {
        private const val REQUEST_PHOTO = 10001
        private const val REQUEST_VIDEO_CAPTURE = 10002
    }

    override fun getLayoutId() = R.layout.fragment_home


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackStackData()
        showSnackBar()
    }

    override fun setupFirstTime() {
        requireActivity().onBackPressedDispatcher.addCallback {
            interactionListener?.changeNavigationPosition(
                R.id.navigation_home
            )
        }

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (GeneralUtils.getScreenSize(requireActivity()).second * 0.0245).toInt()

        setupUI()

        if (mainViewModel?.adult == null) {
            mainViewModel?.getHomeCategories()
        }

    }

    private fun handleBackStackData() {
        val isNeedPicUpload =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
                PostPicFragment.UPLOAD_PIC
            )
        val isNeedVideoUpload =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
                PostVideoFragment.UPLOAD_VIDEO
            )

        if (isNeedPicUpload?.value != null) {
            val memberRequest =
                findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<PostMemberRequest>(
                    PostPicFragment.MEMBER_REQUEST
                )
            val picUriList =
                findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<PostAttachmentItem>>(
                    PostPicFragment.PIC_URI
                )

            postMemberRequest = memberRequest!!.value!!

            uploadPicUri.addAll(picUriList!!.value!!)
            val pic = uploadPicUri[uploadCurrentPicPosition]
            viewModel.postAttachment(pic.uri, requireContext(), TYPE_PIC)
        } else if (isNeedVideoUpload?.value != null) {
            showSnackBar()

            val memberRequest =
                findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<PostMemberRequest>(
                    PostVideoFragment.MEMBER_REQUEST
                )
            uploadVideoUri =
                findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<PostVideoAttachment>>(
                    PostVideoFragment.VIDEO_DATA
                )?.value!!

            postMemberRequest = memberRequest!!.value!!
            viewModel.postAttachment(uploadVideoUri[0].picUrl, requireContext(), TYPE_COVER)
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
                    findNavController().navigate(R.id.action_adultHomeFragment_to_myPostFragment)
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

        viewModel.postReportResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                }
                is Error -> onApiError(it.throwable)
            }
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
                        viewModel.postPic(postMemberRequest, content)
                    } else {
                        val pic = uploadPicUri[uploadCurrentPicPosition]
                        viewModel.postAttachment(pic.uri, requireContext(), TYPE_PIC)
                    }
                }
                is Error -> {
                    resetAndCancelJob(it.throwable)
                }
            }
        })

        viewModel.postCoverResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    picParameter.id = it.result.toString()
                    viewModel.postAttachment(uploadVideoUri[0].videoUrl, requireContext(), TYPE_VIDEO)
                }
                is Error -> {
                    resetAndCancelJob(it.throwable)
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
                    Timber.d("Post video content item : $content")
                    viewModel.postPic(postMemberRequest, content)
                }
                is Error -> {
                    resetAndCancelJob(it.throwable)
                }
            }
        })

        viewModel.uploadPicItem.observe(viewLifecycleOwner, Observer {
            uploadPicItem.add(it)
        })

        viewModel.postVideoMemberResult.observe(viewLifecycleOwner, Observer {
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
                findNavController().navigate(R.id.action_adultHomeFragment_to_myPostFragment)
            }

            uploadCurrentPicPosition = 0
            uploadPicUri.clear()

            Handler().postDelayed({
                snackBar?.dismiss()
            }, 3000)
        })

        viewModel.uploadCoverItem.observe(viewLifecycleOwner, Observer {
            picParameter = it
        })

        viewModel.totalCountResult.observe(viewLifecycleOwner, Observer { totalCount ->
            takeIf { rv_sixth.visibility == View.VISIBLE }?.also { clubMemberAdapter.totalCount = totalCount }
        })
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
            ChooseUploadMethodDialogFragment.newInstance(onChooseUploadMethodDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    ChooseUploadMethodDialogFragment::class.java.simpleName
                )
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

        recyclerview_tab.adapter = tabAdapter
        setupRecyclerByPosition(0)

        refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
    }

    private fun setupRecyclerByPosition(position: Int) {

        rv_home.visibility = View.GONE
        rv_first.visibility = View.GONE
        rv_second.visibility = View.GONE
        rv_third.visibility = View.GONE
        rv_fourth.visibility = View.GONE
        rv_fifth.visibility = View.GONE
        rv_sixth.visibility = View.GONE

        when (position) {
            0 -> {
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
                }
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
                }
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
                }
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
                }
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
            { item, isLike, func -> likePost(item, isLike, func) }
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
            val bundle = ClipFragment.createBundle(ArrayList(item), position)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(item), position, true)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_clipFragment,
                    bundle
                )
            )
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
    }

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(requireContext(), getString(R.string.report_error))
            } else {
                reportDialog?.dismiss()
                when (item) {
                    is MemberPostItem -> viewModel.sendPostReport(item, content)
                }
            }
        }

        override fun onCancel() {
            reportDialog?.dismiss()
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem) {
            moreDialog?.dismiss()
            reportDialog = ReportDialogFragment.newInstance(item, onReportDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    ReportDialogFragment::class.java.simpleName
                )
            }
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

        override fun onVideoClick(view: View, item: PlayerData) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivity(intent)
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
            Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
                takeVideoIntent.resolveActivity(requireContext().packageManager)?.also {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
                }
            }
        }

        override fun onUploadPic() {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePicture, REQUEST_PHOTO)
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
                    data?.let {
                        val uriImage: Uri?

                        uriImage = if (it.data != null) {
                            it.data
                        } else {
                            val extras = it.extras
                            val imageBitmap = extras?.let { ex -> ex["data"] as Bitmap }
                            Uri.parse(
                                MediaStore.Images.Media.insertImage(
                                    requireContext().contentResolver,
                                    imageBitmap,
                                    null,
                                    null
                                )
                            )
                        }

                        val bundle = Bundle()
                        bundle.putString(BUNDLE_PIC_URI, uriImage.toString())

                        findNavController().navigate(
                            R.id.action_adultHomeFragment_to_postPicFragment,
                            bundle
                        )
                    }
                }

                REQUEST_VIDEO_CAPTURE -> {
                    val videoUri: Uri? = data?.data
                    val myUri =
                        Uri.fromFile(File(UriUtils.getPath(requireContext(), videoUri!!) ?: ""))

                    val bundle = Bundle()
                    bundle.putString(BUNDLE_VIDEO_URI, myUri.toString())
                    findNavController().navigate(
                        R.id.action_adultHomeFragment_to_editVideoFragment,
                        bundle
                    )
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
        viewModel.followMember(memberPostItem, isFollow, update)
    }

    private fun likePost(
        memberPostItem: MemberPostItem,
        isLike: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        viewModel.likePost(memberPostItem, isLike, update)
    }

    private fun clubFollow(
        memberClubItem: MemberClubItem,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        viewModel.clubFollow(memberClubItem, isFollow, update)
    }

    private fun resetAndCancelJob(t: Throwable) {
        onApiError(t)
        viewModel.cancelJob()
        snackBar?.dismiss()
        uploadCurrentPicPosition = 0
        uploadPicUri.clear()
        //TODO show error toast
    }
}