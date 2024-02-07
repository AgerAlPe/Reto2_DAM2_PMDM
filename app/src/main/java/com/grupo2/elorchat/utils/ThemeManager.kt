package com.grupo2.elorchat.utils

import androidx.appcompat.app.AppCompatActivity
import com.grupo2.elorchat.UserPreferences

object ThemeManager {
    fun applyAppMode(mode: Int, activity: AppCompatActivity, userPreferences: UserPreferences) {
        // Almacena el modo seleccionado
        userPreferences.saveAppMode(mode)

        // Configura el modo de la aplicación
        activity.delegate.localNightMode = mode

        // Notificar a la actividad para que pueda reiniciar y aplicar los cambios de tema
        if (activity is AppModeChangeListener) {
            activity.onAppModeChanged()
        }
    }

    interface LanguageChangeListener {
        fun onLanguageChanged()
    }

    interface AppModeChangeListener {
        fun onAppModeChanged()
    }
}