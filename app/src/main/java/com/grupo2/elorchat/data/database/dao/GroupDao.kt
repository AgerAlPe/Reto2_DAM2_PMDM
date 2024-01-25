package com.grupo2.elorchat.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.grupo2.elorchat.data.database.entities.GroupEntity
import com.grupo2.elorchat.data.database.entities.MessageEntity

@Dao
interface GroupDao {

    @Query("SELECT * FROM group_table")
    suspend fun getAllGroup():List<GroupEntity>

    @Query("SELECT * FROM group_table where id = :userId")
    suspend fun getAllUserGroup(userId:Int):List<GroupEntity>
}