package com.grupo2.elorchat.ui.users.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grupo2.elorchat.data.LoginUser
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
}
class LoginViewModelFactory(
    private val userRepository: CommonUserRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return LoginViewModel(userRepository) as T
    }
}