package com.grupo2.elorchat.ui.groups

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupViewModel(private val songRepository: CommonGroupRepository) : ViewModel() {

    private val _items = MutableLiveData<Resource<List<Group>>?>()

    private var originalSongs: List<Group> = emptyList()

    private val _delete = MutableLiveData<Resource<Int>?>()

    private val _create = MutableLiveData<Resource<Int>?>()

    val delete : MutableLiveData<Resource<Int>?> get() = _delete

    val create : MutableLiveData<Resource<Int>?> get() = _create

    val items: MutableLiveData<Resource<List<Group>>?> get() = _items

    init {
        updateSongList()
    }

    private fun updateSongList() {
        viewModelScope.launch {
            val repoResponse = getSongsFromRepository()
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

    private fun createFav(idSong: Int) {
        val id : Int = idSong
        viewModelScope.launch {
            _create.value = createFavouriteSong(id)
        }
    }

    private fun deleteFav(idSong: Int) {
        val id : Int = idSong
        viewModelScope.launch {
            _delete.value = deleteFavouriteSong(id)
        }
    }

    fun addView(idSong: Int){
        val id : Int = idSong
        viewModelScope.launch {
            addViewToSong(id)
            updateSongList()
        }
    }

    private suspend fun addViewToSong(idSong : Int): Resource<Int>{
        return withContext(Dispatchers.IO){
            songRepository.addViewToSong(idSong)
        }
    }

    private suspend fun getSongsFromRepository(): Resource<List<Group>>? {
        return withContext(Dispatchers.IO) {
            songRepository.getSongs()
        }
    }


    private suspend fun createFavouriteSong(idSong : Int): Resource<Int>{
        return withContext(Dispatchers.IO){
            songRepository.createFavouriteForUser(idSong)
        }
    }

    private suspend fun deleteFavouriteSong(idSong: Int): Resource<Int>{
        return withContext(Dispatchers.IO){
            songRepository.deleteFavouriteForUser(idSong)
        }
    }
}

class SongsViewModelFactory(
    private val songRepository : RemoteSongDataSource
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return SongViewModel(songRepository) as T
    }
}