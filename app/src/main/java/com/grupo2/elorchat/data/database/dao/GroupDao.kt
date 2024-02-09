package com.grupo2.elorchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.database.entities.GroupEntity
import com.grupo2.elorchat.data.database.entities.MessageEntity

@Dao
interface GroupDao {

    @Query("SELECT * FROM group_table WHERE is_private = 0")
    suspend fun getAllPublicGroups(): List<GroupEntity>

    @Query("SELECT * FROM group_table WHERE is_private = 1")
    suspend fun getAllPrivateGroups(): List<GroupEntity>

    @Query("SELECT * FROM group_table where id = :userId")
    suspend fun getAllUserGroup(userId:Int):List<GroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<GroupEntity>)

    @Query("SELECT * FROM group_table WHERE id = :groupId")
    suspend fun getGroupById(groupId: Int): GroupEntity?

    @Query("Delete FROM group_table WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Int): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)
}