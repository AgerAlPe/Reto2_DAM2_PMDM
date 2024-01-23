package com.grupo2.elorchat

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.grupo2.elorchat.ui.users.login.LoginActivity
import com.grupo2.elorchat.ui.users.login.OfflineLoginActivity
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
            Toast.makeText(this, "there is no connection", Toast.LENGTH_LONG).show()
            val intent = Intent(applicationContext, OfflineLoginActivity::class.java)
            intent.putExtra("isConnected", connection)
            startActivity(intent)
            finish()
        }
    }
}