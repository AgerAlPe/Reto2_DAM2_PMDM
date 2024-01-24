package com.grupo2.elorchat.ui.groups

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grupo2.elorchat.ElorChat.Companion.context
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.database.entities.GroupEntity
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
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
    private val groupRepository: CommonGroupRepository) : ViewModel() {

    private val dataStoreManager by lazy { DataStoreManager.getInstance(context) }

    private val _items = MutableLiveData<Resource<List<Group>>?>()

    private var originalGroups: List<Group> = emptyList()

    private var allGroups: List<Group> = emptyList()

    private val _privateGroups = MutableLiveData<Resource<List<Group>>?>()

    private val _publicGroups = MutableLiveData<Resource<List<Group>>?>()

    private val _delete = MutableLiveData<Resource<Int>?>()

    private val _create = MutableLiveData<Resource<Int>?>()

    val delete : MutableLiveData<Resource<Int>?> get() = _delete

    val create : MutableLiveData<Resource<Int>?> get() = _create

    val items: MutableLiveData<Resource<List<Group>>?> get() = _items

    val privateGroups: MutableLiveData<Resource<List<Group>>?> get() = _privateGroups

    val publicGroups: MutableLiveData<Resource<List<Group>>?> get() = _publicGroups

    init {
        updateGroupList()
    }

    private fun updateGroupList() {
        viewModelScope.launch {
            val userId = dataStoreManager.getSavedUserId().first()
            val repoResponse = getAllUserGroupsFromRepository(userId)
            val allGroupsFromRepository= getAllGroupsFromRepository()
            allGroups = allGroupsFromRepository.data.orEmpty()
            originalGroups = repoResponse.data.orEmpty() // Guarda la lista original
            filterPrivateGroups() // Inicializa los grupos privados
            filterPublicGroups() // Inicializa los grupos públicos
        }
    }

    // Filtrar grupos privados
    private fun filterPrivateGroups() {
        viewModelScope.launch {
            try {
                // Retrieve the saved groups from Room database
                val savedGroupsRoom = appDatabase.getGroupDao().getAllPrivateGroups()

                // Filter originalGroups to include only private groups that are not already in Room
                val newPrivateGroups = originalGroups.filter { it.isPrivate && !savedGroupsRoom.any { savedGroup -> savedGroup.id == it.id } }

                // Combine newPrivateGroups with savedGroupsRoom, removing duplicates based on id
                val privateGroups = (newPrivateGroups + savedGroupsRoom.map { Group(it.id, it.name, it.isPrivate) }).distinctBy { it.id }

                // Convert the combinedPrivateGroups to List<GroupEntity>
                val privateGroupsEntities = privateGroups.map {
                    GroupEntity(it.id, it.name, it.isPrivate)
                }

                // Save the combinedPrivateGroups to Room database
                appDatabase.getGroupDao().insertAll(privateGroupsEntities)

                _privateGroups.value = Resource.success(privateGroups)
            } catch (e: Exception) {
                // Handle the exception or log the error
                _privateGroups.value = Resource.error(e.message ?: "Error filtering private groups", null)
            }
        }
    }

    // Filtrar grupos públicos
    private fun filterPublicGroups() {
        viewModelScope.launch {
            try {
                // Retrieve the saved public groups from Room database
                val savedPublicGroupsRoom = appDatabase.getGroupDao().getAllPublicGroups()

                // Filter groups to include only public groups that are not already in Room
                val newPublicGroups = allGroups.filterNot { it.isPrivate }

                // Combine newPublicGroups with savedPublicGroupsRoom, removing duplicates based on id
                val publicGroups = (newPublicGroups + savedPublicGroupsRoom.map { Group(it.id, it.name, it.isPrivate) }).distinctBy { it.id }

                // Convert the combined publicGroups to List<GroupEntity>
                val publicGroupsEntities = publicGroups.map {
                    GroupEntity(it.id, it.name, it.isPrivate)
                }

                // Save the combined publicGroups to Room database
                appDatabase.getGroupDao().insertAll(publicGroupsEntities)

                _publicGroups.value = Resource.success(publicGroups)
            } catch (e: Exception) {
                // Handle the exception or log the error
                _publicGroups.value = Resource.error(e.message ?: "Error filtering public groups", null)
            }
        }
    }

    private suspend fun getAllGroupsFromRepository(): Resource<List<Group>> {
        return withContext(Dispatchers.IO) {
            groupRepository.getGroups()
        }
    }

    private suspend fun getAllUserGroupsFromRepository(userId : Int): Resource<List<Group>> {
        return withContext(Dispatchers.IO) {
            groupRepository.getUserGroups(userId)
        }
    }

    suspend fun createGroupFromRepository(group: Group): Resource<Int> {
        return withContext(Dispatchers.IO) {
            groupRepository.createGroup(group)
        }
    }

}

class GroupsViewModelFactory(
    private val appDatabase: AppDatabase,
    private val groupRepository : RemoteGroupDataSource
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return GroupViewModel(appDatabase, groupRepository) as T
    }
}