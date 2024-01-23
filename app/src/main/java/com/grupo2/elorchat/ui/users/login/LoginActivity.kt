package com.grupo2.elorchat.ui.users.login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.R
import androidx.appcompat.app.AppCompatActivity;
import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.remote.RemoteUserDataSource
import com.grupo2.elorchat.ui.groups.GroupActivity
import com.grupo2.elorchat.ui.users.register.RegisterActivity
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {

    private val dataStoreManager by lazy { DataStoreManager.getInstance(ElorChat.context) }
    private lateinit var loginUser: LoginUser

    private val userRepository = RemoteUserDataSource()
    private val viewModel: LoginViewModel by viewModels { LoginViewModelFactory(
        userRepository
    ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.buttonAccept)
        val email = findViewById<EditText>(R.id.emailAddress)
        val pass = findViewById<EditText>(R.id.password)
        val chBox = findViewById<CheckBox>(R.id.checkBox)
        val deviceCode = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

        lifecycleScope.launch(Dispatchers.IO) {
            dataStoreManager.getSavedValues().collect { savedValues ->
                withContext(Dispatchers.Main) {
                    Log.i("name", savedValues.email)
                    Log.i("password", savedValues.password)
                    Log.i("chbox", savedValues.chbox.toString())

                    if (savedValues.chbox) {
                        email.setText(savedValues.email)
                        pass.setText(savedValues.password)
                        chBox.isChecked = savedValues.chbox
                    }
                }
            }
        }

        btnLogin.setOnClickListener {
            if (!(email.text.isNullOrEmpty() or pass.text.isNullOrEmpty())) {
                loginUser = LoginUser(email.text.toString(), pass.text.toString())
            } else {
                Log.i("errorDeUsuario", "El usuario introducido no tiene email o contraseña válidos")
                Toast.makeText(this, "The user provided has no valid email or password", Toast.LENGTH_SHORT).show()
            }

            if (loginUser != null) {
            viewModel.loginOfUser(loginUser)
            }

            viewModel.loggedUser.observe(this) { result ->
                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        result.data?.let { data ->
                            if (data.accessToken != null) {
                                ElorChat.userPreferences.saveAuthToken(data.accessToken)

                                lifecycleScope.launch(Dispatchers.IO) {
                                    dataStoreManager.saveValues(email.text.toString(), pass.text.toString(), chBox.isChecked)
                                }

                                if (loginUser.password == "Elorrieta00") {
                                    val intent = Intent(applicationContext, RegisterActivity::class.java)
                                    intent.putExtra("userEmail", loginUser.email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val intent = Intent(applicationContext, GroupActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                Log.d("tokenNull", "the token to access is null")
                                Toast.makeText(this, "There has been an error, please try again", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Resource.Status.ERROR -> {
                        Toast.makeText(this, "The login provided is not valid, please try again", Toast.LENGTH_SHORT).show()
                    }
                    Resource.Status.LOADING -> {
                        // Handle loading state (optional)
                    }
                }
            }
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.loggedUser.removeObservers(this)
    }

}