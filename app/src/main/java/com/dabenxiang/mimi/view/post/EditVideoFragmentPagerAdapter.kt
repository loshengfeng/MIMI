package com.dabenxiang.mimi.view.post

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class EditVideoFragmentPagerAdapter(fm: FragmentManager, private var totalTabs: Int, private val uri: String)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var fragmentArray = SparseArray<Fragment>()

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> { EditVideoRangeFragment.newInstance(uri) }
            1 -> { CropVideoFragment.newInstance(uri) }
            else -> EditVideoRangeFragment()
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        fragmentArray.put(position, fragment)
        return super.instantiateItem(container, position)
    }

    override fun getCount() = totalTabs

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        fragmentArray.remove(position)
        super.destroyItem(container, position, `object`)
    }

    fun getFragment(position: Int): Fragment? {
        return fragmentArray.get(position)
    }
}