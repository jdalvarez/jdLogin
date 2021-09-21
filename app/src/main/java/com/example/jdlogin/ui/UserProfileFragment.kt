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

    private val viewModel by viewModels<ProfileViewModel> { ProfileViewModelFactory(LoginRepoImpl(LoginDataSource())) }
    private lateinit var binding: FragmentUserProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated()

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }
        binding.btnSave.setOnClickListener {
            val firstName = binding.etName.text.toString()
            val lastName = binding.etLastname.text.toString()
            val age = binding.etAge.text.toString()
            val birthDate = binding.etDate.text.toString()
            viewModel.saveData(firstName, lastName, age, birthDate)
        }
    }

    private fun setupObservers(){
        viewModel.clientLiveData.observe(viewLifecycleOwner) {
            loadClientData(it)
        }
        viewModel.loadingLiveData.observe(viewLifecycleOwner) {
            showLoading(it)
        }
        viewModel.navigateToLogin.observe(viewLifecycleOwner) {
            showLogin()
        }
    }

    private fun loadClientData(client: Client) {
        binding.etName.setText(client.firstName)
        binding.etLastname.setText(client.lastName)
        binding.etAge.setText(client.age)
        binding.etDate.setText(client.dateOfBirth)
        binding.userName.setText(client.displayName)
        enableDisableFields(false)
    }

    private fun enableDisableFields(enable: Boolean) {
        binding.etName.isEnabled = enable
        binding.etLastname.isEnabled = enable
        binding.etAge.isEnabled = enable
        binding.etDate.isEnabled = enable
        binding.btnSave.visibility = if (enable) View.VISIBLE else View.INVISIBLE
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

    private fun showLogin() {
        val logoutAction = UserProfileFragmentDirections.actionUserProfileFragmentToAuthFragment()
        findNavController().navigate(logoutAction)
    }


}