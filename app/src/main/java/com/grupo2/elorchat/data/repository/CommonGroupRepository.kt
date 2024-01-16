package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.utils.Resource

interface CommonGroupRepository {
    suspend fun getSongs() : Resource<List<Group>>
}