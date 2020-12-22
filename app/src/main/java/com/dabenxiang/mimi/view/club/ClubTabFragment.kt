package com.dabenxiang.mimi.view.club

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.ClubTabViewModel.Companion.REFRESH_TASK
import com.dabenxiang.mimi.view.club.adapter.ClubTabAdapter
import com.dabenxiang.mimi.view.club.adapter.TopicItemListener
import com.dabenxiang.mimi.view.club.adapter.TopicListAdapter
import com.dabenxiang.mimi.view.club.pages.ClubItemFragment
import com.dabenxiang.mimi.view.club.topic_detail.TopicTabFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.OnChooseUploadMethodDialogListener
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.utility.PostManager
import com.dabenxiang.mimi.view.post.video.EditVideoFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab_club.*
import kotlinx.android.synthetic.main.fragment_tab_club.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.lang.Exception


class ClubTabFragment : BaseFragment() {

    companion object {
        const val TAB_FOLLOW = 0
        const val TAB_RECOMMEND = 1
        const val TAB_LATEST = 2
        const val TAB_CLIP = 3
        const val TAB_PICTURE = 4
        const val TAB_NOVEL = 5

        const val DEFAULT_TAB = TAB_RECOMMEND

        private const val PERMISSION_VIDEO_REQUEST_CODE = 20001
        private const val PERMISSION_PIC_REQUEST_CODE = 20002

        private const val REQUEST_PHOTO = 10001
        private const val REQUEST_VIDEO_CAPTURE = 10002
        private const val REQUEST_LOGIN = 10003
    }

    private lateinit var tabLayoutMediator: TabLayoutMediator
    private val viewModel: ClubTabViewModel by viewModels()

    private var file = File("")

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        TAB_FOLLOW to { ClubItemFragment(ClubTabItemType.FOLLOW) },
        TAB_RECOMMEND to { ClubItemFragment(ClubTabItemType.HOTTEST) },
        TAB_LATEST to { ClubItemFragment(ClubTabItemType.LATEST) },
        TAB_CLIP to { ClubItemFragment(ClubTabItemType.SHORT_VIDEO) },
        TAB_PICTURE to { ClubItemFragment(ClubTabItemType.PICTURE) },
        TAB_NOVEL to { ClubItemFragment(ClubTabItemType.NOVEL) }
    )

    private val topicListAdapter by lazy {
        TopicListAdapter(object : TopicItemListener {
            override fun itemClicked(clubItem: MemberClubItem, position: Int) {
                Timber.i("clubItem= $clubItem")
                val bundle = TopicTabFragment.createBundle(clubItem)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_topicDetailFragment,
                        bundle
                    )
                )
            }

            override fun getAttachment(id: Long?, view: ImageView, type: LoadImageType) {
                viewModel.loadImage(id, view, type)
            }
        })
    }

    override fun getLayoutId() = R.layout.fragment_tab_club
    override fun setupObservers() {
        mainViewModel?.isShowSnackBar?.observe(viewLifecycleOwner, {
            iv_post.isEnabled = !it
        })
    }
    override fun setupListeners() {
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

        viewModel.clubCount.observe(this, {
            topic_group.visibility = if (it <= 0) View.GONE else View.VISIBLE
        })

        viewModel.doTask.observe(this, {
            when (it) {
                REFRESH_TASK -> getClubItemList()
                else -> {
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getClubItemList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        view.club_view_pager.adapter = ClubTabAdapter(tabFragmentsCreators, childFragmentManager, lifecycle)
        view.club_view_pager.offscreenPageLimit = 1
        val tabs = resources.getStringArray(R.array.club_tabs)
        tabLayoutMediator = TabLayoutMediator(view.club_tabs,  view.club_view_pager) { tab, position ->
            val tabView = View.inflate(requireContext(), R.layout.custom_tab, null)
            val textView = tabView?.findViewById<TextView>(R.id.tv_title)
            textView?.text = tabs[position]
            tab.customView = tabView
        }
        tabLayoutMediator.attach()
        view.club_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.currentTab = tab?.position ?: 0
                view.search_bar.text = String.format(
                    getString(R.string.text_search_classification),
                    tabs[tab?.position?:0]
                )
                Timber.d("tabs[${tab?.position}]=${tabs[tab?.position?:0]}")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        if(viewModel.currentTab == -1)  view.club_tabs.getTabAt(DEFAULT_TAB)?.select()

        view.topic_tabs.adapter = topicListAdapter

        view.search_bar.setOnClickListener {
            navToSearch(view.club_tabs.selectedTabPosition)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        Timber.i("ClubTabFragment onResume")
        val tabs = resources.getStringArray(R.array.club_tabs)
        search_bar.text = String.format(
            getString(R.string.text_search_classification),
            tabs[club_tabs.selectedTabPosition]
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("ClubTabFragment onDestroy")
        if (::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
    }

    private fun getClubItemList() {
        Timber.i("getClubItemList")
        CoroutineScope(Dispatchers.IO).launch {
            delay(100)
            topicListAdapter.submitData(PagingData.empty())
            viewModel.getClubItemList()
                .collectLatest {
                    topicListAdapter.submitData(it)
                }
        }
    }

    private val onChooseUploadMethodDialogListener = object : OnChooseUploadMethodDialogListener {
        override fun onUploadVideo() {
            val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)
            if (requestList.size > 0) {
                requestPermissions(
                    requestList.toTypedArray(),
                    PERMISSION_VIDEO_REQUEST_CODE
                )
            } else {
                PostManager().selectVideo(this@ClubTabFragment)
            }
        }

        override fun onUploadPic() {
            val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)
            if (requestList.size > 0) {
                requestPermissions(
                    requestList.toTypedArray(),
                    PERMISSION_PIC_REQUEST_CODE
                )
            } else {
                file = FileUtil.getTakePhoto(System.currentTimeMillis().toString() + ".jpg")
                PostManager().selectPics(this@ClubTabFragment, file)
            }
        }

        override fun onUploadArticle() {
            findNavController().navigate(R.id.action_to_postArticleFragment)
        }
    }

    private fun requestVideoPermissions() {
        val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)

        if (requestList.size == 0) {
            PostManager().selectVideo(this@ClubTabFragment)
        }
    }

    private fun requestPicPermissions() {
        val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)

        if (requestList.size == 0) {
            file = FileUtil.getTakePhoto(System.currentTimeMillis().toString() + ".jpg")
            PostManager().selectPics(this@ClubTabFragment, file)
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
                try{
                    pciUri.add(UriUtils.getPath(requireContext(), uri)!!)
                } catch(e: Exception) {
                    GeneralUtils.showToast(requireContext(), "不支援此图片上传")
                    onApiError(e)
                }
            } else {
                pciUri.add(file.absolutePath)
            }
        }

        if (pciUri.isEmpty()) {
            return
        }

        val bundle = Bundle()
        bundle.putStringArrayList(BasePostFragment.BUNDLE_PIC_URI, pciUri)

        findNavController().navigate(
            R.id.action_to_postPicFragment,
            bundle
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult")
        if (requestCode == PERMISSION_VIDEO_REQUEST_CODE) {
            if (getNotGrantedPermissions(externalPermissions + cameraPermissions).isEmpty()) {
                PostManager().selectVideo(this@ClubTabFragment)
            } else {
                requestVideoPermissions()
            }
        } else if (requestCode == PERMISSION_PIC_REQUEST_CODE) {
            if (getNotGrantedPermissions(externalPermissions + cameraPermissions).isEmpty()) {
                file = FileUtil.getTakePhoto(System.currentTimeMillis().toString() + ".jpg")
                PostManager().selectPics(this@ClubTabFragment, file)
            } else {
                requestPicPermissions()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
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
                        bundle.putString(EditVideoFragment.BUNDLE_VIDEO_URI, myUri.toString())
                        findNavController().navigate(
                            R.id.action_to_editVideoFragment,
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

    private fun navToSearch(position: Int) {
        val searchPostItem = when (position) {
            TAB_FOLLOW -> SearchPostItem(type = PostType.FOLLOWED)
            TAB_RECOMMEND -> SearchPostItem(
                type = PostType.TEXT_IMAGE_VIDEO,
                orderBy = StatisticsOrderType.HOTTEST
            )
            TAB_LATEST -> SearchPostItem(
                type = PostType.TEXT_IMAGE_VIDEO,
                orderBy = StatisticsOrderType.LATEST
            )
            TAB_CLIP -> SearchPostItem(type = PostType.VIDEO)
            TAB_PICTURE -> SearchPostItem(type = PostType.IMAGE)
            TAB_NOVEL -> SearchPostItem(type = PostType.TEXT)
            else -> SearchPostItem(type = PostType.TEXT_IMAGE_VIDEO)
        }
        val bundle = SearchPostFragment.createBundle(searchPostItem)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_searchPostFragment,
                bundle
            )
        )
    }
}


