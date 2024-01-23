package com.grupo2.elorchat.ui.users.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.ui.groups.GroupActivity
import com.grupo2.elorchat.ui.users.register.RegisterActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_login)

        val dataStoreManager by lazy { DataStoreManager(this) }

        lifecycleScope.launch(Dispatchers.IO) {
            dataStoreManager.getSavedValues().collect { savedValues ->
                withContext(Dispatchers.Main) {
                    Log.i("email", savedValues.email)
                    Log.i("password", savedValues.password)
                    Log.i("chbox", savedValues.chbox.toString())

                    if (savedValues.chbox && savedValues.email.isNotEmpty() && savedValues.password.isNotEmpty()) {
                        val intent = Intent(applicationContext, GroupActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }


    }
}