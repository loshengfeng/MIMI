package com.dabenxiang.mimi.view.home

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.holder.statisticsItemToCarouselHolderItem
import com.dabenxiang.mimi.model.holder.statisticsItemToVideoItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.model.serializable.SearchPostItem
import com.dabenxiang.mimi.model.vo.UploadPicItem
import com.dabenxiang.mimi.view.adapter.*
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.TextPostHolder
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.club.ClubFuncItem
import com.dabenxiang.mimi.view.club.ClubMemberAdapter
import com.dabenxiang.mimi.view.clubdetail.ClubDetailFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.OnChooseUploadMethodDialogListener
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.post.pic.PostPicFragment
import com.dabenxiang.mimi.view.post.pic.PostPicFragment.Companion.BUNDLE_PIC_URI
import com.dabenxiang.mimi.view.post.video.EditVideoFragment.Companion.BUNDLE_VIDEO_URI
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.putLruCache
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
    private var uploadPicUri = arrayListOf<String>()
    private var postMemberRequest = PostMemberRequest()

    private var snackbar: Snackbar? = null

    companion object {
        private const val REQUEST_PHOTO = 10001
        private const val REQUEST_VIDEO_CAPTURE = 10002
    }

    override fun getLayoutId() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback {
            interactionListener?.changeNavigationPosition(
                R.id.navigation_home
            )
        }

        setupUI()

        if (mainViewModel?.adult == null) {
            mainViewModel?.getHomeCategories()
        }

        val isNeedUpload =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
                PostPicFragment.UPLOAD_PIC
            )
        val memberRequest =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<PostMemberRequest>(
                PostPicFragment.MEMBER_REQUEST
            )
        val picUriList =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<String>>(
                PostPicFragment.PIC_URI
            )
        if (isNeedUpload?.value != null) {
            snackbar = Snackbar.make(testView, "", Snackbar.LENGTH_INDEFINITE)
            val snackbarLayout: Snackbar.SnackbarLayout = snackbar?.view as Snackbar.SnackbarLayout
            val textView = snackbarLayout.findViewById(R.id.snackbar_text) as TextView
            textView.visibility = View.INVISIBLE

            val snackView: View = layoutInflater.inflate(R.layout.snackbar_upload, null)
            snackbarLayout.addView(snackView, 0)
            snackbarLayout.setPadding(15, 0, 15, 0)
            snackbarLayout.setBackgroundColor(Color.TRANSPARENT)
            snackbar?.show()

            postMemberRequest = memberRequest!!.value!!

            uploadPicUri.addAll(picUriList!!.value!!)
            val pic = uploadPicUri[uploadCurrentPicPosition]
            viewModel.postAttachment(pic, requireContext())
        }
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
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.tabLayoutPosition.observe(viewLifecycleOwner, Observer { position ->
            lastPosition = position
            tabAdapter.setLastSelectedIndex(lastPosition)
            recyclerview_tab.scrollToPosition(position)
            setupRecyclerByPosition(position)
            getData(position)
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
                is Error -> Timber.e(response.throwable)
            }
        })

        viewModel.videosResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeStatisticsViewHolderMap[it.first]
                    val statistics = statisticsMap[it.first]
                    val videoHolderItems =
                        response.result.content?.statisticsItemToVideoItem(statistics!!.isAdult)
                    viewHolder?.submitList(videoHolderItems)
                }
                is Error -> Timber.e(response.throwable)
            }
        })

        viewModel.clipsResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeClipViewHolderMap[it.first]
                    val memberPostItems = response.result.content ?: arrayListOf()
                    viewHolder?.submitList(memberPostItems)
                }
                is Error -> Timber.e(response.throwable)
            }
        })

        viewModel.pictureResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homePictureViewHolderMap[it.first]
                    val memberPostItems = response.result.content ?: arrayListOf()
                    viewHolder?.submitList(memberPostItems)
                }
                is Error -> Timber.e(response.throwable)
            }
        })

        viewModel.clubResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeClubViewHolderMap[it.first]
                    val memberClubItems = response.result.content ?: arrayListOf()
                    viewHolder?.submitList(memberClubItems)
                }
                is Error -> Timber.e(response.throwable)
            }
        })

        viewModel.followClubResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when (val holder = homeAdapter.functionViewHolderMap[FunctionType.FOLLOW]) {
                        is HomeClubViewHolder -> {
                            holder.updateItemByFollow(it.result.first, it.result.second)
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.followPostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when (memberPostPagedAdapter.viewHolderMap[it.result]) {
                        is ClipPostHolder,
                        is PicturePostHolder,
                        is TextPostHolder -> {
                            memberPostPagedAdapter.notifyItemChanged(
                                it.result,
                                MemberPostPagedAdapter.PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI
                            )
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when (memberPostPagedAdapter.viewHolderMap[it.result]) {
                        is ClipPostHolder,
                        is PicturePostHolder,
                        is TextPostHolder -> {
                            memberPostPagedAdapter.notifyItemChanged(
                                it.result,
                                MemberPostPagedAdapter.PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI
                            )
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.attachmentByTypeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val attachmentItem = it.result
                    putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)

                    when (attachmentItem.type) {
                        AttachmentType.ADULT_HOME_CLIP -> {
                            homeAdapter.attachmentViewHolderMap[attachmentItem.type]?.also { holder ->
                                holder as HomeClipViewHolder
                                holder.updateItem(attachmentItem.position!!)
                            }
                        }
                        AttachmentType.ADULT_HOME_PICTURE -> {
                            homeAdapter.attachmentViewHolderMap[attachmentItem.type]?.also { holder ->
                                holder as HomePictureViewHolder
                                holder.updateItem(attachmentItem.position!!)
                            }
                        }
                        AttachmentType.ADULT_HOME_CLUB -> {
                            homeAdapter.attachmentViewHolderMap[attachmentItem.type]?.also { holder ->
                                holder as HomeClubViewHolder
                                holder.updateItem(attachmentItem.position!!)
                            }
                        }
                        AttachmentType.ADULT_TAB_CLIP,
                        AttachmentType.ADULT_TAB_PICTURE,
                        AttachmentType.ADULT_TAB_TEXT -> {
                            memberPostPagedAdapter.notifyItemChanged(attachmentItem.position!!)
                        }
                        else -> {
                        }
                    }

                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.scrollToLastPosition.observe(viewLifecycleOwner, Observer { result ->
            if (result) recyclerview.scrollToPosition(viewModel.lastListIndex)
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val attachmentItem = it.result
                    putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    when (val holder =
                        memberPostPagedAdapter.viewHolderMap[attachmentItem.parentPosition]) {
                        is PicturePostHolder -> {
                            if (holder.pictureRecycler.tag == attachmentItem.parentPosition) {
                                memberPostPagedAdapter.updateInternalItem(holder)
                            }
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.postFollowItemListResult.observe(viewLifecycleOwner, Observer {
            memberPostPagedAdapter.submitList(it)
        })

        viewModel.clipPostItemListResult.observe(viewLifecycleOwner, Observer {
            memberPostPagedAdapter.submitList(it)
        })

        viewModel.picturePostItemListResult.observe(viewLifecycleOwner, Observer {
            memberPostPagedAdapter.submitList(it)
        })

        viewModel.textPostItemListResult.observe(viewLifecycleOwner, Observer {
            memberPostPagedAdapter.submitList(it)
        })

        viewModel.clubItemListResult.observe(viewLifecycleOwner, Observer {
            clubMemberAdapter.submitList(it)
        })

        viewModel.postReportResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.postPicResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    uploadPicItem[uploadCurrentPicPosition].id = it.result
                    uploadCurrentPicPosition += 1

                    if (uploadCurrentPicPosition > uploadPicUri.size - 1) {

                        val picItem = PicItem()

                        for (item in uploadPicItem) {
                            picItem.picParameter.add(
                                PicParameter(
                                    id = item.id.toString(),
                                    ext = item.ext
                                )
                            )
                        }

                        val content = Gson().toJson(picItem)
                        Timber.d("Post pic content item : $content")
                        viewModel.postPic(postMemberRequest, content)
                    } else {
                        val pic = uploadPicUri[uploadCurrentPicPosition]
                        viewModel.postAttachment(pic, requireContext())
                    }
                }
                is Error -> {
                    onApiError(it.throwable)
                    //TODO 中斷上傳 and reset
                }
            }
        })

        viewModel.uploadPicItem.observe(viewLifecycleOwner, Observer {
            uploadPicItem.add(it)

//            snackbar = Snackbar.make(testView, "", Snackbar.LENGTH_INDEFINITE)
//            val snackbarLayout: Snackbar.SnackbarLayout = snackbar?.view as Snackbar.SnackbarLayout
//            val textView = snackbarLayout.findViewById(R.id.snackbar_text) as TextView
//            textView.visibility = View.INVISIBLE
//
//            val snackView: View = layoutInflater.inflate(R.layout.snackbar_upload, null)
//            snackbarLayout.addView(snackView, 0)
//            snackbarLayout.setPadding(15,0,15,0)
//            snackbarLayout.setBackgroundColor(Color.TRANSPARENT)
//            snackbar?.show()
        })

        viewModel.postPicMemberResult.observe(viewLifecycleOwner, Observer {
            val snackBarLayout: Snackbar.SnackbarLayout = snackbar?.view as Snackbar.SnackbarLayout
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

            uploadCurrentPicPosition = 0
            uploadPicUri.clear()
        })
    }

    override fun setupListeners() {
        refresh.setOnRefreshListener {
            viewModel.lastListIndex = 0
            refresh.isRefreshing = false
            getData(lastPosition)
        }

        iv_bg_search.setOnClickListener {
            val bundle = SearchVideoFragment.createBundle("")
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_homeFragment_to_searchVideoFragment,
                    bundle
                )
            )
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

    override fun onDestroyView() {
        saveLastIndex()
        super.onDestroyView()
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

        recyclerview_tab.adapter = tabAdapter
        recyclerview.background = requireActivity().getDrawable(R.color.adult_color_background)
        recyclerview.layoutManager = LinearLayoutManager(requireContext())
        recyclerview.adapter = homeAdapter

        refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
    }

    private fun setupRecyclerByPosition(position: Int) {
        when (position) {
            0 -> {
                recyclerview.layoutManager = LinearLayoutManager(requireContext())
                recyclerview.adapter = homeAdapter
            }
            1 -> {
                recyclerview.layoutManager = GridLayoutManager(requireContext(), 2)
                recyclerview.adapter = videoListAdapter
            }
            2, 3, 4, 5 -> {
                recyclerview.layoutManager = LinearLayoutManager(requireContext())
                recyclerview.adapter = memberPostPagedAdapter
            }
            else -> {
                recyclerview.layoutManager = LinearLayoutManager(requireContext())
                recyclerview.adapter = clubMemberAdapter
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

        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
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

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                viewModel.setTopTabPosition(index)
            }
        }, true)
    }

    private val homeAdapter by lazy {
        HomeAdapter(
            requireContext(),
            adapterListener,
            true,
            clubListener,
            attachmentListener
        )
    }

    private val memberPostPagedAdapter by lazy {
        MemberPostPagedAdapter(requireActivity(), adultListener, attachmentListener)
    }

    private val clubMemberAdapter by lazy {
        ClubMemberAdapter(
            requireContext(),
            ClubFuncItem(
                { item -> onItemClick(item) },
                { id, function -> getBitmap(id, function) },
                { item, isFollow, function -> clubFollow(item, isFollow, function) })
        )
    }

    private val videoListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, true)
    }

    private val adultListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            viewModel.followPost(item, position, isFollow)
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            viewModel.likePost(item, position, isLike)
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_textDetailFragment,
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
            // TODO: Sion Wang
            val bundle = ClipFragment.createBundle(ArrayList(item), position)
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

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: String, position: Int, type: AttachmentType) {
            viewModel.getAttachment(id, position, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
            viewModel.getAttachment(id, parentPosition, position)
        }
    }

    private val clubListener = object : HomeClubAdapter.ClubListener {
        override fun followClub(item: MemberClubItem, position: Int, isFollow: Boolean) {
            viewModel.followClub(item, position, isFollow)
        }
    }

    private val adapterListener = object : HomeAdapter.EventListener {

        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            when (item.title) {
                "蜜蜜影视" -> viewModel.setTopTabPosition(1)
                "短视频" -> viewModel.setTopTabPosition(3)
                "图片" -> viewModel.setTopTabPosition(4)
                "圈子" -> viewModel.setTopTabPosition(6)
            }
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

    /**
     * 儲存最後一筆滑到的 index
     */
    private fun saveLastIndex() {
        val layoutManager = recyclerview.layoutManager as LinearLayoutManager
        viewModel.lastListIndex = layoutManager.findFirstCompletelyVisibleItemPosition()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_PHOTO -> {
                    data?.let {
                        val uriImage: Uri?

                        if (it.data != null) {
                            uriImage = it.data
                        } else {
                            val extras = it.extras
                            val imageBitmap = extras!!["data"] as Bitmap?
                            uriImage = Uri.parse(
                                MediaStore.Images.Media.insertImage(
                                    requireContext().contentResolver,
                                    imageBitmap,
                                    null,
                                    null
                                )
                            );
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
                    val myUri = Uri.fromFile(File(UriUtils.getPath(requireContext(), videoUri!!)))

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

    private fun clubFollow(
        memberClubItem: MemberClubItem,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        viewModel.clubFollow(memberClubItem, isFollow, update)
    }
}
