package com.grupo2.elorchat

import android.content.Context
import android.content.SharedPreferences

class UserPreferences() {

    private val sharedPreferences: SharedPreferences by lazy {
        ElorChat.context.getSharedPreferences(ElorChat.context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    companion object {
        const val USER_TOKEN = "user_token"
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return sharedPreferences.getString(USER_TOKEN, null)
    }
}