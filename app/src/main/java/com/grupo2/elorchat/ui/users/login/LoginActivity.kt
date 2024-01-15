package com.grupo2.elorchat.ui.users.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.preferences.UserProfile
import com.grupo2.elorchat.data.repository.remote.RemoteUserDataSource
import com.grupo2.elorchat.ui.users.register.RegisterActivity
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

val Context.dataStore by preferencesDataStore(name = "USER_PREFERENCES_NAME")
private const val PREFS_FILENAME = "LoginPrefs"

class LoginActivity : AppCompatActivity() {
    private val userRepository = RemoteUserDataSource()
    private val viewModel: LoginViewModel by viewModels { LoginViewModelFactory(
        userRepository
    ) }

    // private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // sharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        val btnLogin = findViewById<Button>(R.id.buttonAccept)
        val email = findViewById<EditText>(R.id.emailAddress)
        val pass = findViewById<EditText>(R.id.password)
        val chBox = findViewById<CheckBox>(R.id.checkBox)
        var deviceCode = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

        if (deviceCode.isNullOrEmpty()) {
            deviceCode = "testphone"
        }

        var loginUser = LoginUser("email","password", deviceCode)

        btnLogin.setOnClickListener{

            //RECOGE LOS DATOS GUARDADOS
            lifecycleScope.launch(Dispatchers.IO) {
                getSavedValues().collect {
                    Log.i("name",it.email)
                    Log.i("password", it.password)
                    Log.i("chbox", it.chbox.toString())
                    if(it.email.isNotEmpty() && it.password.isNotEmpty() && it.chbox.toString() == "true") {
                        email.setText(it.email)
                        pass.setText(it.password)
                        chBox.isChecked = it.chbox
                    }
                    loginUser = LoginUser(it.email, it.password, deviceCode)
                }
            }
            viewModel.loginOfUser(loginUser)

            viewModel.loggedUser.observe(this) { result ->
                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        Log.i("QueryStatus", result.data.toString())
                        // Handle successful login
                        result.data?.let { data ->
                            ElorChat.userPreferences.saveAuthToken(data.accessToken)
                            //GUARDAR LOS DATOS INTRODUCIDOS
                            lifecycleScope.launch(Dispatchers.IO) {
                                saveValues(email.text.toString(), pass.text.toString(), chBox.isChecked)
                            }

                            val intent = Intent(applicationContext, RegisterActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    Resource.Status.ERROR -> {
                        // Handle login error
                        Toast.makeText(this, "The login provided is not valid, please try again", Toast.LENGTH_SHORT).show()
                    }
                    Resource.Status.LOADING -> {
                        // Handle loading state (optional)
                        // You can show a loading indicator or perform other actions while waiting
                    }
                }
            }
        }


    }
    private suspend fun saveValues(name: String, password: String, checked: Boolean) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("name")] = name
            preferences[stringPreferencesKey("password")] = password
            preferences[booleanPreferencesKey("checked")] = checked
        }
    }

    private fun getSavedValues() = dataStore.data.map { preferences ->
        UserProfile(
            email = preferences[stringPreferencesKey("name")].orEmpty(),
            password = preferences[stringPreferencesKey("password")].orEmpty(),
            chbox = preferences[booleanPreferencesKey("checked")] ?: false
        )
    }

}