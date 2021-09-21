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
    private val viewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(
            LoginRepoImpl(
                LoginDataSource()
            )
        )
    }
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var binding: FragmentAuthBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val GOOGLE_SIGN_IN = 100
    private val callbackManager = CallbackManager.Factory.create()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAuthBinding.inflate(inflater, container, false)

        // Obtain the FirebaseAnalytics instance.
        analytics = Firebase.analytics
        //Analytics Event
        analytics.logEvent(
            "InitScreen",
            Bundle().apply { putString("message", "Integracion de firebase complete") })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        checkIfUserIsLoggedIn()
    }

    override fun onResume() {
        super.onResume()
        showLoading(false)
    }


    private fun setup() {
        binding.btnRegister.setOnClickListener { doEmailRegister() }
        binding.btnLogin.setOnClickListener { doEmailLogin() }
        binding.icGoogle.setOnClickListener { doGoogleLogin()
            showLoading(true)}
        binding.icFacebook.setOnClickListener { doFacebookLogin()
            showLoading(true)}
        binding.icPhone.setOnClickListener { showPhoneLogin() }
    }

    private fun createUser(email: String, pass: String) {
        viewModel.signUp(email, pass).observe(viewLifecycleOwner, { result ->
            when (result) {
                is Response.Loading -> {
                    showLoading(isLoading = true)
                }
                is Response.Success -> {
                    showLoading(isLoading = false)
                    showProfile(email)
                }
                is Response.Failure -> {
                    showLoading(isLoading = false)
                    Toast.makeText(requireContext(), "Error: ${result.exception}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun signIn(email: String, pass: String) {
        viewModel.signIn(email, pass).observe(viewLifecycleOwner, { result ->
            when (result) {
                is Response.Loading -> {
                    showLoading(isLoading = true)
                }
                is Response.Success -> {
                    showLoading(isLoading = false)
                    showProfile(email)
                }
                is Response.Failure -> {
                    showLoading(isLoading = false)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${result.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data) //facebook
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance()
                        .signInWithCredential(credential)
                        .addOnCompleteListener {
                            showLoading(false)
                            if (it.isSuccessful) {
                                showProfile(account.email ?: "")
                            } else {
                                showAlert()
                            }
                        }
                }
            } catch (e: ApiException) {
                showAlert()
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        builder.setMessage("Something has hapend, please try again")
        builder.setPositiveButton("Accept", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showProfile(email: String) {
        val loginAction =
            AuthFragmentDirections.actionAuthFragmentToUserProfileFragment(email)
        findNavController().navigate(loginAction)
    }

    private fun checkIfUserIsLoggedIn() {
        auth.currentUser?.let {
            showProfile(auth.currentUser!!.email.toString())
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

    private fun doFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    result?.let {
                        val token = it.accessToken

                        val credential = FacebookAuthProvider.getCredential(token.token)
                        FirebaseAuth.getInstance()
                            .signInWithCredential(credential)
                            .addOnCompleteListener {
                                showLoading(false)
                                if (it.isSuccessful) {
                                    LoginManager.getInstance().logOut()
                                    showProfile(it.result?.user?.email ?: "")
                                } else {
                                    showAlert()
                                }
                            }
                    }
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    showAlert()
                }

            })
    }

    private fun doEmailRegister() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()
        if (validateFormData(email, pass)) {
            createUser(email, pass)
        } else {
            // Show error, please complete user/pass
            showAlert()
        }
    }

    private fun doEmailLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()
        //Only Check if the email and pass are not empty
        if (validateFormData(email, pass)) {
            signIn(email, pass)
        } else {
            showAlert()
        }
    }
}

