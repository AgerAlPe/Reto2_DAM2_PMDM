package com.grupo2.elorchat.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grupo2.elorchat.data.Role
import com.grupo2.elorchat.data.database.entities.GroupEntity

class GroupListConverter {
    @TypeConverter
    fun fromString(value: String): List<GroupEntity> {
        val listType = object : TypeToken<List<GroupEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<GroupEntity>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
