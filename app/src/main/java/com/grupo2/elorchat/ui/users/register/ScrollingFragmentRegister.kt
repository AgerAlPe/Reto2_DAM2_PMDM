package com.grupo2.elorchat.ui.users.register

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.grupo2.elorchat.R
import androidx.fragment.app.Fragment
import com.grupo2.elorchat.ui.chats.ChatsActivity

class ScrollingFragmentRegister : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_scrolling_register, container, false)

        view.findViewById<Button>(R.id.registerAcceptButton).setOnClickListener{
            view.context.startActivity(Intent(view.context, ChatsActivity::class.java))
        }

        return view
    }


}