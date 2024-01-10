package com.grupo2.elorchat.ui.users.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.grupo2.elorchat.R
import com.grupo2.elorchat.ui.users.register.RegisterActivity
import com.grupo2.elorchat.ui.users.register.ScrollingFragmentRegister

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.buttonAccept).setOnClickListener{
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        //val primeraVez = false

        //TODO
        //Miramos si la contraseña es la por defecto o no.

//      if(primeraVez) {
//          Vamos a la página de registro
//      }else {
//          Vamos directamente a la página de chats
//      }
    }
}