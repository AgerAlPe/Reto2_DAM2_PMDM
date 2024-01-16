package com.grupo2.elorchat.ui.users.login

import android.content.Context
import android.content.Intent
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
import com.grupo2.elorchat.ui.groups.GroupActivity
import com.grupo2.elorchat.ui.users.register.RegisterActivity
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    val Context.dataStore by preferencesDataStore(name = "USER_PREFERENCES_NAME")

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

        var loginUser = LoginUser("","", "")

        btnLogin.setOnClickListener{
            //RECOGE LOS DATOS GUARDADOS Y LOS ESCRIBE EN LOS CAMPOS
            lifecycleScope.launch(Dispatchers.IO) {
                getSavedValues().collect {
                    Log.i("name",it.email)
                    Log.i("password", it.password)
                    Log.i("chbox", it.chbox.toString())
                }
            }
            if(!(email.text.isNullOrEmpty() or pass.text.isNullOrEmpty())) {
                loginUser = LoginUser(email.text.toString(),pass.text.toString(), deviceCode)
            }else {
                Log.i("errorDeUsuario", "El usuario introducido no tiene email o contraseña validos")
            }
            Log.i("user", loginUser.toString())
            viewModel.loginOfUser(loginUser)

            viewModel.loggedUser.observe(this) { result ->
                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        // Handle successful login
                        result.data?.let { data ->
                            if (data.token != null) {
                                ElorChat.userPreferences.saveAuthToken(data.token)

                                //GUARDAR LOS DATOS INTRODUCIDOS
                                lifecycleScope.launch(Dispatchers.IO) {
                                    saveValues(email.text.toString(), pass.text.toString(), chBox.isChecked)
                                }

                                if(loginUser.password.equals("Elorrieta00")){
                                    val intent = Intent(applicationContext, RegisterActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }else {
                                    val intent = Intent(applicationContext, GroupActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }else {
                                Log.d("tokenNull","the token to access is null")
                                Toast.makeText(this, "The has been an error, please try again", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Resource.Status.ERROR -> {
                        // Handle login error
                        Toast.makeText(this, "The login provided is not valid, please try again", Toast.LENGTH_SHORT).show()
                    }
                    Resource.Status.LOADING -> {
                        // Handle loading state (optional)
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.loggedUser.removeObservers(this)
    }

}