package com.grupo2.elorchat.utils

import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.grupo2.elorchat.UserPreferences
import java.util.Locale

object LanguageManager {

    fun applyLanguage(languageCode: String, activity: AppCompatActivity, userPreferences: UserPreferences) {
        // Verificar si el idioma actual es igual al idioma que se está tratando de aplicar
        if (userPreferences.fetchSelectedLanguage() != languageCode) {
            // Almacena el idioma seleccionado
            userPreferences.saveSelectedLanguage(languageCode)

            // Configura el idioma de la aplicación
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = Configuration().apply {
                setLocale(locale)
            }
            activity.resources.updateConfiguration(config, activity.resources.displayMetrics)

            // Postergar la recreación de la actividad
            Handler(Looper.getMainLooper()).postDelayed({
                activity.recreate()
            }, 100) // Ajusta este valor según sea necesario
        }
    }
}

