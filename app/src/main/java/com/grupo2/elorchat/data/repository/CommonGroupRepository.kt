package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.data.ChangePasswordRequest
import com.grupo2.elorchat.data.ChatUser
import com.grupo2.elorchat.data.ChatUserEmailRequest
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.utils.Resource

interface CommonGroupRepository {

    suspend fun getGroups() : Resource<List<Group>>

    suspend fun createGroup(group: Group) : Resource<Int>

    suspend fun getMessagesFromGroup(groupId : Int) : Resource<List<Message>>

    suspend fun joinChat (chatUser: ChatUser) : Resource<ChatUser>

    suspend fun leaveChat (userId : Int , chatId : Int) : Resource<Void>

    suspend fun getUserGroups(userId : Int) : Resource<List<Group>>

    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest) : Resource<Void>

    suspend fun getChatsWhereUserIsAdmin(userId: Int) : Resource<List<Group>>

    suspend fun makeAnUserJoinAChat(chatUserEmailRequest: ChatUserEmailRequest) : Resource<String>

    suspend fun makeAnUserLeaveAChat(chatUserEmailRequest: ChatUserEmailRequest) : Resource<String>

}