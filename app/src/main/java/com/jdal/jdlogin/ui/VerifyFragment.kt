package com.jdal.jdlogin.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jdal.jdlogin.databinding.FragmentVerifyBinding
import com.jdal.jdlogin.presentation.VerifyViewModel
import com.jdal.jdlogin.presentation.VerifyViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.jdal.jdlogin.core.UiUtils

class VerifyFragment : Fragment() {
    private val viewModel by viewModels<VerifyViewModel> {
        VerifyViewModelFactory(
            FirebaseAuth.getInstance()
        )
    }
    private lateinit var binding: FragmentVerifyBinding
    lateinit var auth: FirebaseAuth
    private val args: VerifyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerifyBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        viewModel.onViewCreated()

        binding.btnVerify.setOnClickListener {
            val otp = binding.pinView.text.toString().trim()
            if (validateField()) {
                viewModel.signInWithPhoneAuthCredential(otp, args.storeVerificationId)
            }
        }
    }

    private fun setupObservers() {
        viewModel.onLoginError.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.navigateToProfile.observe(viewLifecycleOwner) {
            phoneLogin()
        }
        viewModel.loadingLiveData.observe(viewLifecycleOwner) {
            showLoading(it)
        }
    }

    private fun validateField(): Boolean {
        return UiUtils.validateField(binding.pinView, "Please enter the given Code")
    }

    private fun phoneLogin() {
        val phoneLoginAction =
            VerifyFragmentDirections.actionVerifyFragmentToUserProfileFragment()
        findNavController().navigate(phoneLoginAction)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pinView.apply {
            isEnabled = !isLoading
            alpha = if (isLoading) 0.2F else 1.0F
        }
        binding.progressBar.apply {
            visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
        }
    }

}