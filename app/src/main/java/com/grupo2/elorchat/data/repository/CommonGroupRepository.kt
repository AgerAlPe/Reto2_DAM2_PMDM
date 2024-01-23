package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.utils.Resource

interface CommonGroupRepository {

    suspend fun getGroups() : Resource<List<Group>>
<<<<<<< HEAD
    suspend fun createGroup(group: Group) : Resource<Int>
=======

    suspend fun createGroup(name : String, isPrivate : Boolean) : Resource<Int>
>>>>>>> 11a2ccceb51eab8c4942fa97122728a14d75b105

    suspend fun getMessagesFromGroup(groupId : Int) : Resource<List<Message>>
}