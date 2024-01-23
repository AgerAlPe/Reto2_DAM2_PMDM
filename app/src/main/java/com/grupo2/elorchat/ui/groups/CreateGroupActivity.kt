package com.grupo2.elorchat.ui.groups

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.google.android.material.button.MaterialButtonToggleGroup
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.launch

class CreateGroupActivity : AppCompatActivity() {
    private val groupRepository = RemoteGroupDataSource()
    private val viewModel: GroupViewModel by viewModels {
        GroupsViewModelFactory(groupRepository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        var name = findViewById<EditText>(R.id.groupName)

        val groupTypeToggleGroup: MaterialButtonToggleGroup = findViewById(R.id.groupTypeToggleGroup)

        fun isGroupPrivate(): Boolean {
            return when (groupTypeToggleGroup.checkedButtonId) {
                R.id.publicButton -> false
                R.id.privateButton -> true
                else -> false
            }
        }

        findViewById<Button>(R.id.createGroupButton).setOnClickListener {
            val isPrivate = isGroupPrivate()

            // Asegúrate de obtener el texto del EditText correctamente
            val groupName = name.text.toString()

            if (groupName.isBlank() && groupTypeToggleGroup.checkedButtonId == View.NO_ID) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val group = Group(0, groupName, isPrivate)

            viewModel.viewModelScope.launch {
                val result = viewModel.createGroupFromRepository(group)

                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        // Manejar la creación exitosa del grupo
                        result.data?.let { int ->
                            if (int == 1) {
                                Toast.makeText(this@CreateGroupActivity, "Group created successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@CreateGroupActivity, "Error, please restart and try again", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Resource.Status.ERROR -> {
                        // Manejar el error durante la creación del grupo
                        Toast.makeText(this@CreateGroupActivity, "Error while creating group, please try again", Toast.LENGTH_SHORT).show()
                    }
                    Resource.Status.LOADING -> {
                        // Manejar el estado de carga (opcional)
                    }
                }
            }
        }
    }
}
