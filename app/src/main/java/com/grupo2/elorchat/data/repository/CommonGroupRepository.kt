package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.utils.Resource

interface CommonGroupRepository {

    suspend fun getGroups() : Resource<List<Group>>

    suspend fun createGroup(name : String, isPrivate : Boolean) : Resource<Int>

    suspend fun getMessagesFromGroup(groupId : Int) : Resource<List<Message>>
}