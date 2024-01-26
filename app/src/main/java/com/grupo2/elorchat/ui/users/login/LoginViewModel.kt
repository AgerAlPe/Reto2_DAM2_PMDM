package com.grupo2.elorchat.ui.users.login

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

class LoginViewModel(
    private val userRepository: CommonUserRepository
) : ViewModel() {

    private val _loggedUser = MutableLiveData<Resource<AuthenticationResponse>>()
    val loggedUser : LiveData<Resource<AuthenticationResponse>> get() = _loggedUser

    private val _userData = MutableLiveData<Resource<User>>()
    val userData : LiveData<Resource<User>> get() = _userData

    fun loginOfUser(user: LoginUser) {
        viewModelScope.launch {
            _loggedUser.value =  loggedUser(user)
        }
    }
    private suspend fun loggedUser(user: LoginUser): Resource<AuthenticationResponse> {
        return withContext(Dispatchers.IO) {
            userRepository.loginUser(user)
        }
    }

    //GET THE INFO FOR LOGGED USER
    fun getUserData(userEmail: String) {
        viewModelScope.launch {
            _userData.value =  userData(userEmail)
        }
    }
    private suspend fun userData(userEmail: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            userRepository.getUserByEmail(userEmail)
        }
    }
}
class LoginViewModelFactory(
    private val userRepository: CommonUserRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return LoginViewModel(userRepository) as T
    }
}