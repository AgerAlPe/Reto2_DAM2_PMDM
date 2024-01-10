package com.grupo2.elorchat.ui.users.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.grupo2.elorchat.R
import androidx.fragment.app.Fragment

class ScrollingFragmentRegister : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scrolling_register, container, false)
    }
}