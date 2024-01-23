package com.grupo2.elorchat.ui.groups

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grupo2.elorchat.ElorChat.Companion.context
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupViewModel(private val groupRepository: CommonGroupRepository) : ViewModel() {

    private val dataStoreManager by lazy { DataStoreManager.getInstance(context) }

    private val _items = MutableLiveData<Resource<List<Group>>?>()

    private var originalGroups: List<Group> = emptyList()

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
            val repoResponse = getAllGroupsFromRepository()
            originalGroups = repoResponse.data.orEmpty() // Guarda la lista original
            filterPrivateGroups() // Inicializa los grupos privados
            filterPublicGroups() // Inicializa los grupos públicos
        }
    }

    // Filtrar grupos privados
    private fun filterPrivateGroups() {
        viewModelScope.launch {
            try {
                // Retrieve the saved groups from DataStoreManager
                val savedGroups = dataStoreManager.getSavedGroups().first()

                // Filter the original groups based on the saved groups
                val privateGroups = originalGroups.filter { group ->
                    group.isPrivate && savedGroups.any { savedGroup -> savedGroup.id == group.id }
                }

                _privateGroups.value = Resource.success(privateGroups)
            } catch (e: Exception) {
                // Handle the exception or log the error
                _privateGroups.value = Resource.error(e.message ?: "Error filtering private groups", null)
            }
        }
    }

    // Filtrar grupos públicos
    private fun filterPublicGroups() {
        val publicGroups = originalGroups.filterNot { group -> group.isPrivate }
        _publicGroups.value = Resource.success(publicGroups)
    }

    private suspend fun getAllGroupsFromRepository(): Resource<List<Group>> {
        return withContext(Dispatchers.IO) {
            groupRepository.getGroups()
        }
    }

    suspend fun createGroupFromRepository(group: Group): Resource<Int> {
        return withContext(Dispatchers.IO) {
            groupRepository.createGroup(group)
        }
    }

}

class GroupsViewModelFactory(
    private val groupRepository : RemoteGroupDataSource
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return GroupViewModel(groupRepository) as T
    }
}