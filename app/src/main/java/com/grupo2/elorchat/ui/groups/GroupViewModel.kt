package com.grupo2.elorchat.ui.groups


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo2.elorchat.data.ChatUser
import com.grupo2.elorchat.ElorChat.Companion.context
import com.grupo2.elorchat.data.ChangePasswordRequest
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.database.entities.GroupEntity
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val groupRepository: CommonGroupRepository
) : ViewModel() {

    private val dataStoreManager by lazy { DataStoreManager.getInstance(context) }

    private val _items = MutableLiveData<Resource<List<Group>>?>()

    private var originalGroups: List<Group> = emptyList()

    private var allGroups: List<Group> = emptyList()

    private val _privateGroups = MutableLiveData<Resource<List<Group>>?>()

    private val _publicGroups = MutableLiveData<Resource<List<Group>>?>()

    private val _delete = MutableLiveData<Resource<Int>?>()

    private val _create = MutableLiveData<Resource<Int>?>()

    private val _joinChat = MutableLiveData<Resource<ChatUser>>()

    private val _leaveChat = MutableLiveData<Resource<Void>>()

    private val _groups = MutableLiveData<List<Group>>() // LiveData for the list of groups
    val groups: LiveData<List<Group>> get() = _groups // Expose LiveData to observe in the fragment

    private val _changePassword = MutableLiveData<Resource<Void>>()

    private val _firstUserEmail = MutableLiveData<String>()

    private val _adminInGroups = MutableLiveData<List<Group>>()

    val adminInGroups :MutableLiveData<List<Group>> get() = _adminInGroups

    val firstUserEmail: LiveData<String> get() = _firstUserEmail
    val changePassword : MutableLiveData<Resource<Void>> get() = _changePassword

    val leaveChat: MutableLiveData<Resource<Void>> get() = _leaveChat
    val joinChat: MutableLiveData<Resource<ChatUser>> get() = _joinChat
    val delete: MutableLiveData<Resource<Int>?> get() = _delete
    val create: MutableLiveData<Resource<Int>?> get() = _create
    val items: MutableLiveData<Resource<List<Group>>?> get() = _items
    val privateGroups: MutableLiveData<Resource<List<Group>>?> get() = _privateGroups
    val publicGroups: MutableLiveData<Resource<List<Group>>?> get() = _publicGroups

    init {
        updateGroupList()
        getAdminGroups()
    }

    private val _joinChatListener = MutableLiveData<Resource<ChatUser>>()
    val joinChatListener: LiveData<Resource<ChatUser>> get() = _joinChatListener

    fun joinChatListener(chatUser: ChatUser) {
        viewModelScope.launch {
            try {
                _joinChatListener.value = joinChatFromRepo(chatUser)
            } catch (e: Exception) {
                // Handle exceptions if any
                Log.e(TAG, "Exception while joining the chat: ${e.message}")
            }
        }
    }

    fun updateGroupList() {
        viewModelScope.launch {
            try {
                val userId = appDatabase.getUserDao().getAllUser().first().id
                val repoResponse = getAllUserGroupsFromRepository(userId)
                val allGroupsFromRepository = getAllGroupsFromRepository()
                originalGroups = repoResponse.data.orEmpty()

                // Create a temporary list to update isUserOnGroup property
                val updatedGroups = allGroupsFromRepository.data.orEmpty().toMutableList()

                // Update the isUserOnGroup property
                updatedGroups.forEach { group ->
                    group.isUserOnGroup = originalGroups.any { userGroup -> userGroup.id == group.id }
                }

                // Assign the updated list to allGroups
                allGroups = updatedGroups

                filterPrivateGroups()
                filterPublicGroups()
                updateIsUserOnGroupStatus()

                // Update LiveData with the latest list of groups
                _groups.postValue(allGroups)
            } catch (e: Exception) {
                Log.e(TAG, "Exception while updating group list: ${e.message}")
            }
        }
    }

    private fun updateIsUserOnGroupStatus() {
        viewModelScope.launch {
            try {
                val userId = appDatabase.getUserDao().getAllUser().first().id
                val allGroupsFromRepository = getAllGroupsFromRepository()
                val userGroupsFromRepository = getAllUserGroupsFromRepository(userId)

                allGroupsFromRepository.data?.let { allGroups ->
                    userGroupsFromRepository.data?.let { userGroups ->
                        allGroups.forEach { group ->
                            group.isUserOnGroup = userGroups.any { userGroup -> userGroup.id == group.id }
                        }
                    }
                }

                allGroupsFromRepository.data?.let { allGroups ->
                    userGroupsFromRepository.data?.let { userGroups ->
                        Log.d("GroupViewModel", "All Groups: $allGroups")
                        Log.d("GroupViewModel", "User Groups: $userGroups")
                    }
                }

                allGroups = allGroupsFromRepository.data.orEmpty()

                _publicGroups.value = Resource.success(allGroups)
            } catch (e: Exception) {
                _publicGroups.value = Resource.error("Error updating isUserOnGroup status", null)
                Log.e("GroupViewModel", "Exception in updateIsUserOnGroupStatus: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "GroupViewModel"
    }

    private fun filterPrivateGroups() {
        viewModelScope.launch {
            try {
                val savedGroupsRoom = appDatabase.getGroupDao().getAllPrivateGroups()
                val newPrivateGroups =
                    originalGroups.filter { it.isPrivate && !savedGroupsRoom.any { savedGroup -> savedGroup.id == it.id } }
                val privateGroups =
                    (newPrivateGroups + savedGroupsRoom.map { Group(it.id, it.name, it.isPrivate) }).distinctBy { it.id }
                val privateGroupsEntities = privateGroups.map {
                    GroupEntity(it.id, it.name, it.isPrivate)
                }

                appDatabase.getGroupDao().insertAll(privateGroupsEntities)

                _privateGroups.value = Resource.success(privateGroups)
            } catch (e: Exception) {
                _privateGroups.value =
                    Resource.error(e.message ?: "Error filtering private groups", null)
            }
        }
    }

    private fun filterPublicGroups() {
        viewModelScope.launch {
            try {
                val savedPublicGroupsRoom = appDatabase.getGroupDao().getAllPublicGroups()
                val newPublicGroups = allGroups.filterNot { it.isPrivate }
                val publicGroups =
                    (newPublicGroups + savedPublicGroupsRoom.map { Group(it.id, it.name, it.isPrivate) }).distinctBy { it.id }
                val publicGroupsEntities = publicGroups.map {
                    GroupEntity(it.id, it.name, it.isPrivate)
                }

                appDatabase.getGroupDao().insertAll(publicGroupsEntities)

                _publicGroups.value = Resource.success(publicGroups)
            } catch (e: Exception) {
                _publicGroups.value =
                    Resource.error(e.message ?: "Error filtering public groups", null)
            }
        }
    }

    private suspend fun getAllGroupsFromRepository(): Resource<List<Group>> {
        return withContext(Dispatchers.IO) {
            groupRepository.getGroups()
        }
    }

    private suspend fun getAllUserGroupsFromRepository(userId: Int): Resource<List<Group>> {
        return withContext(Dispatchers.IO) {
            groupRepository.getUserGroups(userId)
        }
    }

    suspend fun createGroupFromRepository(group: Group): Resource<Int> {
        return withContext(Dispatchers.IO) {
            groupRepository.createGroup(group)
        }
    }

    fun joinChat(chatUser: ChatUser) {
        viewModelScope.launch {
            try {
                _joinChat.value = joinChatFromRepo(chatUser)
            } catch (e: Exception) {
                Log.e(TAG, "Exception while joining the chat: ${e.message}")
            }
        }
    }

    private suspend fun joinChatFromRepo(chatUser: ChatUser): Resource<ChatUser> {
        return withContext(Dispatchers.IO) {
            groupRepository.joinChat(chatUser)
        }
    }

    private val _leaveChatResult = MutableLiveData<Resource<Unit>>()
    val leaveChatResult: LiveData<Resource<Unit>> get() = _leaveChatResult

    fun leaveChat(userId: Int, chatId: Int) {
        viewModelScope.launch {
            try {
                _leaveChatResult.value = Resource.loading()
                groupRepository.leaveChat(userId, chatId)
                _leaveChatResult.value = Resource.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Exception while leaving the chat: ${e.message}")
                _leaveChatResult.value = Resource.error("Error leaving the chat", null)
            }
        }
    }

    private suspend fun leaveChatFromRepo(userId: Int, chatId: Int): Resource<Void> {
        return withContext(Dispatchers.IO) {
            groupRepository.leaveChat(userId, chatId)
        }
    }


    fun changeUserPassword(changePasswordRequest: ChangePasswordRequest) {
        viewModelScope.launch {
            _changePassword.value =  userPassword(changePasswordRequest)
        }
    }
    private suspend fun userPassword(changePasswordRequest: ChangePasswordRequest): Resource<Void> {
        return withContext(Dispatchers.IO) {
            groupRepository.changePassword(changePasswordRequest)
        }
    }

    suspend fun loadFirstUserEmail() {
        val userEmail = try {
            val userDao = appDatabase.getUserDao()
            val firstUser = userDao.getFirstUser()

            firstUser?.email ?: "default@example.com" // Default value if no user is found
        } catch (e: Exception) {
            // Handle the exception if there is an error while accessing the database
            Log.e("GroupViewModel", "Error getting user email from Room", e)
            "default@example.com" // Default value on error
        }

        _firstUserEmail.value = userEmail
    }

    private fun getAdminGroups() {
        viewModelScope.launch {
            try {
                val userId = appDatabase.getUserDao().getAllUser().first().id
                Log.i("userID", userId.toString())
                val adminGroupsResponse = getAllGroupsWhereUserIsAdmin(userId)
                _adminInGroups.postValue(adminGroupsResponse.data.orEmpty())
                Log.i(TAG, "Admin Groups: $adminGroupsResponse")
            } catch (e: Exception) {
                Log.e(TAG, "Exception while getting admin groups: ${e.message}")
            }
        }
    }
    private suspend fun getAllGroupsWhereUserIsAdmin(userId: Int): Resource<List<Group>> {
        return withContext(Dispatchers.IO) {
            groupRepository.getChatsWhereUserIsAdmin(userId)
        }
    }
}

sealed class GroupEvent {
    data class JoinGroupSuccess(val groupId: Int) : GroupEvent()
}