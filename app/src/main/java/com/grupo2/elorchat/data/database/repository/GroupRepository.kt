package com.grupo2.elorchat.data.database.repository

import androidx.lifecycle.LiveData
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.database.dao.GroupDao
import com.grupo2.elorchat.data.database.entities.GroupEntity
import com.grupo2.elorchat.data.database.entities.toGroup
import com.grupo2.elorchat.data.database.entities.toGroupEntity
import javax.inject.Inject

class GroupRepository @Inject constructor(private val groupDao: GroupDao) {

    suspend fun getAllPublicGroups(): List<Group> {
        return groupDao.getAllPublicGroups().map { it.toGroup() }
    }

    suspend fun getAllPrivateGroups(): List<Group> {
        return groupDao.getAllPrivateGroups().map { it.toGroup() }
    }

    suspend fun getAllUserGroups(userId: Int): List<Group> {
        return groupDao.getAllUserGroup(userId).map { it.toGroup() }
    }

    suspend fun insertGroups(groups: List<Group>) {
        val groupEntities = groups.map { it.toGroupEntity() }
        groupDao.insertAll(groupEntities)
    }

    suspend fun getGroupById(groupId: Int): Group? {
        val groupEntity = groupDao.getGroupById(groupId)
        return groupEntity?.toGroup()
    }
}


