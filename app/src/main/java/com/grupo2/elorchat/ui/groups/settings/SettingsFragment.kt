package com.grupo2.elorchat.ui.groups.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.grupo2.elorchat.R
class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //El codigo debe de ir antes de que se devuelva la view
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
}