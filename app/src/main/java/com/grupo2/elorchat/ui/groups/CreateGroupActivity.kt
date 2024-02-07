package com.grupo2.elorchat.ui.groups

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.ChatUser
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.database.dao.UserDao
import com.grupo2.elorchat.data.database.entities.UserEntity
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateGroupActivity : AppCompatActivity() {

    private lateinit var groupRepository: RemoteGroupDataSource
    private lateinit var userRolesRepository: UserDao
    private lateinit var viewModel: GroupViewModel
    private lateinit var userRoles: UserEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        // Initialize repositories and viewModel
        groupRepository = RemoteGroupDataSource()
        userRolesRepository = AppDatabase.getInstance(application).getUserDao()
        viewModel = ViewModelProvider(this).get(GroupViewModel::class.java)

        initViews()
        fetchUserRoles()
    }

    private fun initViews() {
        val name = findViewById<EditText>(R.id.groupName)
        val groupTypeToggleGroup: MaterialButtonToggleGroup = findViewById(R.id.groupTypeToggleGroup)
        val privateButton: MaterialButton = findViewById(R.id.privateButton)

        privateButton.setOnClickListener { handlePrivateButtonClick() }

        findViewById<Button>(R.id.createGroupButton).setOnClickListener { handleCreateGroupButtonClick() }
    }

    private fun fetchUserRoles() {
        lifecycleScope.launch {
            try {
                // Fetch user roles from Room
                userRoles = userRolesRepository.getAllUser().first()

                // Disable privateButton if the user is not a teacher
                if (!userRoles.isTeacher()) {
                    findViewById<MaterialButton>(R.id.privateButton).isEnabled = false
                }
            } catch (e: Exception) {
                // Handle exception, e.g., if the coroutine is canceled
            }
        }
    }

    private fun handlePrivateButtonClick() {
        val groupTypeToggleGroup: MaterialButtonToggleGroup = findViewById(R.id.groupTypeToggleGroup)
        val privateButton: MaterialButton = findViewById(R.id.privateButton)

        if (!userRoles.isTeacher()) {
            showToast("Only teachers can create private groups")
            groupTypeToggleGroup.check(R.id.publicButton)  // Uncheck privateButton
        } else {
            groupTypeToggleGroup.check(R.id.privateButton)
        }
    }

    private fun handleCreateGroupButtonClick() {
        val groupTypeToggleGroup: MaterialButtonToggleGroup = findViewById(R.id.groupTypeToggleGroup)
        val name = findViewById<EditText>(R.id.groupName)

        val isPrivate = groupTypeToggleGroup.checkedButtonId == R.id.privateButton
        val groupName = name.text.toString()

        if (groupName.isBlank()) {
            showToast("Enter a group name")
            return
        }

        if (isPrivate && !userRoles.isTeacher()) {
            showToast("Only teachers can create private groups")
            return
        }

        val group = Group(0, groupName, isPrivate)

        viewModel.viewModelScope.launch {
            val result = viewModel.createGroupFromRepository(group)

            when (result.status) {
                Resource.Status.SUCCESS -> {

                    val groupId = result.data // Assuming the result.data is the group ID
                    viewModel.updateGroupList()
                    // Assuming the group creation was successful, you can now join the chat
                    if (groupId != null) {
                        joinChat(groupId)

                        handleSuccess(groupId)
                    }

                }
                Resource.Status.ERROR -> handleError()
                Resource.Status.LOADING -> handleLoading()
            }
        }
    }

    private fun handleSuccess(groupId: Int?) {
        groupId?.let {
            showToast("Group created successfully")
            finish()
        }
    }

    private fun joinChat(groupId: Int) {
        Log.i("JOINCHATuserId", userRoles.id.toString())
        Log.i("JOINCHATGroupId", groupId.toString())

        val chatUser = ChatUser(userRoles.id!!, groupId, true)
        viewModel.joinChat(chatUser)
    }

    private fun handleError() {
        showToast("Error while creating a group, please try again")
    }

    private fun handleLoading() {
        // Handle loading state if needed
    }

    private fun showToast(message: String) {
        Toast.makeText(this@CreateGroupActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun UserEntity.isTeacher(): Boolean {
        return roles.any { it.name.lowercase() == "teacher" }
    }
}