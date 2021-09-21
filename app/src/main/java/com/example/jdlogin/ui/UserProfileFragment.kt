package com.example.jdlogin.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.jdlogin.core.Response
import com.example.jdlogin.data.remote.LoginDataSource
import com.example.jdlogin.databinding.FragmentUserProfileBinding
import com.example.jdlogin.presentation.ProfileViewModel
import com.example.jdlogin.presentation.ProfileViewModelFactory
import com.example.jdlogin.repo.LoginRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserProfileFragment : Fragment() {

    private val viewModel by viewModels<ProfileViewModel> {
        ProfileViewModelFactory(
            LoginRepoImpl(
                LoginDataSource()
            )
        )
    }
    private lateinit var binding: FragmentUserProfileBinding
    lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setLogOut()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            showLogin()
        } else {
            readClient(currentUser.uid)
            if (currentUser.displayName == "" || currentUser.displayName == null) {
                binding.userName.text = currentUser.phoneNumber
            } else {
                binding.userName.text = currentUser.displayName
            }
        }

        saveClient()
    }

    private fun readClient(id: String) {
        viewModel.readData().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Response.Loading -> {
                    showLoading(isLoading = true)
                }
                is Response.Success -> {
                    showLoading(isLoading = false)
                    enableDisableFields(false)
                    loadClientData(result.data)
                }
                is Response.Failure -> {
                    showLoading(isLoading = false)
                    enableDisableFields(true)
                    Toast.makeText(context, "Something was wrong, please try again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadClientData(client: Client) {
        binding.etName.setText(client.firstName)
        binding.etLastname.setText(client.lastName)
        binding.etAge.setText(client.age)
        binding.etDate.setText(client.dateOfBirth)
    }

    private fun enableDisableFields(enable: Boolean) {
        binding.etName.isEnabled = enable
        binding.etLastname.isEnabled = enable
        binding.etAge.isEnabled = enable
        binding.etDate.isEnabled = enable
        binding.btnSave.visibility = if (enable) View.VISIBLE else View.INVISIBLE
    }

    private fun saveClient() {
        binding.btnSave.setOnClickListener {
            val id = auth.currentUser?.uid ?: ""
            val userName = auth.currentUser?.displayName
            val firstName = binding.etName.text.toString()
            val lastName = binding.etLastname.text.toString()
            val age = binding.etAge.text.toString()
            val birthDate = binding.etDate.text.toString()
            val phoneNumber = auth.currentUser?.phoneNumber

            viewModel.saveData(
                userName ?: auth.currentUser?.email.toString(),
                firstName,
                lastName,
                age,
                birthDate,
                phoneNumber ?: ""
            ).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Response.Loading -> {
                        showLoading(isLoading = true)
                    }
                    is Response.Success -> {
                        showLoading(isLoading = false)
                        Toast.makeText(context, "Client saved", Toast.LENGTH_SHORT).show()
                        readClient(id)
                    }
                    is Response.Failure -> {
                        showLoading(isLoading = false)
                        Toast.makeText(
                            context,
                            "Something was wrong, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            binding.btnLogout.isEnabled = false
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.btnSave.isEnabled = true
            binding.btnLogout.isEnabled = true
        }
    }

    private fun setLogOut() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            showLogin()
        }
    }

    private fun showLogin() {
        val logoutAction = UserProfileFragmentDirections.actionUserProfileFragmentToAuthFragment()
        findNavController().navigate(logoutAction)
    }


}