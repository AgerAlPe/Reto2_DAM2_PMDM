package com.grupo2.elorchat.ui.users.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.RegisterUser
import com.grupo2.elorchat.data.Role
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.remote.RemoteUserDataSource
import com.grupo2.elorchat.ui.groups.GroupActivity
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RegisterActivity : AppCompatActivity() {

    private val dataStoreManager by lazy { DataStoreManager.getInstance(ElorChat.context) }
    private val userRepository = RemoteUserDataSource()
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_scrolling_register)

        var id = 0
        val name = findViewById<EditText>(R.id.registerName)
        val surnames = findViewById<EditText>(R.id.registerSurname)
        val email = findViewById<EditText>(R.id.registerEmail)
        val direction = findViewById<EditText>(R.id.registerDirection)
        val phoneNumber = findViewById<EditText>(R.id.registerPhone)
        val dni = findViewById<EditText>(R.id.registerDni)
        val fctDual = findViewById<EditText>(R.id.registerFctDual)

        var stringRoles: String = ""

        var roles: List<Role>? = null

        val pass1 = findViewById<EditText>(R.id.firstPassword)
        val pass2= findViewById<EditText>(R.id.secondPassword)

        val userEmail = intent.getStringExtra("userEmail")

        if (userEmail != null) {
            viewModel.registerOfUser(userEmail)
            Log.i("",userEmail)
        } else {
            Log.i("userEmail", "userEmail is null")
        }


        viewModel.registeringUser.observe(this) { result ->
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    // Handle successful login
                    result.data?.let { data ->
                        email.setText(data.email)
                        name.setText(data.name)
                        surnames.setText(data.surnames)
                        phoneNumber.setText(data.phoneNumber)
                        dni.setText(data.dni)
                        direction.setText(data.direction)

                        //STORE USER ID
                        id = data.id!!
                        lifecycleScope.launch(Dispatchers.IO) {
                            dataStoreManager.saveUserId(id)
                        }

                        //STORE ROLES
                        roles = data.roles
                        lifecycleScope.launch(Dispatchers.IO) {
                            dataStoreManager.saveRoles(roles!!)
                        }

                        //STORE THE GROUPS OF THIS USER
                        lifecycleScope.launch(Dispatchers.IO) {
                            dataStoreManager.saveGroups(data.chats)
                        }

                        stringRoles = (data.roles?.map { it.name }).toString()

                        Log.i("userEntero", data.toString())
                        Log.i("solo los roles", stringRoles)
                        if (data.fctDual) {
                            // ESCRIBE DUAL
                           fctDual.setText(getString(R.string.dual))
                        } else {
                            // ESCRIBE FCT
                            fctDual.setText(getString(R.string.fct))
                        }
                    }
                }
                Resource.Status.ERROR -> {
                    // Handle login error
                    Toast.makeText(
                        this,
                        "There has been an error fetching your information, please restart",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Resource.Status.LOADING -> {
                    // Handle loading state (optional)
                    // You can show a loading indicator or perform other actions while waiting
                }
            }
        }

        findViewById<Button>(R.id.registerAcceptButton).setOnClickListener {
            if(!pass1.text.isNullOrEmpty()) {
                if (pass1.text.toString().equals(pass2.text.toString()) && pass1.text.toString() != "Elorrieta00") {

                    val user = RegisterUser(name.text.toString(), surnames.text.toString() , dni.text.toString(), email.text.toString(), direction.text.toString(), phoneNumber.text.toString(), fctDual.text.equals("true"), pass1.text.toString())

                    Log.i("el usuario que se envia", user.toString())

                        viewModel.updateRegisterOfUser(id, user)


                    viewModel.updateRegisteredUser.observe(this) { result ->
                        when (result.status) {
                            Resource.Status.SUCCESS -> {
                                // Handle successful login
                                result.data?.let { int ->
                                    if (int == 1){
                                        Toast.makeText(this, "Updated successfuly, enjoy", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(applicationContext, GroupActivity::class.java)
                                        intent.putExtra("userRoles", stringRoles)
                                        startActivity(intent)
                                        finish()
                                    }else {
                                        Toast.makeText(this, "Error while updating, please restart and try again", Toast.LENGTH_SHORT).show()
                                        Log.i("RegisterButtonError", "No se ha actualizado el usuario")
                                    }
                                }
                            }
                            Resource.Status.ERROR -> {
                                // Handle login error
                                Toast.makeText(this, "Error while updating, please restart and try again", Toast.LENGTH_SHORT).show()
                            }
                            Resource.Status.LOADING -> {
                                // Handle loading state (optional)
                            }
                        }
                    }

                    startActivity(Intent(this, GroupActivity::class.java))
                    finish()
                }else {
                    Toast.makeText(this, "The password do not match, or they have base value", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}