package com.grupo2.elorchat.ui.groups

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupViewModel(private val songRepository: CommonSongRepository) : ViewModel() {

    private val _items = MutableLiveData<Resource<List<Song>>?>()

    private var originalSongs: List<Song> = emptyList()

    private val _delete = MutableLiveData<Resource<Int>?>()

    private val _create = MutableLiveData<Resource<Int>?>()

    private val _favs = MutableLiveData<Resource<List<Song>>?>()

    val favs: MutableLiveData<Resource<List<Song>>?> get() = _favs

    val delete : MutableLiveData<Resource<Int>?> get() = _delete

    val create : MutableLiveData<Resource<Int>?> get() = _create

    val items: MutableLiveData<Resource<List<Song>>?> get() = _items

    init {
        updateSongList()
        getFavs()

    }

    private fun compareFavs() {
        if (_items.value != null && _favs.value != null) {
            for(fav : Song in _favs.value!!.data!!) {
                for (song: Song in _items.value!!.data!!) {
                    if (fav.id == song.id) {
                        song.favorite = true
                    }
                }
            }
        }
    }
    private fun updateSongList() {
        viewModelScope.launch {
            val repoResponse = getSongsFromRepository()
            _items.value = repoResponse
            originalSongs = repoResponse?.data.orEmpty() // Guarda la lista original
            compareFavs()
        }
    }

    fun filterSongsTitle(query: String) {
        val currentSongs = originalSongs.toMutableList()

        // Realiza el filtrado basado en la consulta
        if (query.isNotBlank()) {
            currentSongs.retainAll { song ->
                song.title.contains(query, ignoreCase = true)
            }
        }
        _items.value = Resource.success(currentSongs)
    }

    fun filterSongsAuthor(query: String) {
        val currentSongs = originalSongs.toMutableList()

        // Realiza el filtrado basado en la consulta
        if (query.isNotBlank()) {
            currentSongs.retainAll { song ->
                song.author.contains(query, ignoreCase = true)
            }
        }
        _items.value = Resource.success(currentSongs)
    }

    fun toggleFavourite(song: Song, context: Context) {
        val toastMessage = if (song.favorite) {
            deleteFav(song.id)
            song.favorite = false
            "${song.title}, Removed from favorites"
        } else {
            createFav(song.id)
            song.favorite = true
            "${song.title}, Added to favorites"
        }
        Log.i("mikel","2")
        val currentSongs = originalSongs.toMutableList()
        _items.value = Resource.success(currentSongs)
        // compareFavs()
        // Show a Toast message
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }
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

    private fun getFavs() {
        viewModelScope.launch {
            val repoResponse = getFavourites()
            _favs.value = repoResponse
            compareFavs()
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

    private suspend fun getFavourites(): Resource<List<Song>>? {
        return withContext(Dispatchers.IO) {
            songRepository.getFavouriteSongsFromUser()
        }
    }

    private suspend fun getSongsFromRepository(): Resource<List<Song>>? {
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