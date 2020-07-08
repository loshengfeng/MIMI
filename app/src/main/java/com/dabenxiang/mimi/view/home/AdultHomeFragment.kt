package com.dabenxiang.mimi.view.home

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.model.holder.statisticsItemToCarouselHolderItem
import com.dabenxiang.mimi.model.holder.statisticsItemToVideoItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeClubAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.home.viewholder.HomeCarouselViewHolder
import com.dabenxiang.mimi.view.home.viewholder.HomeClipViewHolder
import com.dabenxiang.mimi.view.home.viewholder.HomeStatisticsViewHolder
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.SearchVideoFragment
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber

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
    private val attachmentMap: HashMap<Long, Bitmap> = hashMapOf()

    override fun getLayoutId() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback { backToDesktop() }
        setupAdultUI()
        recyclerview_tab.adapter = tabAdapter
        recyclerview_home.adapter = adapter
        recyclerview_videos.adapter = videoListAdapter
        refresh_home.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))

        if (mainViewModel?.adult == null) {
            mainViewModel?.loadHomeCategories()
        }
    }

    override fun setupObservers() {
        mainViewModel?.categoriesData?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> refresh_home.isRefreshing = true
                is Loaded -> refresh_home.isRefreshing = false
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
                        loadFirstTab(mainViewModel?.adult)
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.tabLayoutPosition.observe(viewLifecycleOwner, Observer { position ->
            lastPosition = position
            tabAdapter.setLastSelectedIndex(lastPosition)
            when (position) {
                0 -> {
                    btn_filter.visibility = View.GONE
                    loadFirstTab(mainViewModel?.adult)
                }
                1 -> {
                    btn_filter.visibility = View.VISIBLE
                    loadCategories(null)
                }
                6 -> {
                    btn_filter.visibility = View.VISIBLE
                    recyclerview_videos.visibility = View.VISIBLE
                    refresh_home.visibility = View.GONE
                }
                else -> {
                    btn_filter.visibility = View.VISIBLE
                    val keyword = mainViewModel?.adult?.categories?.get(position)?.name
                    loadCategories(keyword)
                }
            }
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
                    when (val holder = adapter.homeViewHolderMap[HomeItemType.CLUB]) {
                        is HomeClubViewHolder -> {
                            holder.updateItemByFollow(it.result, true)
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.cancelFollowClubResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when (val holder = adapter.homeViewHolderMap[HomeItemType.CLUB]) {
                        is HomeClubViewHolder -> {
                            holder.updateItemByFollow(it.result, false)
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val attachmentItem = it.result
                    attachmentMap[attachmentItem.id] = attachmentItem.bitmap
                    when (val holder = adapter.homeViewHolderMap[attachmentItem.type]) {
                        is HomeClipViewHolder -> {
                            holder.updateItem(attachmentItem.position)
                        }
                        is HomePictureViewHolder -> {
                            holder.updateItem(attachmentItem.position)
                        }
                        is HomeClubViewHolder -> {
                            holder.updateItem(attachmentItem.position)
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        refresh_home.setOnRefreshListener {
            refresh_home.isRefreshing = false
            mainViewModel?.loadHomeCategories()
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
    }

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                viewModel.setTopTabPosition(index)
            }
        }, true)
    }

    private val adapter by lazy {
        HomeAdapter(
            requireContext(),
            adapterListener,
            true,
            clubListener,
            attachmentListener,
            attachmentMap
        )
    }

    private val videoListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, true)
    }

    private val attachmentListener = object : HomeAdapter.AttachmentListener {
        override fun onGetAttachment(id: Long, position: Int, type: HomeItemType) {
            viewModel.getAttachment(id, position, type)
        }
    }

    private val clubListener = object : HomeClubAdapter.ClubListener {
        override fun followClub(id: Int, position: Int) {
            viewModel.followClub(id, position)
        }

        override fun cancelFollowClub(id: Int, position: Int) {
            viewModel.cancelFollowClub(id, position)
        }
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            val bundle = CategoriesFragment.createBundle(item.title ?: "", item.categories)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_homeFragment_to_categoriesFragment,
                    bundle
                )
            )
        }

        override fun onVideoClick(view: View, item: PlayerData) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivity(intent)
        }

        override fun onClipClick(view: View, item: List<MemberPostItem>) {
            val bundle = ClipFragment.createBundle(ArrayList(item))
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onPictureClick(view: View, item: MemberPostItem) {

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

    private fun setupAdultUI() {
        layout_top.background = requireActivity().getDrawable(R.color.adult_color_status_bar)

        layout_search_bar.background = requireActivity().getDrawable(R.color.adult_color_background)
        iv_bg_search.setBtnSolidColor(requireActivity().getColor(R.color.adult_color_search_bar))

        iv_search.setImageResource(R.drawable.adult_btn_search)
        tv_search.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))

        recyclerview_home.background = requireActivity().getDrawable(R.color.adult_color_background)
        recyclerview_videos.background =
            requireActivity().getDrawable(R.color.adult_color_background)

        btn_filter.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))
        btn_filter.setBtnSolidColor(
            requireActivity().getColor(R.color.color_white_1_30),
            requireActivity().getColor(R.color.color_red_1),
            resources.getDimension(R.dimen.dp_6)
        )
    }

    private fun loadFirstTab(root: CategoriesItem?) {
        recyclerview_videos.visibility = View.GONE
        refresh_home.visibility = View.VISIBLE

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
        adapter.submitList(templateList)
    }

    private fun loadCategories(keyword: String?) {
        recyclerview_videos.visibility = View.VISIBLE
        refresh_home.visibility = View.GONE
        viewModel.setupVideoList(keyword, true)
    }
}
