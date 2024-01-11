package com.grupo2.elorchat.ui.chats

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.grupo2.elorchat.ui.socketMessage.SocketActivity

class FragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return if(position == 0) {
            PrivateChatsFragment()
        }else if (position == 1) {
            PublicChatsFragment()
        }else {
            SettingsFragment()
        }
    }

}