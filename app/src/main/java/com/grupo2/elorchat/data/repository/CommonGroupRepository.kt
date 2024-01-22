package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.RegisterUser
import com.grupo2.elorchat.utils.Resource

interface CommonGroupRepository {

    suspend fun getGroups() : Resource<List<Group>>
    suspend fun createGroup(name : String, isPrivate : Boolean) : Resource<Int>

}