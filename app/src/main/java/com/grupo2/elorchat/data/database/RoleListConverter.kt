package com.grupo2.elorchat.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grupo2.elorchat.data.Role

class RoleListConverter {
    @TypeConverter
    fun fromString(value: String): List<Role> {
        val listType = object : TypeToken<List<Role>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Role>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}