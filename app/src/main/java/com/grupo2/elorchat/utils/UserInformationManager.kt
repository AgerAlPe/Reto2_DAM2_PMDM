package com.grupo2.elorchat.utils

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

            val combinedGroups = (remoteRepository.getUserGroups(userId).data
                ?: emptyList()) + (remoteRepository.getPublicGroups().data ?: emptyList())
            val remoteUserGroups = combinedGroups.distinct()

            // group
            if (localUserGroupsResource.isNotEmpty()) {
                val mergedGroups = (remoteUserGroups + localUserGroupsResource).distinct()

                localGroupRepository.insertGroups(mergedGroups)
            } else {
                // Si no hay información en la local, obtenerla del backend
                localGroupRepository.insertGroups(remoteUserGroups)
            }

            // chatUser
            if (localChatUserResource.isNotEmpty()) {
                val mergedChatUser = ((remoteRepository.getChatUser(userId).data
                    ?: emptyList()) + localChatUserResource).distinct()

                localChatUserRepository.insertChatUsers(mergedChatUser)
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

