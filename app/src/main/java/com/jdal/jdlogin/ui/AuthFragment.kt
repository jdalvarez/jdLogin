package com.jdal.jdlogin.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.jdal.jdlogin.data.remote.LoginDataSource
import com.jdal.jdlogin.databinding.FragmentAuthBinding
import com.jdal.jdlogin.presentation.AuthViewModel
import com.jdal.jdlogin.presentation.AuthViewModelFactory
import com.jdal.jdlogin.repo.LoginRepoImpl
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException



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
            try {
                GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)?.let {
                    viewModel.onGoogleSignInResult(it)
            }

            }catch (e:Exception){
                Log.d("GOOGLE",e.message.toString())
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
            .requestIdToken("1009174892765-ia2fgbmnpp1t1qja9329m0i15ro4h8s3.apps.googleusercontent.com")
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

