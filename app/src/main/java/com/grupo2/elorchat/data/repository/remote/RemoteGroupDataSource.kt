package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.ChangePasswordRequest
import com.grupo2.elorchat.data.ChatUser
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.CommonGroupRepository


class RemoteGroupDataSource: BaseDataSource(), CommonGroupRepository {
    override suspend fun getGroups() = getResult {
        RetrofitClient.apiInterface.getGroups()
    }

    override suspend fun createGroup(group : Group) = getResult {
        RetrofitClient.apiInterface.createGroup(group)
    }
    
    override suspend fun getMessagesFromGroup(groupId : Int) = getResult {
        RetrofitClient.apiInterface.getMessagesFromGroup(groupId)
    }


    override suspend fun joinChat(chatUser: ChatUser) = getResult{
        RetrofitClient.apiInterface.joinChat(chatUser)
    }

    override suspend fun leaveChat(userId : Int, chatId : Int) = getResult {
        RetrofitClient.apiInterface.leaveChat(userId, chatId)

    }

    override suspend fun getUserGroups(userId: Int) = getResult {
        RetrofitClient.apiInterface.getUserGroups(userId)
    }

    override suspend fun changePassword(changePasswordRequest: ChangePasswordRequest) = getResult {
        RetrofitClient.apiInterface.changePassword(changePasswordRequest)
    }
}