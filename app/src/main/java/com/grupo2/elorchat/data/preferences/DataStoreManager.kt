package com.grupo2.elorchat.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(context: Context) {

    private val appContext = context.applicationContext
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "USER_PREFERENCES")

    suspend fun saveValues(email: String, password: String, checked: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("email")] = email
            preferences[stringPreferencesKey("password")] = password
            preferences[booleanPreferencesKey("checked")] = checked
        }
    }

    fun getSavedValues(): Flow<UserProfile> = appContext.dataStore.data.map { preferences ->
        UserProfile(
            email = preferences[stringPreferencesKey("email")] ?: "",
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

    suspend fun saveUserId(userId: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[intPreferencesKey("userId")] = userId
        }
    }

    fun getSavedUserId(): Flow<Int> = appContext.dataStore.data.map { preferences ->
        preferences[intPreferencesKey("userId")] ?: 0 // Default value 0 or handle as needed
    }

    suspend fun saveGroups(groups: List<Group>) {
        appContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("groups")] = groups.joinToString { group ->
                "${group.id},${group.name},${group.isPrivate}"
            }
        }
    }

    fun getSavedGroups(): Flow<List<Group>> = appContext.dataStore.data.map { preferences ->
        val groupsString = preferences[stringPreferencesKey("groups")] ?: ""
        groupsString.split(",").chunked(3) { chunk ->
            Group(chunk[0].toInt(), chunk[1], chunk[2].toBoolean())
        }
    }

    companion object {
        private var instance: DataStoreManager? = null
        fun getInstance(context: Context): DataStoreManager {
            return instance ?: synchronized(this) {
                instance ?: DataStoreManager(context).also { instance = it }
            }
        }
    }

}
