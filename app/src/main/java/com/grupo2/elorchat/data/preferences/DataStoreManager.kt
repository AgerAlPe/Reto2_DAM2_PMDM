package com.grupo2.elorchat.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.grupo2.elorchat.data.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(context: Context) {

    private val appContext = context.applicationContext
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "USER_PREFERENCES")

    suspend fun saveValues(name: String, password: String, checked: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("name")] = name
            preferences[stringPreferencesKey("password")] = password
            preferences[booleanPreferencesKey("checked")] = checked
        }
    }

    fun getSavedValues(): Flow<UserProfile> = appContext.dataStore.data.map { preferences ->
        UserProfile(
            email = preferences[stringPreferencesKey("name")] ?: "",
            password = preferences[stringPreferencesKey("password")] ?: "",
            chbox = preferences[booleanPreferencesKey("checked")] ?: false
        )
    }

    suspend fun saveRoles(roles: List<Role>) {
        appContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("roles")] = roles.joinToString { it.name }
        }
    }

    fun getSavedRoles(): Flow<List<Role>> = appContext.dataStore.data.map { preferences ->
        val rolesString = preferences[stringPreferencesKey("roles")] ?: ""
        rolesString.split(",").map { Role(it.trim().toInt(), "Role Name") }
    }


}
