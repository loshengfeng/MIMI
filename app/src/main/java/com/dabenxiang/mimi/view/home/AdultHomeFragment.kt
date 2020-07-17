package com.dabenxiang.mimi.view.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.holder.statisticsItemToCarouselHolderItem
import com.dabenxiang.mimi.model.holder.statisticsItemToVideoItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.*
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.OnChooseUploadMethodDialogListener
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.putLruCache
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import java.lang.ClassCastException

class AdultHomeFragment : BaseFragment() {

    private var lastPosition = 0

    private val viewModel: HomeViewModel by viewModels()

    private val homeCarouselViewHolderMap = hashMapOf<Int, HomeCarouselViewHolder>()
    private val carouselMap = hashMapOf<Int, HomeTemplate.Carousel>()

    private val homeStatisticsViewHolderMap = hashMapOf<Int, HomeStatisticsViewHolder>()
    private val statisticsMap = hashMapOf<Int, HomeTemplate.Statistics>()

    private val homeClipViewHolderMap = hashMapOf<Int, HomeClipViewHolder>()
    private val homePictureViewHolderMap = hashMapOf<Int, HomePictureViewHolder>()
    private val homeClubViewHolderMap = hashMapOf<Int, HomeClubViewHolder>()

    var moreDialog: MoreDialogFragment? = null
    var reportDialog: ReportDialogFragment? = null

    private var interactionListener: InteractionListener? = null

    override fun getLayoutId() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback { interactionListener?.changeNavigationPosition(R.id.navigation_home) }

        setupUI()

        if (mainViewModel?.adult == null) {
            mainViewModel?.getHomeCategories()
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
            setupPostTypeByPosition(position)
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
                    when (commonPagedAdapter.viewHolderMap[it.result]) {
                        is PicturePostHolder -> {
                            commonPagedAdapter.notifyItemChanged(it.result, CommonPagedAdapter.PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI)
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when (commonPagedAdapter.viewHolderMap[it.result]) {
                        is PicturePostHolder -> {
                            commonPagedAdapter.notifyItemChanged(it.result, CommonPagedAdapter.PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI)
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
                            val holder = homeAdapter.attachmentViewHolderMap[attachmentItem.type]
                            holder as HomeClipViewHolder
                            holder.updateItem(attachmentItem.position!!)
                        }
                        AttachmentType.ADULT_HOME_PICTURE -> {
                            val holder = homeAdapter.attachmentViewHolderMap[attachmentItem.type]
                            holder as HomePictureViewHolder
                            holder.updateItem(attachmentItem.position!!)
                        }
                        AttachmentType.ADULT_HOME_CLUB -> {
                            val holder = homeAdapter.attachmentViewHolderMap[attachmentItem.type]
                            holder as HomeClubViewHolder
                            holder.updateItem(attachmentItem.position!!)
                        }
                        AttachmentType.ADULT_PICTURE_ITEM -> {
                            commonPagedAdapter.notifyItemChanged(attachmentItem.position!!)
                        }
                        else -> {
                        }
                    }

                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.scrollToLastPosition.observe(viewLifecycleOwner, Observer {result->
            if (result) recyclerview.scrollToPosition(viewModel.lastListIndex)
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val attachmentItem = it.result
                    putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    when (val holder =
                        commonPagedAdapter.viewHolderMap[attachmentItem.parentPosition]) {
                        is PicturePostHolder -> {
                            if (holder.pictureRecycler.tag == attachmentItem.parentPosition) {
                                commonPagedAdapter.updateInternalItem(holder)
                            }
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.picturePostItemListResult.observe(viewLifecycleOwner, Observer {
            commonPagedAdapter.submitList(it)
        })

        viewModel.clipPostItemListResult.observe(viewLifecycleOwner, Observer {
            commonPagedAdapter.submitList(it)
        })

        viewModel.postReportResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                }
                is Error -> Timber.e(it.throwable)
            }
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
        LinearSnapHelper().attachToRecyclerView(recyclerview)

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
            else -> {
                recyclerview.layoutManager = LinearLayoutManager(requireContext())
                recyclerview.adapter = commonPagedAdapter
            }
        }
    }

    private fun setupPostTypeByPosition(position: Int) {
        val type = when (position) {
            0 -> AdultTabType.HOME
            1 -> AdultTabType.VIDEO
            2 -> AdultTabType.FOLLOW
            3 -> AdultTabType.CLIP
            4 -> AdultTabType.PICTURE
            5 -> AdultTabType.TEXT
            else -> AdultTabType.CLUB
        }
        commonPagedAdapter.setupAdultTabType(type)
    }

    private fun getData(position: Int) {
        when (position) {
            0 -> mainViewModel?.getHomeCategories()
            1 -> viewModel.getVideos(null, true)
            2 -> {
                // TODO: 關注
            }
            3 -> viewModel.getClipPosts()
            4 -> viewModel.getPicturePosts()
            5 -> {
                // TODO: 短文
            }
            6 -> {
                // TODO: 圈子
            }
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

    private val commonPagedAdapter by lazy {
        CommonPagedAdapter(requireActivity(), adultListener, attachmentListener)
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

        override fun onCommentClick(item: MemberPostItem) {
            val bundle = PictureDetailFragment.createBundle(item, 1)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_pictureDetailFragment,
                    bundle
                )
            )
        }

        override fun onMoreClick(item: MemberPostItem) {
            moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    MoreDialogFragment::class.java.simpleName
                )
            }
        }

        override fun onItemClick(item: MemberPostItem) {
            val bundle = PictureDetailFragment.createBundle(item, 0)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_pictureDetailFragment,
                    bundle
                )
            )
        }
    }

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: MemberPostItem, content: String) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(requireContext(), getString(R.string.report_error))
            } else {
                reportDialog?.dismiss()
                viewModel.sendPostReport(item, content)
            }
        }

        override fun onCancel() {
            reportDialog?.dismiss()
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: MemberPostItem) {
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
        }

        override fun onUploadPic() {
        }

        override fun onUploadArticle() {
            findNavController().navigate(R.id.action_adultHomeFragment_to_postArticleFragment)
        }
    }

    /**
     * 儲存最後一筆滑到的 index
     */
    private fun saveLastIndex(){
        val layoutManager = recyclerview.layoutManager as LinearLayoutManager
        viewModel.lastListIndex = layoutManager.findFirstCompletelyVisibleItemPosition()
    }
}
