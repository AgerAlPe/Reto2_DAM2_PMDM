package com.grupo2.elorchat.ui.chats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.grupo2.elorchat.R
class PrivateChatsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //El codigo debe de ir antes de que se devuelva la view
        return inflater.inflate(R.layout.fragment_private_chats, container, false)
    }
}