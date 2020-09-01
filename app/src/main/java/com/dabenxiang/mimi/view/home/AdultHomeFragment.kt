package com.dabenxiang.mimi.view.home

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.*
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter.Companion.PAYLOAD_UPDATE_FOLLOW
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.club.ClubFuncItem
import com.dabenxiang.mimi.view.club.ClubMemberAdapter
import com.dabenxiang.mimi.view.club.MiMiLinearLayoutManager
import com.dabenxiang.mimi.view.clubdetail.ClubDetailFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.OnChooseUploadMethodDialogListener
import com.dabenxiang.mimi.view.dialog.login_request.LoginRequestDialog
import com.dabenxiang.mimi.view.home.category.CategoriesFragment
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.listener.OnLoginRequestDialogListener
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.ADULT
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.BUNDLE_PIC_URI
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.PAGE
import com.dabenxiang.mimi.view.post.utility.PostManager
import com.dabenxiang.mimi.view.post.video.EditVideoFragment.Companion.BUNDLE_VIDEO_URI
import com.dabenxiang.mimi.view.ranking.RankingFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File

class AdultHomeFragment : BaseFragment() {

    private var lastPosition = 0

    private var file = File("")

    private val viewModel: HomeViewModel by viewModels()

    private val homeBannerViewHolderMap = hashMapOf<Int, HomeBannerViewHolder>()
    private val homeCarouselViewHolderMap = hashMapOf<Int, HomeCarouselViewHolder>()
    private val carouselMap = hashMapOf<Int, HomeTemplate.Carousel>()

    private val homeStatisticsViewHolderMap = hashMapOf<Int, HomeStatisticsViewHolder>()
    private val statisticsMap = hashMapOf<Int, HomeTemplate.Statistics>()

    private val homeClipViewHolderMap = hashMapOf<Int, HomeClipViewHolder>()
    private val homePictureViewHolderMap = hashMapOf<Int, HomePictureViewHolder>()
    private val homeClubViewHolderMap = hashMapOf<Int, HomeClubViewHolder>()

    private var interactionListener: InteractionListener? = null

    private var loginDialog: LoginRequestDialog? = null

    val accountManager: AccountManager by inject()

    companion object {
        private const val REQUEST_PHOTO = 10001
        private const val REQUEST_VIDEO_CAPTURE = 10002
        private const val REQUEST_LOGIN = 10003

        const val RECORD_LIMIT_TIME = 15
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
        checkPageState()
        getCurrentAdapter()?.notifyDataSetChanged()
    }

    override fun setupObservers() {
    }

    private fun checkPageState() {
        when(lastPosition){
            2-> {
                if(accountManager.isLogin()) {
                    Timber.i("isLogin")
                    showNoLoginToggle(false)
                    refresh.isRefreshing
                    getData(lastPosition)
                }
                else  showNoLoginToggle(true)
            }
            else  -> showNoLoginToggle(false)
        }
    }

    override fun setupFirstTime() {
        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()
        setupUI()

        if (mainViewModel?.adult == null) {
            mainViewModel?.getHomeCategories()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel?.categoriesData?.observe(this, Observer {
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

        mainViewModel?.getAdHomeResult?.observe(this, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeBannerViewHolderMap[it.first]
                    viewHolder?.updateItem(response.result)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.showProgress.observe(this, Observer { showProgress ->
            showProgress?.takeUnless { it }?.also { refresh.isRefreshing = it }
        })

        viewModel.videoList.observe(this, Observer {
            videoListAdapter.submitList(it)
            videoListAdapter.notifyDataSetChanged()
        })

        viewModel.carouselResult.observe(this, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeCarouselViewHolderMap[it.first]
                    val carousel = carouselMap[it.first]
                    val carouselHolderItems =
                        response.result.content?.categoryBannerItemCarouselHolderItem()
//                        response.result.content?.statisticsItemToCarouselHolderItem(carousel!!.isAdult)
                    viewHolder?.submitList(carouselHolderItems)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.videosResult.observe(this, Observer {
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

        viewModel.clipsResult.observe(this, Observer {
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

        viewModel.pictureResult.observe(this, Observer {
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

        viewModel.clubResult.observe(this, Observer {
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

        viewModel.postFollowItemListResult.observe(this, Observer {
            followPostPagedAdapter.submitList(it)
            followPostPagedAdapter.notifyDataSetChanged()
        })

        viewModel.clipPostItemListResult.observe(this, Observer {
            clipPostPagedAdapter.submitList(it)
            clipPostPagedAdapter.notifyDataSetChanged()
        })

        viewModel.picturePostItemListResult.observe(this, Observer {
            picturePostPagedAdapter.submitList(it)
            picturePostPagedAdapter.notifyDataSetChanged()
        })

        viewModel.textPostItemListResult.observe(this, Observer {
            textPostPagedAdapter.submitList(it)
            textPostPagedAdapter.notifyDataSetChanged()
        })

        viewModel.clubItemListResult.observe(this, Observer {
            clubMemberAdapter.submitList(it)
            clubMemberAdapter.notifyDataSetChanged()
        })

        viewModel.totalCountResult.observe(this, Observer {
            it?.also { totalCount ->
                cl_no_data.visibility =
                    takeIf { totalCount > 0 }?.let { View.GONE } ?: let { View.VISIBLE }
                takeIf { rv_sixth.visibility == View.VISIBLE }?.also {
                    clubMemberAdapter.totalCount = totalCount
                }
                viewModel.clearLiveDataValue()
            }
        })

        viewModel.followResult.observe(this, Observer {
            when (it) {
                is Empty -> {
                    getCurrentAdapter()?.notifyItemRangeChanged(
                        0,
                        viewModel.totalCount,
                        PAYLOAD_UPDATE_FOLLOW
                    )
                    viewModel.getAllOtherPosts(lastPosition)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.deletePostResult?.observe(this, Observer {
            when (lastPosition) {
                2, 3, 4, 5 -> {
                    when (it) {
                        is Success -> {
                            val adapter = getCurrentAdapter() as MemberPostPagedAdapter
                            adapter.removedPosList.add(it.result)
                            adapter.notifyItemChanged(it.result)
                        }
                        is Error -> onApiError(it.throwable)
                    }
                }
            }
        })

        viewModel.cleanRemovedPosList.observe(this, Observer {
            when (lastPosition) {
                2, 3, 4, 5 -> {
                    val adapter = getCurrentAdapter() as MemberPostPagedAdapter
                    adapter.removedPosList.clear()
                }
            }
        })

    }

    private fun getCurrentAdapter(): RecyclerView.Adapter<*>? {
        return when (lastPosition) {
            1 -> rv_first.adapter
            2 -> rv_second.adapter
            3 -> rv_third.adapter
            4 -> rv_fourth.adapter
            5 -> rv_fifth.adapter
            else -> rv_sixth.adapter
        }
    }

    private fun navigationToText(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_adultHomeFragment_to_textDetailFragment,
                bundle
            )
        )
    }

    private fun navigationToPicture(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_adultHomeFragment_to_pictureDetailFragment,
                bundle
            )
        )
    }

    private fun navigationToClip(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_adultHomeFragment_to_clipFragment,
                bundle
            )
        )
    }

    override fun setupListeners() {
        refresh.setOnRefreshListener {
            viewModel.lastListIndex = 0
            refresh.isRefreshing = true
            getData(lastPosition)
        }

        iv_bg_search.setOnClickListener {
            val item: SearchPostItem = when (lastPosition) {
                0 -> SearchPostItem(type = PostType.HYBRID)
                1 -> SearchPostItem(type = PostType.VIDEO_ON_DEMAND)
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

    private fun showNoLoginToggle(isShow:Boolean){
        if(isShow){
            cl_no_login.visibility = View.VISIBLE
            cl_no_data.visibility = View.GONE
            refresh.visibility = View.GONE
        } else {
            cl_no_login.visibility = View.GONE
            refresh.visibility = View.VISIBLE
        }
    }

    private fun showLoginDialog(){
        loginDialog?.dismiss()
        loginDialog = LoginRequestDialog.newInstance(object : OnLoginRequestDialogListener {
            override fun onRegister() {
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_loginFragment,
                        LoginFragment.createBundle(LoginFragment.TYPE_REGISTER)
                    )
                )
            }

            override fun onLogin() {
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_loginFragment,
                        LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                    )
                )
            }

            override fun onCancel() {}

        }).also {
            it.show(
                requireActivity().supportFragmentManager,
                LoginRequestDialog::class.java.simpleName
            )
        }
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
        cl_no_login.visibility = View.GONE
        cl_no_login.background =
            requireActivity().getDrawable(R.color.adult_color_background)
        when (position) {
            0 -> {
                btn_ranking.visibility = View.VISIBLE
                rv_home.visibility = View.VISIBLE
                showNoLoginToggle(false)
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
                showNoLoginToggle(false)
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
                } ?: run {
                    cl_no_data.visibility =
                        followPostPagedAdapter.currentList.takeUnless { isListEmpty(it) }
                            ?.let { View.GONE } ?: let { View.VISIBLE }
                }
                takeIf{ !accountManager.isLogin() }?.also {
                    showNoLoginToggle(true)
                    showLoginDialog()
                }

            }
            3 -> {
                rv_third.visibility = View.VISIBLE
                showNoLoginToggle(false)
                takeIf { rv_third.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_third.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_third.layoutManager = LinearLayoutManager(requireContext())
                    rv_third.adapter = clipPostPagedAdapter
                    viewModel.getClipPosts()
                } ?: run {
                    cl_no_data.visibility =
                        clipPostPagedAdapter.currentList.takeUnless { isListEmpty(it) }
                            ?.let { View.GONE } ?: let { View.VISIBLE }
                }
            }
            4 -> {
                rv_fourth.visibility = View.VISIBLE
                showNoLoginToggle(false)
                takeIf { rv_fourth.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_fourth.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_fourth.layoutManager = LinearLayoutManager(requireContext())
                    rv_fourth.adapter = picturePostPagedAdapter
                    viewModel.getPicturePosts()
                } ?: run {
                    cl_no_data.visibility =
                        picturePostPagedAdapter.currentList.takeUnless { isListEmpty(it) }
                            ?.let { View.GONE } ?: let { View.VISIBLE }
                }
            }
            5 -> {
                rv_fifth.visibility = View.VISIBLE
                showNoLoginToggle(false)
                takeIf { rv_fifth.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_fifth.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_fifth.layoutManager = LinearLayoutManager(requireContext())
                    rv_fifth.adapter = textPostPagedAdapter
                    viewModel.getTextPosts()
                } ?: run {
                    cl_no_data.visibility =
                        textPostPagedAdapter.currentList.takeUnless { isListEmpty(it) }
                            ?.let { View.GONE } ?: let { View.VISIBLE }
                }
            }
            else -> {
                rv_sixth.visibility = View.VISIBLE
                showNoLoginToggle(false)
                takeIf { rv_sixth.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_sixth.background =
                        requireActivity().getDrawable(R.color.adult_color_background)
                    rv_sixth.layoutManager = MiMiLinearLayoutManager(requireContext())
                    rv_sixth.adapter = clubMemberAdapter
                    viewModel.getClubs()
                } ?: run {
                    cl_no_data.visibility =
                        clubMemberAdapter.currentList.takeUnless { isClubListEmpty(it) }
                            ?.let { View.GONE } ?: let { View.VISIBLE }
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
        viewModel.lastPosition = index
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
            { item, items, isFollow, func -> followMember(item, items, isFollow, func) },
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
                        navigationToPicture(bundle)
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 2)
                        navigationToText(bundle)
                    }
                    AdultTabType.CLIP -> {
                        val bundle = ClipFragment.createBundle(arrayListOf(item), 0, true)
                        navigationToClip(bundle)
                    }
                }
            }
        }

        override fun onMoreClick(item: MemberPostItem, items: List<MemberPostItem>) {
            onMoreClick(
                item,
                ArrayList(items),
                onEdit = {
                    val bundle = Bundle()
                    bundle.putBoolean(MyPostFragment.EDIT, true)
                    bundle.putString(PAGE, ADULT)
                    bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)

                    it as MemberPostItem
                    when (item.type) {
                        PostType.TEXT -> {
                            findNavController().navigate(
                                R.id.action_adultHomeFragment_to_postArticleFragment,
                                bundle
                            )
                        }
                        PostType.IMAGE -> {
                            findNavController().navigate(
                                R.id.action_adultHomeFragment_to_postPicFragment,
                                bundle
                            )
                        }
                        PostType.VIDEO -> {
                            findNavController().navigate(
                                R.id.action_adultHomeFragment_to_postVideoFragment,
                                bundle
                            )
                        }
                    }
                }
            )
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
                    navigationToPicture(bundle)
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigationToText(bundle)
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipFragment.createBundle(arrayListOf(item), 0)
                    navigationToClip(bundle)
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val bundle =
                ClipFragment.createBundle(ArrayList(item.subList(1, item.size)), position - 1)
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
            navigateTo(NavigateItem.Destination(R.id.action_to_myPostFragment, bundle))
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
            viewModel.loadNestedStatisticsListForCarousel(vh.adapterPosition, src, true)
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

        override fun onClickBanner(item: CarouselHolderItem) {
            GeneralUtils.openWebView(requireContext(), item.url)
        }
    }

    private val onChooseUploadMethodDialogListener = object : OnChooseUploadMethodDialogListener {
        override fun onUploadVideo() {
            PostManager().selectVideo(this@AdultHomeFragment)
        }

        override fun onUploadPic() {
            file = FileUtil.getTakePhoto(System.currentTimeMillis().toString() + ".jpg")
            PostManager().selectPics(this@AdultHomeFragment, file)
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
                    handleTakePhoto(data)
                }

                REQUEST_VIDEO_CAPTURE -> {
                    val videoUri: Uri? = data?.data
                    val myUri =
                        Uri.fromFile(File(UriUtils.getPath(requireContext(), videoUri!!) ?: ""))

                    if (PostManager().isVideoTimeValid(myUri, requireContext())) {
                        val bundle = Bundle()
                        bundle.putString(BUNDLE_VIDEO_URI, myUri.toString())
                        findNavController().navigate(
                            R.id.action_adultHomeFragment_to_editVideoFragment,
                            bundle
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.post_video_length_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                REQUEST_LOGIN -> {
                    findNavController().navigate(R.id.action_to_loginFragment, data?.extras)
                }
            }
        }
    }

    private fun handleTakePhoto(data: Intent?) {
        val pciUri = arrayListOf<String>()

        val clipData = data?.clipData
        if (clipData != null) {
            pciUri.addAll(PostManager().getPicsUri(clipData, requireContext()))
        } else {
            val uri = PostManager().getPicUri(data, requireContext(), file)

            if (uri.path!!.isNotBlank()) {
                pciUri.add(UriUtils.getPath(requireContext(), uri)!!)
            } else {
                pciUri.add(file.absolutePath)
            }
        }

        val bundle = Bundle()
        bundle.putStringArrayList(BUNDLE_PIC_URI, pciUri)

        findNavController().navigate(
            R.id.action_adultHomeFragment_to_postPicFragment,
            bundle
        )
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
        items: List<MemberPostItem>,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.followMember(memberPostItem, ArrayList(items), isFollow, update) }
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

    private fun isListEmpty(list: PagedList<MemberPostItem>?): Boolean {
        return list == null || list.size == 0 || (list.size == 1 && list[0]?.adItem != null)
    }

    private fun isClubListEmpty(list: PagedList<MemberClubItem>?): Boolean {
        return list == null || list.size == 0 || (list.size == 1 && list[0]?.adItem != null)
    }
}
