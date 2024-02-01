package com.grupo2.elorchat.ui.users.login

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle

import android.os.IBinder
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
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.database.entities.UserEntity
import com.grupo2.elorchat.data.database.repository.UserRepository
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.remote.RemoteUserDataSource
import com.grupo2.elorchat.ui.groups.GroupActivity
import com.grupo2.elorchat.ui.socket.SocketService
import com.grupo2.elorchat.ui.users.register.RegisterActivity
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val dataStoreManager by lazy { DataStoreManager.getInstance(ElorChat.context) }
    @Inject
    lateinit var userRepository: UserRepository
    private val userDataRepository = RemoteUserDataSource()
    private val viewModel: LoginViewModel by viewModels()

    private val socketServiceIntent by lazy {
        Intent(this, SocketService::class.java).apply {
            putExtra("SOCKET_HOST", "http://10.5.7.39:8085/")  // Update with your socket host
            putExtra("AUTHORIZATION_HEADER", "Authorization")  // Update with your authorization header
        }
    }

    private val socketServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Handle connection to the service if needed
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Handle disconnection from the service if needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.buttonAccept)
        val email = findViewById<EditText>(R.id.emailAddress)
        val pass = findViewById<EditText>(R.id.password)
        val chBox = findViewById<CheckBox>(R.id.checkBox)

        val btnChangePassword = findViewById<Button>(R.id.retrievePassButton)

        var loginUser = LoginUser("", "")

        lifecycleScope.launch(Dispatchers.IO) {
            dataStoreManager.getSavedValues().collect { savedValues ->
                withContext(Dispatchers.Main) {
                    if (savedValues.chbox) {
                        email.setText(savedValues.email)
                        pass.setText(savedValues.password)
                        chBox.isChecked = savedValues.chbox
                    }
                }
            }
        }

        //TODO ESTE BOTON NECESITA REDIRIGIRTE
        btnChangePassword.setOnClickListener {

        }

        btnLogin.setOnClickListener {
            loginUser = LoginUser(email.text.toString(), pass.text.toString())

            viewModel.loginOfUser(loginUser)

            viewModel.loggedUser.observe(this) { result ->
                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        result.data?.let { data ->
                            if (data.accessToken != null) {
                                ElorChat.userPreferences.saveAuthToken(data.accessToken)

                                lifecycleScope.launch(Dispatchers.IO) {
                                    dataStoreManager.saveValues(email.text.toString(), pass.text.toString(), chBox.isChecked)
                                }

                                viewModel.getUserData(loginUser.email)

                                startService(socketServiceIntent)
                                bindService(socketServiceIntent, socketServiceConnection, Context.BIND_AUTO_CREATE)
                            } else {
                                Toast.makeText(this, "Authentication failed. Token is null.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Resource.Status.ERROR -> {
                        Toast.makeText(this, "Login failed. Verify the email and password", Toast.LENGTH_SHORT).show()
                    }
                    Resource.Status.LOADING -> {
                        // Handle loading state (optional)
                    }
                }
            }
        }


        viewModel.userData.observe(this) { userDataResult ->
            when (userDataResult.status) {
                Resource.Status.SUCCESS -> {
                    userDataResult.data?.let { userData ->

                        lifecycleScope.launch(Dispatchers.IO) {
                            userRepository.insertUser(userData)
                        }

                        // Move the navigation logic outside this block
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
                    }
                }
                Resource.Status.ERROR -> {
                    Toast.makeText(this, "Error getting user data. Verify connection and try again", Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING -> {
                    // Handle loading state (optional)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.loggedUser.removeObservers(this)
    }
}