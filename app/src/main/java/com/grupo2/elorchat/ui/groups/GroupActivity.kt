package com.grupo2.elorchat.ui.groups

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.grupo2.elorchat.R


class GroupActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewpager)

        adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)

        viewPager2.adapter = adapter

        val createChatButton: ImageButton = findViewById(R.id.createChat)
        createChatButton.setOnClickListener {
            // Lógica para manejar el clic en createChat
            val intent = Intent(this, CreateGroupActivity::class.java).apply {
                //TODO Se tiene que recoger el rol del usuario desde el DataStoreManager
                //putExtra("userRole", user.role)
            }
            startActivity(intent)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager2.currentItem = tab.position
                }
                if (viewPager2.currentItem != 2) {
                    createChatButton.visibility = View.VISIBLE
                }else{
                    createChatButton.visibility = View.GONE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
    }
}
