package com.grupo2.elorchat.ui.groups

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupViewModel(private val groupRepository: CommonGroupRepository) : ViewModel() {

    private val _items = MutableLiveData<Resource<List<Group>>?>()

    private var originalSongs: List<Group> = emptyList()

    private val _delete = MutableLiveData<Resource<Int>?>()

    private val _create = MutableLiveData<Resource<Int>?>()

    val delete : MutableLiveData<Resource<Int>?> get() = _delete

    val create : MutableLiveData<Resource<Int>?> get() = _create

    val items: MutableLiveData<Resource<List<Group>>?> get() = _items

    init {
        updateGroupList()
    }

    private fun updateGroupList() {
        viewModelScope.launch {
            val repoResponse = getGroupsFromRepository()
            _items.value = repoResponse
            originalSongs = repoResponse?.data.orEmpty() // Guarda la lista original
        }
    }
//      //TODO FILTRAR LOS GRUPOS QUE SE MUESTRAN DEPENDIENDO DESDE DONDE SE LES LLAME (Privados ~ Publicos)
//    fun filterSongsTitle(query: String) {
//        val currentSongs = originalSongs.toMutableList()
//
//        // Realiza el filtrado basado en la consulta
//        if (query.isNotBlank()) {
//            currentSongs.retainAll { song ->
//                song.title.contains(query, ignoreCase = true)
//            }
//        }
//        _items.value = Resource.success(currentSongs)
//    }
//
//    fun filterSongsAuthor(query: String) {
//        val currentSongs = originalSongs.toMutableList()
//
//        // Realiza el filtrado basado en la consulta
//        if (query.isNotBlank()) {
//            currentSongs.retainAll { song ->
//                song.author.contains(query, ignoreCase = true)
//            }
//        }
//        _items.value = Resource.success(currentSongs)
//    }

    private suspend fun getGroupsFromRepository(): Resource<List<Group>>? {
        return withContext(Dispatchers.IO) {
            groupRepository.getGroups()
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