package com.grupo2.elorchat.ui.groups.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.grupo2.elorchat.R
import com.grupo2.elorchat.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        // Configura el Spinner
        val languageSpinner = binding.languageSpinner
        val languageArray = resources.getStringArray(R.array.languages_array) // Crea un array en res/values/arrays.xml
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languageArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Maneja la selección del idioma
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Manejar la selección del idioma
                val selectedLanguage = languageArray[position]
                // Puedes guardar la selección en las preferencias compartidas o aplicar cambios según necesites
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejar caso de nada seleccionado
            }
        }

        return view
    }
}
