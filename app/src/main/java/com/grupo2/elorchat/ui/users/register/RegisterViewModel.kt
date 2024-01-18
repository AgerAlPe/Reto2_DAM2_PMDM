package com.grupo2.elorchat.ui.users.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.data.repository.AuthenticationResponse
import com.grupo2.elorchat.data.repository.CommonUserRepository
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel(
    private val userRepository: CommonUserRepository
) : ViewModel() {

    private val _registeringUser = MutableLiveData<Resource<User>>()
    val registeringUser : LiveData<Resource<User>> get() = _registeringUser

    fun registerOfUser(userEmail: String) {
        viewModelScope.launch {
            _registeringUser.value =  registerUser(userEmail)
            Log.i("valueOfUser", _registeringUser.toString())
        }
    }
    private suspend fun registerUser(userEmail: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            userRepository.getUserByEmail(userEmail)
        }
    }
}
class RegisterViewModelFactory(
    private val userRepository: CommonUserRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return RegisterViewModel(userRepository) as T
    }
}