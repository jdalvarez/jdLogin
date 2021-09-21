package com.example.jdlogin.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.jdlogin.core.Response
import com.example.jdlogin.data.remote.LoginDataSource
import com.example.jdlogin.databinding.FragmentAuthBinding
import com.example.jdlogin.presentation.AuthViewModel
import com.example.jdlogin.presentation.AuthViewModelFactory
import com.example.jdlogin.repo.LoginRepoImpl
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase


class AuthFragment : Fragment() {
    private val viewModel by viewModels<AuthViewModel> { AuthViewModelFactory(LoginRepoImpl(LoginDataSource())) }

    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private lateinit var binding: FragmentAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(inflater, container, false)
        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(callbackManager)
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.btnRegister.setOnClickListener { showLoading(true)
            doEmailRegister() }
        binding.btnLogin.setOnClickListener { showLoading(true)
            doEmailLogin() }
        binding.icGoogle.setOnClickListener { doGoogleLogin()
            showLoading(true)}
        binding.icFacebook.setOnClickListener { showLoading(true)
            viewModel.doFacebookLogin(this) }
        binding.icPhone.setOnClickListener { showLoading(true)
            viewModel.doPhoneLogin() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data) //facebook
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)?.let {
                viewModel.onGoogleSignInResult(it)
            }
        }
    }

    private fun showAlert(msg: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Accept", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showProfile() {
        val loginAction =
            AuthFragmentDirections.actionAuthFragmentToUserProfileFragment()
        findNavController().navigate(loginAction)
    }

    private fun setupObservers(){
        viewModel.navigateToProfile.observe(viewLifecycleOwner){
            showProfile()
        }
        viewModel.loadingLiveData.observe(viewLifecycleOwner) {
            showLoading(it)
        }
        viewModel.showErrorLiveData.observe(viewLifecycleOwner) {
            showAlert(it)
        }
        viewModel.navigateToPhoneLogin.observe(viewLifecycleOwner) {
            showPhoneLogin()
        }
    }


    private fun showPhoneLogin() {
        val toPhoneAction =
            AuthFragmentDirections.actionAuthFragmentToPhoneLoginFragment()
        findNavController().navigate(toPhoneAction)
    }

    private fun validateFormData(email: String?, password: String?): Boolean {
        var isValid = true
        if (email.isNullOrEmpty()) {
            binding.etEmail.error = "E-mail is empty"
            isValid = false
        }
        if (password.isNullOrEmpty()) {
            binding.etPassword.error = "Password is empty"
            isValid = false
        }
        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            binding.btnRegister.isEnabled = false
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.btnLogin.isEnabled = true
            binding.btnRegister.isEnabled = true
        }
    }

    private fun doGoogleLogin() {
        val googleConf = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("378944346534-iq3pjq811hk2kvvaivsgpja9uglmp7f1.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(requireContext(), googleConf)
        googleClient.signOut()
        startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
    }

    private fun doEmailRegister() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()
        if (validateFormData(email, pass)) {
            viewModel.signUp(email, pass)
        } else {
            // Show error, please complete user/pass
            showAlert("Please enter email/pass")
        }
    }

    private fun doEmailLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()
        //Only Check if the email and pass are not empty
        if (validateFormData(email, pass)) {
            viewModel.signIn(email, pass)
        } else {
            showAlert("Please enter email/pass")
        }
    }

    companion object {
        private const val GOOGLE_SIGN_IN = 100
    }
}

