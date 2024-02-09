package com.grupo2.elorchat.ui.groups


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo2.elorchat.data.ChatUser
import com.grupo2.elorchat.ElorChat.Companion.context
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.ChangePasswordRequest
import com.grupo2.elorchat.data.ChatUserEmailRequest
import com.grupo2.elorchat.data.ChatUserMovementResponse
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.database.entities.GroupEntity
import com.grupo2.elorchat.data.database.repository.ChatUserRepository
import com.grupo2.elorchat.data.database.repository.GroupRepository
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val groupRepository: CommonGroupRepository,
    private val roomGroupRepository: GroupRepository,
    private val chatUserRepository: ChatUserRepository
) : ViewModel() {

    private val _items = MutableLiveData<Resource<List<Group>>?>()

    private var allGroups: List<Group> = emptyList()

    private var _privateGroups = MutableLiveData<Resource<List<Group>>?>()

    private var _publicGroups = MutableLiveData<Resource<List<Group>>?>()

    private val _delete = MutableLiveData<Resource<Int>?>()

    private val _create = MutableLiveData<Resource<Int>?>()

    private val _joinChat = MutableLiveData<Resource<ChatUser>>()

    private val _leaveChat = MutableLiveData<Resource<Void>>()

    private val _groups = MutableLiveData<List<Group>>() // LiveData for the list of groups
    val groups: LiveData<List<Group>> get() = _groups // Expose LiveData to observe in the fragment

    private val _changePassword = MutableLiveData<Resource<Void>>()

    private val _firstUserEmail = MutableLiveData<String>()

    private val _adminInGroups = MutableLiveData<List<Group>>()

    private val _addUser = MutableLiveData<ChatUserMovementResponse>()

    val addUser : MutableLiveData<ChatUserMovementResponse> get() = _addUser

    private val _kickUser = MutableLiveData<ChatUserMovementResponse>()

    val kickUser : MutableLiveData<ChatUserMovementResponse> get() = _kickUser

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

    val publicRoomGrops : MutableLiveData<Resource<List<Group>>?> get() = _publicRoomGroups

    private val _publicRoomGroups = MutableLiveData<Resource<List<Group>>?>()

    private var originalPublicGroups: List<Group> = emptyList()
    private var originalPrivateGroups: List<Group> = emptyList()

    private val _leaveChatResult = MutableLiveData<Resource<Unit>>()
    val leaveChatResult: LiveData<Resource<Unit>> get() = _leaveChatResult

    companion object {
        private const val TAG = "GroupViewModel"
    }


    suspend fun RoomGroups() {
        val groups: List<Group> = roomGroupRepository.getAllPublicGroups()
        val resource: Resource<List<Group>> = Resource.success(groups)
        _publicRoomGroups.value = resource
    }

    // Example of calling RoomGroups from a coroutine scope
    fun someCoroutineFunction() {
        viewModelScope.launch {
            RoomGroups()
        }
    }


    init {
        updateGroupList()
        getAdminGroups()
    }

    private val _joinChatListener = MutableLiveData<Resource<ChatUser>>()
    val joinChatListener: LiveData<Resource<ChatUser>> get() = _joinChatListener


    fun updateGroupsLists() {
        // Update LiveData objects for public and private groups
        _publicGroups.value = Resource.success(allGroups.filter { !it.isPrivate })
        _privateGroups.value = Resource.success(allGroups.filter { it.isPrivate })
    }

    fun updateGroupList() {
        viewModelScope.launch {
            try {
                val userId = appDatabase.getUserDao().getAllUser().firstOrNull()?.id
                val localGroups = userId?.let { chatUserRepository.getChatsForUser(it) }?.data.orEmpty()
                val allGroupsFromRepository = getAllGroupsFromRepository()
                val allUserGroups = userId?.let { getAllUserGroupsFromRepository(it) }

                val updatedGroups = mutableListOf<Group>()
                val allGroupIds = mutableSetOf<Int>()

                allUserGroups?.data?.forEach { group ->
                    allGroupIds.add(group.id)
                    group.isUserOnGroup = true
                    updatedGroups.add(group)
                }

                allGroupsFromRepository.data?.forEach { group ->
                    if (allGroupIds.add(group.id) && !group.isPrivate) {
                        updatedGroups.add(group)
                    }
                }

                allGroups = updatedGroups
                originalPublicGroups = allGroups.filterNot { it.isPrivate }
                originalPrivateGroups = allGroups.filter { it.isPrivate && it.isUserOnGroup }

                filterPrivateGroups()
                filterPublicGroups()
                getAdminGroups()

                _groups.postValue(allGroups)
            } catch (e: Exception) {
                Log.e(TAG, "Exception while updating group list: ${e.message}")
            }
        }
    }


    private fun filterPrivateGroups() {
        viewModelScope.launch {
            try {
                // Filter private groups from originalPrivateGroups
                val newPrivateGroups = originalPrivateGroups.filter { it.isUserOnGroup }

                // Insert filtered private groups into the database
                val privateGroupsEntities = newPrivateGroups.map {
                    GroupEntity(it.id, it.name, it.isPrivate)
                }
                appDatabase.getGroupDao().insertAll(privateGroupsEntities)

                // Update private groups LiveData
                _privateGroups.value = Resource.success(newPrivateGroups)
            } catch (e: Exception) {
                _privateGroups.value = Resource.error(
                    e.message ?: "Error filtering private groups",
                    null
                )
            }
        }
    }

    private fun filterPublicGroups() {
        viewModelScope.launch {
            try {
                // Filter public groups from originalPublicGroups
                val newPublicGroups = originalPublicGroups.filter { !it.isPrivate }

                // Insert filtered public groups into the database
                val publicGroupsEntities = newPublicGroups.map {
                    GroupEntity(it.id, it.name, it.isPrivate)
                }
                appDatabase.getGroupDao().insertAll(publicGroupsEntities)

                // Update public groups LiveData
                _publicGroups.value = Resource.success(newPublicGroups)
            } catch (e: Exception) {
                _publicGroups.value = Resource.error(
                    e.message ?: "Error filtering public groups",
                    null
                )
            }
        }
    }

    fun filterPublicGroupsByName(query: String?) {
        val filteredGroups = query?.takeIf { it.isNotBlank() }
            ?.let { searchTerm ->
                originalPublicGroups.filter { group ->
                    group.name.contains(searchTerm, ignoreCase = true)
                }
            } ?: originalPublicGroups

        _publicGroups.value = Resource.success(filteredGroups)
    }

    fun filterPrivateGroupsByName(query: String?) {
        val filteredGroups = query?.takeIf { it.isNotBlank() }
            ?.let { searchTerm ->
                originalPrivateGroups.filter { group ->
                    group.name.contains(searchTerm, ignoreCase = true)
                }
            } ?: originalPrivateGroups

        _privateGroups.value = Resource.success(filteredGroups)
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

    fun createGroup(group: Group) {
        viewModelScope.launch {
            try {
                val result = createGroupFromRepository(group)
                if (result.status == Resource.Status.SUCCESS) {
                    // If group creation was successful, update the list of groups
                    updateGroupList()
                    updateGroupsLists()
                    if (!group.isPrivate){
                        addGroupToPublicList(group)
                    }else {
                        addGroupToPrivateList(group)
                    }

                }
                _create.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Exception while creating group: ${e.message}")
                _create.value = Resource.error("Error creating group", null)
            }
        }
    }

    suspend fun createGroupFromRepository(group: Group): Resource<Int> {
        return withContext(Dispatchers.IO) {
            groupRepository.createGroup(group)
        }
    }

    private fun addGroupToPublicList(group: Group) {
        val currentGroups = _publicGroups.value?.data?.toMutableList() ?: mutableListOf()
        currentGroups.add(group)
        _publicGroups.value = Resource.success(currentGroups)
    }

    private fun addGroupToPrivateList(group: Group) {
        val currentGroups = _privateGroups.value?.data?.toMutableList() ?: mutableListOf()
        currentGroups.add(group)
        _privateGroups.value = Resource.success(currentGroups)
    }

    fun joinChat(chatUser: ChatUser) {
        viewModelScope.launch {
            try {
                chatUserRepository.insertChatUser(chatUser)
                joinChatFromRepo(chatUser)
                _joinChat.value = Resource.success(chatUser)
                updateGroupList()
            } catch (e: Exception) {
                Log.e(TAG, "Exception while joining the chat: ${e.message}")
                _joinChat.value = Resource.error("Error joining the chat", null)
            }
        }
    }

    private suspend fun joinChatFromRepo(chatUser:  ChatUser): Resource<ChatUser> {
        return withContext(Dispatchers.IO) {
            groupRepository.joinChat(chatUser)
        }
    }

    fun leaveChat(userId: Int, chatId: Int) {
        viewModelScope.launch {
            try {
                _leaveChatResult.value = Resource.loading()
                chatUserRepository.deleteChatUsersForChatAndUser(chatId, userId)
                leaveChatFromRepo(userId, chatId)
                _leaveChatResult.value = Resource.success(Unit)
                updateGroupList()
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
                val adminGroupsResponse = userId?.let { getAllGroupsWhereUserIsAdmin(it) }
                if (adminGroupsResponse != null) {
                    _adminInGroups.postValue(adminGroupsResponse.data.orEmpty())
                }
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

    fun makeAnUserJoin(chatUserEmailRequest: ChatUserEmailRequest) {
        viewModelScope.launch {
            val result = makeAnUserJoinAChat(chatUserEmailRequest)
            _addUser.value = result.data ?: ChatUserMovementResponse("Error: ${result.status}${result.message}")
        }
    }

    private suspend fun makeAnUserJoinAChat(chatUserEmailRequest: ChatUserEmailRequest): Resource<ChatUserMovementResponse> {
        return withContext(Dispatchers.IO) {
            groupRepository.makeAnUserJoinAChat(chatUserEmailRequest)
        }
    }

    fun makeAnUserLeave(chatUserEmailRequest: ChatUserEmailRequest) {
        viewModelScope.launch {
            val result = makeAnUserLeaveAChat(chatUserEmailRequest)
            Log.i("resultMessage", result.message.toString())
            if (result.message.toString().contains("400")) {
                _kickUser.value = ChatUserMovementResponse("ERROR 400")
            } else {
                _kickUser.value = result.data ?: ChatUserMovementResponse("End of input at line 1 column 1 path \$")
            }
        }
    }

    private suspend fun makeAnUserLeaveAChat(chatUserEmailRequest: ChatUserEmailRequest): Resource<ChatUserMovementResponse> {
        return withContext(Dispatchers.IO) {
            groupRepository.makeAnUserLeaveAChat(chatUserEmailRequest)
        }
    }
    private val _deleteGroup = MutableLiveData<Resource<Void>>()
    val deleteGroup: LiveData<Resource<Void>> get() = _deleteGroup
    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            try {
                val result = deleteGroupFromRepo(groupId)
                if (result.status == Resource.Status.SUCCESS) {
//                    deleteGroupFromDao(groupId)
                    // If group deletion was successful, update the list of groups
                    updateGroupList()
                    updateGroupsLists()
                }
                _deleteGroup.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Exception while deleting group: ${e.message}")
                _deleteGroup.value = Resource.error("Error deleting group", null)
            }
        }
    }

    private suspend fun deleteGroupFromRepo(groupId: Int): Resource<Void> {
        return withContext(Dispatchers.IO) {
            groupRepository.deleteGroup(groupId)
        }
    }

//    suspend fun deleteGroupFromDao(groupId: Int): Resource<Unit> {
//        return try {
//            // Assuming your DAO is named groupDao
//            roomGroupRepository.deleteGroup(groupId)
//            Resource.success(Unit)
//        } catch (e: Exception) {
//            Resource.error("Error deleting group", null)
//        }
//    }

}

sealed class GroupEvent {
    data class JoinGroupSuccess(val groupId: Int) : GroupEvent()
}