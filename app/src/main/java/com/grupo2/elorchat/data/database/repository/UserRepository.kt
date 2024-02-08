package com.grupo2.elorchat.data.database.repository

import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.data.database.dao.UserDao
import com.grupo2.elorchat.data.database.entities.toUser
import com.grupo2.elorchat.data.database.entities.toUserEntity
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao) {

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUser().map { it.toUser() }
    }

    suspend fun getFirstUser(): User? {
        return userDao.getFirstUser()?.toUser()
    }

    suspend fun getUser(userId: Int): User {
        return userDao.getUser(userId).toUser()
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user.toUserEntity())
    }

}