package com.grupo2.elorchat.ui.groups

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.ui.groups.privategroups.PrivateGroupsFragment
import com.grupo2.elorchat.ui.groups.publicgroups.PublicGroupsFragment
import com.grupo2.elorchat.ui.groups.settings.SettingsFragment

class FragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val appDatabase: AppDatabase
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return if(position == 0) {
            PrivateGroupsFragment()
        }else if (position == 1) {
            PublicGroupsFragment()
        }else {
            SettingsFragment()
        }
    }

}