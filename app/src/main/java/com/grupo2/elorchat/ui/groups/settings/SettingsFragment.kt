package com.grupo2.elorchat.ui.groups.settings

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
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
import android.widget.TextView
import androidx.core.app.ActivityCompat.recreate

import com.grupo2.elorchat.databinding.FragmentSettingsBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.ui.users.login.LoginActivity
import com.grupo2.elorchat.utils.Resource
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

        val languageSpinner = binding.languageSpinner

        // Obtener la lista de idiomas disponibles
        val availableLanguages = getAvailableLanguages()

        // Crear un ArrayAdapter usando la lista de idiomas
        val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availableLanguages)

        // Establecer el estilo del dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Establecer el adaptador en el Spinner
        languageSpinner.adapter = adapter

        // Manejar la selección del idioma
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Cambiar el idioma cuando se selecciona un nuevo elemento en el Spinner
                val selectedLanguage = availableLanguages[position]
                setLocale(selectedLanguage)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // No es necesario hacer nada aquí
            }
        }

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return view
    }

    // Método para obtener la lista de idiomas disponibles
    private fun getAvailableLanguages(): List<String> {
        val languages: MutableList<String> = ArrayList()
        val resDir = File(requireContext().applicationInfo.sourceDir)
        val valuesDirs = resDir.parentFile.listFiles { file -> file.isDirectory && file.name.startsWith("values") }

        valuesDirs?.forEach { valuesDir ->
            // Buscar el archivo strings.xml en cada directorio values
            val stringsFile = File(valuesDir, "strings.xml")
            if (stringsFile.exists()) {
                // Extraer el nombre del idioma del nombre del directorio values
                val language = valuesDir.name.replace("values-", "")
                languages.add(language)
            }
        }

        return languages
    }




    // Método para cambiar el idioma de la aplicación
    private fun setLocale(languageCode: String) {
        val resources: Resources = resources
        val config: Configuration = resources.configuration
        val dm: DisplayMetrics = resources.displayMetrics
        config.setLocale(Locale(languageCode))
        resources.updateConfiguration(config, dm)

        // Recargar la actividad para que los cambios tengan efecto
        requireActivity().recreate()
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