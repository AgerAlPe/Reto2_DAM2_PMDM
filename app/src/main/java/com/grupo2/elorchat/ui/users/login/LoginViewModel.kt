package com.grupo2.elorchat.ui.users.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grupo2.elorchat.data.ChangePasswordRequest
import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.data.database.repository.ChatUserRepository
import com.grupo2.elorchat.data.database.repository.GroupRepository
import com.grupo2.elorchat.data.database.repository.UserRepository
import com.grupo2.elorchat.data.repository.AuthenticationResponse
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.repository.CommonUserRepository
import com.grupo2.elorchat.utils.Resource
import com.grupo2.elorchat.utils.UserInformationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val commonUserRepository: CommonUserRepository,
    private val userRepository: UserRepository,
    private val localGroupRepository: GroupRepository,
    private val localChatUserRepository: ChatUserRepository,
    private val remoteRepository: CommonGroupRepository
) : ViewModel() {

    private val _loggedUser = MutableLiveData<Resource<AuthenticationResponse>>()
    val loggedUser: LiveData<Resource<AuthenticationResponse>> get() = _loggedUser

    private val _userData = MutableLiveData<Resource<User>>()
    val userData: LiveData<Resource<User>> get() = _userData

    fun syncLogUserInfo(userId: Int) {
        viewModelScope.launch {
            val userInformationManager = UserInformationManager(
                localGroupRepository,
                localChatUserRepository,
                remoteRepository
            )
            userInformationManager.syncUserInfo(userId)
        }
    }



    fun loginOfUser(user: LoginUser) {
        viewModelScope.launch {
            _loggedUser.value = loggedUser(user)
        }
    }

    private suspend fun loggedUser(user: LoginUser): Resource<AuthenticationResponse> {
        return withContext(Dispatchers.IO) {
            commonUserRepository.loginUser(user)
        }
    }

    // GET THE INFO FOR LOGGED USER
    fun getUserData(userEmail: String) {
        viewModelScope.launch {
            _userData.value = userData(userEmail)
        }
    }

    private suspend fun userData(userEmail: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            commonUserRepository.getUserByEmail(userEmail)
        }
    }
}

class LoginViewModelFactory(
    private val commonUserRepository: CommonUserRepository,
    private val userRepository: UserRepository,
    private val localGroupRepository: GroupRepository,
    private val localChatUserRepository: ChatUserRepository,
    private val remoteRepository: CommonGroupRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return LoginViewModel(commonUserRepository, userRepository, localGroupRepository, localChatUserRepository, remoteRepository) as T
    }
}