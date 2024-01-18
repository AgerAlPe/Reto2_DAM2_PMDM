package com.grupo2.elorchat.ui.users.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.grupo2.elorchat.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.grupo2.elorchat.data.repository.remote.RemoteUserDataSource
import com.grupo2.elorchat.ui.groups.GroupActivity
import com.grupo2.elorchat.ui.users.login.LoginActivity
import com.grupo2.elorchat.utils.Resource

class ScrollingFragmentRegister : Fragment() {

    private val userRepository = RemoteUserDataSource()
    private val viewModel: RegisterViewModel by viewModels { RegisterViewModelFactory(
        userRepository
    ) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_scrolling_register, container, false)

        val userEmail = arguments?.getString("userEmail")

        if (userEmail != null) {
            Log.i("userEmail", userEmail)
        }else {
            Log.i("userEmail", "userEmail is null")
        }

        //viewModel.registerOfUser(userEmail)

        viewModel.registeringUser.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    // Handle successful login
                    result.data?.let { data ->

                        view.findViewById<EditText>(R.id.registerEmail).setText(data.email)
                        view.findViewById<EditText>(R.id.registerName).setText(data.name)
                        view.findViewById<EditText>(R.id.registerSurname).setText(data.surnames)
                        view.findViewById<EditText>(R.id.registerPhone).setText(data.phoneNumber)
                        view.findViewById<EditText>(R.id.registerDni).setText(data.dni)
                        view.findViewById<EditText>(R.id.registerDirection).setText(data.direction)
                        if (data.fctDual) {
                            //ESCRIBE DUAL
                            view.findViewById<EditText>(R.id.registerFctDual).setText(getString(R.string.dual))
                        }else {
                            //ESCRIBE FCT
                            view.findViewById<EditText>(R.id.registerFctDual).setText(getString(R.string.fct))
                        }

                        //data.roles
                        //data.chats
                    }
                }
                Resource.Status.ERROR -> {
                    // Handle login error
                    Toast.makeText(view.context, "There has been an error fetching your information, please restart", Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING -> {
                    // Handle loading state (optional)
                    // You can show a loading indicator or perform other actions while waiting
                }
            }
        }

        view.findViewById<Button>(R.id.registerAcceptButton).setOnClickListener{
            view.context.startActivity(Intent(view.context, GroupActivity::class.java))
        }

        return view
    }

}