/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dabenxiang.mimi.extension

import android.content.Intent
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.vo.StatusItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber

/**
 * Manages the various graphs needed for a [BottomNavigationView].
 *
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 */

var firstFragmentGraphId = 0
// Map of tags
val graphIdToTagMap = SparseArray<String>()

fun BottomNavigationView.setupWithNavController(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent,
    domainManager: DomainManager,
    accountManager: AccountManager,
    onEmailUnconfirmed: () -> Unit
): LiveData<NavController> {

    // Result. Mutable live data with the selected controlled
    val selectedNavController = MutableLiveData<NavController>()

    // First create a NavHostFragment for each NavGraph ID
    navGraphIds.forEachIndexed { index, navGraphId ->

        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )

        // Obtain its id
        val graphId = navHostFragment.navController.graph.id

        Timber.d("graphId = $graphId")
        Timber.d("index = $index")
        if (index == 0) {
            firstFragmentGraphId = graphId
        }

        // Save to the map
        graphIdToTagMap[graphId] = fragmentTag

        // Attach or detach nav host fragment depending on whether it's the selected item.
        if (this.selectedItemId == graphId) {
            // Update livedata with the selected graph
            Timber.d("this.selectedItemId = ${this.selectedItemId}")
            selectedNavController.value = navHostFragment.navController
            attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
        } else {
            detachNavHostFragment(fragmentManager, navHostFragment)
        }
    }

    // Now connect selecting an item with swapping Fragments
    var selectedItemTag = graphIdToTagMap[this.selectedItemId]
    val firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
    var isOnFirstFragment = selectedItemTag == firstFragmentTag

    // When a navigation item is selected
    setOnNavigationItemSelectedListener { item ->
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) {
            false
        } else {
            //TODO: 目前先不判斷是否有驗證過
//            var status = StatusItem.LOGIN_AND_EMAIL_CONFIRMED
//            if (accountManager.isLogin() && (item.title.toString() == context.getString(R.string.nav_topup) || item.title.toString() == context.getString(
//                    R.string.nav_favorite
//                ))
//            ) {
//                runBlocking {
//                    val isConfirmed = withContext(Dispatchers.Default) {
//                        isEmailConfirmed(domainManager)
//                    }
//                    status =
//                        if (isConfirmed) StatusItem.LOGIN_AND_EMAIL_CONFIRMED else StatusItem.LOGIN_BUT_EMAIL_NOT_CONFIRMED
//                }
//            }
            val status = StatusItem.LOGIN_AND_EMAIL_CONFIRMED
            when (status) {
                StatusItem.NOT_LOGIN -> {
                    false
                }
                StatusItem.LOGIN_BUT_EMAIL_NOT_CONFIRMED -> {
                    onEmailUnconfirmed()
                    false
                }
                StatusItem.LOGIN_AND_EMAIL_CONFIRMED -> {
                    val newlySelectedItemTag = graphIdToTagMap[item.itemId]
                    if (selectedItemTag != newlySelectedItemTag) {
                        // Pop everything above the first fragment (the "fixed start destination")
                        fragmentManager.popBackStack(
                            firstFragmentTag,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                        val selectedFragment =
                            fragmentManager.findFragmentByTag(newlySelectedItemTag)
                                    as NavHostFragment

                        // Exclude the first fragment tag because it's always in the back stack.
                        if (firstFragmentTag != newlySelectedItemTag) {
                            // Commit a transaction that cleans the back stack and adds the first fragment
                            // to it, creating the fixed started destination.
                            fragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.nav_default_enter_anim,
                                    R.anim.nav_default_exit_anim,
                                    R.anim.nav_default_pop_enter_anim,
                                    R.anim.nav_default_pop_exit_anim
                                )
                                .attach(selectedFragment)
                                .setPrimaryNavigationFragment(selectedFragment)
                                .apply {
                                    // Detach all other Fragments
                                    graphIdToTagMap.forEach { _, fragmentTagIter ->
                                        if (fragmentTagIter != newlySelectedItemTag) {
                                            detach(
                                                fragmentManager.findFragmentByTag(
                                                    firstFragmentTag
                                                )!!
                                            )
                                        }
                                    }
                                }
                                .addToBackStack(firstFragmentTag)
                                .setReorderingAllowed(true)
                                .commit()
                        }
                        selectedItemTag = newlySelectedItemTag
                        isOnFirstFragment = selectedItemTag == firstFragmentTag
                        selectedNavController.value = selectedFragment.navController
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }

    // Optional: on item reselected, pop back stack to the destination of the graph
//    setupItemReselected(graphIdToTagMap, fragmentManager)

    // Handle deep link
    setupDeepLinks(navGraphIds, fragmentManager, containerId, intent)

    // Finally, ensure that we update our BottomNavigationView when the back stack changes
    fragmentManager.addOnBackStackChangedListener {
        if (!isOnFirstFragment && !(fragmentManager isOnBackStack firstFragmentTag)) {
            this.selectedItemId = firstFragmentGraphId
        }

        // Reset the graph if the currentDestination is not valid (happens when the back
        // stack is popped after using the back button).
        selectedNavController.value?.let { controller ->
            if (controller.currentDestination == null) {
                controller.navigate(controller.graph.id)
            }
        }
    }
    return selectedNavController
}

private suspend fun isEmailConfirmed(domainManager: DomainManager): Boolean {
    val result = domainManager.getApiRepository().getMe()
    if (!result.isSuccessful) return false
    val meItem = result.body()?.content
    return meItem?.isEmailConfirmed ?: false
}

private fun BottomNavigationView.setupDeepLinks(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent
) {
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )
        // Handle Intent
        if (navHostFragment.navController.handleDeepLink(intent)
            && selectedItemId != navHostFragment.navController.graph.id
        ) {
            this.selectedItemId = navHostFragment.navController.graph.id
        }
    }
}

fun BottomNavigationView.setupItemReselected(
    graphIdToTagMap: SparseArray<String>,
    fragmentManager: FragmentManager
) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedItemTag = graphIdToTagMap[item.itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(
            navController.graph.startDestination, false
        )
    }
}

fun BottomNavigationView.reset() {
    this.selectedItemId = firstFragmentGraphId
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()

}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private infix fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}


private fun getFragmentTag(index: Int) = "bottomNavigation#$index"
private fun getIndex(fragmentTag: String) =
    fragmentTag.substringAfter("bottomNavigation#").toInt()

infix fun BottomNavigationView.switchTab(index:Int){
    val tag = getFragmentTag(index)
    graphIdToTagMap.forEach { key, value ->
        if(value == tag) this.selectedItemId = key
    }
}