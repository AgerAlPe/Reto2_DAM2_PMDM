package com.grupo2.elorchat

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    companion object {
        const val USER_TOKEN = "user_token"
        const val SELECTED_LANGUAGE = "selected_language"
        const val APP_MODE = "app_mode"
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

    //TODO Esto no funciona, tendria que almacenar y aplicar
    /**
     * Function to save selected language
     */
    fun saveSelectedLanguage(languageCode: String) {
        val editor = sharedPreferences.edit()
        editor.putString(SELECTED_LANGUAGE, languageCode)
        editor.apply()
    }

    /**
     * Function to fetch selected language
     */
    fun fetchSelectedLanguage(): String? {
        return sharedPreferences.getString(SELECTED_LANGUAGE, null)
    }

    /**
     * Function to save app mode (day/night)
     */
    fun saveAppMode(mode: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(APP_MODE, mode)
        editor.apply()
    }

    /**
     * Function to fetch app mode (day/night)
     */
    fun fetchAppMode(): Int {
        return sharedPreferences.getInt(APP_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
