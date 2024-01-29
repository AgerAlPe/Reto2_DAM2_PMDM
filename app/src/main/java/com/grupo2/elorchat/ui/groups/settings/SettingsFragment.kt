package com.grupo2.elorchat.ui.groups.settings

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.ChangePasswordRequest
import com.grupo2.elorchat.data.database.AppDatabase

import android.widget.AdapterView
import android.widget.ArrayAdapter

import com.grupo2.elorchat.databinding.FragmentSettingsBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val viewModel: GroupViewModel by viewModels()
    private lateinit var binding: FragmentSettingsBinding
    private var alertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        view.findViewById<Button>(R.id.forgotPasswordButton).setOnClickListener {
            lifecycleScope.launch {
                viewModel.loadFirstUserEmail()
                val userEmail = viewModel.firstUserEmail.value
                if (userEmail != null) {
                    showForgotPasswordDialog(userEmail)
                } else {
                    Log.i("userEmailCheck", "userEmail is null")
                }
            }
        }

        // Configura el Spinner
        val languageSpinner = binding.languageSpinner
        val languageArray = resources.getStringArray(R.array.languages_array) // Crea un array en res/values/arrays.xml
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languageArray
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Maneja la selección del idioma
        languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
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

    private fun showForgotPasswordDialog(userEmail: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)

        val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditText)
        val oldPasswordEditText =
            dialogView.findViewById<EditText>(R.id.oldPasswordEditText)
        val newPasswordEditText1 =
            dialogView.findViewById<EditText>(R.id.newPasswordEditText1)
        val newPasswordEditText2 =
            dialogView.findViewById<EditText>(R.id.newPasswordEditText2)

        // Set the email retrieved from GroupViewModel to the emailEditText
        emailEditText.setText(userEmail)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                handleSubmitClick(
                    emailEditText.text.toString(),
                    oldPasswordEditText.text.toString(),
                    newPasswordEditText1.text.toString(),
                    newPasswordEditText2.text.toString()
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog on cancel
            }

        alertDialog = builder.create()
        alertDialog?.show()
    }

    private fun handleSubmitClick(
        email: String,
        oldPassword: String,
        newPassword1: String,
        newPassword2: String
    ) {
        if (validateInput(email, oldPassword, newPassword1, newPassword2)) {
            val changePasswordRequest =
                ChangePasswordRequest(email, oldPassword, newPassword1)
            viewModel.changeUserPassword(changePasswordRequest)
            viewModel.changePassword.observe(viewLifecycleOwner, Observer { result ->
                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        // Handle success
                        showToast("Password changed successfully")
                        // Dismiss the dialog on success
                        alertDialog?.dismiss()
                    }

                    Resource.Status.ERROR -> {
                        // Handle error, show appropriate error message
                        showToast("Error changing password: ${result.message}")
                    }

                    Resource.Status.LOADING -> {
                        // Handle loading state if needed
                    }
                }
            })
        } else {
            // Handle validation failure
            showToast("Invalid input, please check your entries.")
        }
    }

    private fun validateInput(
        email: String,
        oldPassword: String,
        newPassword1: String,
        newPassword2: String
    ): Boolean {
        // Implement your validation logic here
        // Return true if the input is valid, false otherwise
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex()) &&
                newPassword1 == newPassword2 &&
                newPassword1 != oldPassword
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}