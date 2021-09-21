package com.example.jdlogin.ui

import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.Color.red
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.jdlogin.R
import com.example.jdlogin.data.remote.LoginDataSource
import com.example.jdlogin.databinding.FragmentPhoneLoginBinding
import com.example.jdlogin.presentation.AuthViewModel
import com.example.jdlogin.presentation.AuthViewModelFactory
import com.example.jdlogin.repo.LoginRepoImpl
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class PhoneLoginFragment : Fragment() {
    private val viewModel by viewModels<PhoneLoginViewModel> {
        PhoneLoginViewModelFactory(
            FirebaseAuth.getInstance()
        )
    }

    private lateinit var binding: FragmentPhoneLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhoneLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        viewModel.onViewCreated()

        binding.loginBtn.setOnClickListener {
            if (validateFields()) {
                viewModel.sendVerificationCodeOTP(
                    binding.countryCode.text.toString(),
                    binding.phoneNumber.text.toString(),
                    requireActivity()
                )
            }
        }
    }

    fun setupObservers() {
        viewModel.loadingLiveData.observe(viewLifecycleOwner) {
            showLoading(it)
        }
        viewModel.navigateToLogin.observe(viewLifecycleOwner) {
            showLogin()
        }
        viewModel.navigateToVerify.observe(viewLifecycleOwner) {
            it?.let{
                showVerify(it)
            }

        }
        viewModel.navigateToProfile.observe(viewLifecycleOwner) {
            showProfile()
        }
        viewModel.onShowVerificationError.observe(viewLifecycleOwner) {
            binding.textProcess.apply {
                text = it
                setTextColor(Color.RED)
                visibility = View.VISIBLE
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.countryCode.apply {
            isEnabled = !isLoading
            alpha = if (isLoading) 0.2F else 1.0F
        }
        binding.phoneNumber.apply {
            isEnabled = !isLoading
            alpha = if (isLoading) 0.2F else 1.0F
        }
        binding.progressBar.apply {
            visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun validateFields(): Boolean {
        val validCountryCode =
            UiUtils.validateField(binding.countryCode, "Please enter country code")
        val validPhoneNumber =
            UiUtils.validateField(binding.phoneNumber, "Please enter a phone number")

        return validCountryCode && validPhoneNumber
    }

    private fun showLogin() {
        val logoutAction = PhoneLoginFragmentDirections.actionPhoneLoginFragmentToAuthFragment()
        findNavController().navigate(logoutAction)
    }

    private fun showVerify(storedVerificationId: String) {
        val goVerifyAction = PhoneLoginFragmentDirections.actionPhoneLoginFragmentToVerifyFragment(
            storedVerificationId
        )
        findNavController().navigate(goVerifyAction)
    }

    private fun showProfile() {
        val loginAction =
            PhoneLoginFragmentDirections.actionPhoneLoginFragmentToUserProfileFragment()
        findNavController().navigate(loginAction)
    }


}