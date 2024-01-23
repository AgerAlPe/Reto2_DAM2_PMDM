package com.grupo2.elorchat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.grupo2.elorchat.ui.users.login.LoginActivity
import com.grupo2.elorchat.utils.isOnline

class MainActivity : AppCompatActivity() {

    private var connection : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)

        screenSplash.setKeepOnScreenCondition {true}

        connection = isOnline(this)
        if (connection) {
            Toast.makeText(this, "there is connection", Toast.LENGTH_LONG).show()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.putExtra("isConnected", connection)
            startActivity(intent)
            finish()
        }else {
            //TODO hay que hacer que cuando no haya conexión y el usuario ya se haya logueado, que directamente inicie sesión.
            Toast.makeText(this, "there is NO connection", Toast.LENGTH_LONG).show()
        }
    }
}