package com.grupo2.elorchat.utils

import android.util.Log
import com.grupo2.elorchat.data.database.repository.ChatUserRepository
import com.grupo2.elorchat.data.database.repository.GroupRepository
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import javax.inject.Inject

class UserInformationManager @Inject constructor(
    private val localGroupRepository: GroupRepository,
    private val localChatUserRepository: ChatUserRepository,
    private val remoteRepository: CommonGroupRepository
) {
    // Método para sincronizar la base de datos local y la remota
    suspend fun syncUserInfo(userId: Int) {
        try {
            val localUserGroupsResource = localGroupRepository.getAllUserGroups(userId)
            val localChatUserResource = localChatUserRepository.getUserChats(userId)


            val remoteChatUserResource = remoteRepository.getChatUser(userId).data ?: emptyList()

            val remoteUserGroups = remoteRepository.getUserGroups(userId).data?.toMutableList() ?: mutableListOf()
            val remotePublicGroups = remoteRepository.getPublicGroups().data ?: emptyList()

            for (remotePublicGroup in remotePublicGroups) {
                if (!remoteUserGroups.any { it.id == remotePublicGroup.id }) {
                    remoteUserGroups.add(remotePublicGroup)
                }
            }

            // group
            if (localUserGroupsResource.isNotEmpty()) {

                for (remoteUserGroup in remoteUserGroups) {
                    Log.i("sync", "lista remota: " + remoteUserGroup.name)
                    if (!localUserGroupsResource.any { it.name === remoteUserGroup.name}) {
                        localGroupRepository.insertGroup(remoteUserGroup)
                    }
                }

            } else {
                // Si no hay información en la local, obtenerla del backend
                localGroupRepository.insertGroups(remoteUserGroups)
            }

            // chatUser
            if (localChatUserResource.isNotEmpty()) {

                for (remoteChatUser in remoteChatUserResource) {
                    if (!localChatUserResource.any { it.id == remoteChatUser.id }) {
                        localChatUserRepository.insertChatUser(remoteChatUser)
                    }
                }

            } else {
                // Si no hay información en la local, obtenerla del backend
                localChatUserRepository.insertChatUsers(
                    remoteRepository.getChatUser(userId).data ?: emptyList()
                )
            }

        } catch (e: Exception) {
            // Manejar la excepción
        }
    }

}
