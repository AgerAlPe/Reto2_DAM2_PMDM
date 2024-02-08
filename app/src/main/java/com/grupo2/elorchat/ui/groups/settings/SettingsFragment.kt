package com.grupo2.elorchat.ui.groups.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.ChangePasswordRequest

import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.ElorChat.Companion.userPreferences
import com.grupo2.elorchat.UserPreferences

import com.grupo2.elorchat.databinding.FragmentSettingsBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.ui.users.login.LoginActivity
import com.grupo2.elorchat.utils.LanguageManager
import com.grupo2.elorchat.utils.Resource
import com.grupo2.elorchat.utils.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

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

        //TODO Hay que conseguir los datos del usuario logueado
        //val loggedInUser = getLoggedInUser()
        //view.findViewById<TextView>(R.id.titleTextView).text = loggedInUser.userName
        //view.findViewById<TextView>(R.id.subtitleTextView).text = loggedInUser.subtitle
        //view.findViewById<TextView>(R.id.descriptionTextView).text = loggedInUser.description

        val languageToggleGroup: MaterialButtonToggleGroup = binding.languageToggleGroup
        val appModeToggleGroup: MaterialButtonToggleGroup = binding.appModeToggleGroup

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

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        when (userPreferences.fetchSelectedLanguage()) {
            "en" -> languageToggleGroup.check(R.id.englishButton)
            "es" -> languageToggleGroup.check(R.id.spanishButton)
            "eu" -> languageToggleGroup.check(R.id.basqueButton)
        }

        languageToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.englishButton -> LanguageManager.applyLanguage("en", requireActivity() as AppCompatActivity, userPreferences)
                    R.id.spanishButton -> LanguageManager.applyLanguage("es", requireActivity() as AppCompatActivity, userPreferences)
                    R.id.basqueButton -> LanguageManager.applyLanguage("eu", requireActivity() as AppCompatActivity, userPreferences)
                }
            }
        }

        when (userPreferences.fetchAppMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> appModeToggleGroup.check(R.id.lightModeButton)
            AppCompatDelegate.MODE_NIGHT_YES -> appModeToggleGroup.check(R.id.darkModeButton)
        }

        appModeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.lightModeButton -> {
                        ThemeManager.applyAppMode(
                            AppCompatDelegate.MODE_NIGHT_NO,
                            activity as AppCompatActivity,
                            userPreferences
                        )
                    }
                    R.id.darkModeButton -> {
                        ThemeManager.applyAppMode(
                            AppCompatDelegate.MODE_NIGHT_YES,
                            activity as AppCompatActivity,
                            userPreferences
                        )
                    }
                }
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

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.confirm_logout_title))
            .setMessage(getString(R.string.confirm_logout_message))
            .setPositiveButton(getString(R.string.positive_button_yes)) { _, _ ->
                showToast(getString(R.string.closing_session))
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton(getString(R.string.negative_button_no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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