package com.example.jdlogin.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.jdlogin.databinding.FragmentAuthBinding
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
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.ktx.Firebase


class AuthFragment : Fragment() {
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var binding: FragmentAuthBinding
    lateinit var auth: FirebaseAuth
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
        val bundle = Bundle()
        bundle.putString("message", "Integracion de firebase complete")
        analytics.logEvent("InitScreen", bundle)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setup()
        session()

    }


    private fun setup() {
        //Register Button
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text
            val pass = binding.etPassword.text
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email.toString(), pass.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context, "User Created", Toast.LENGTH_SHORT).show()
                            showProfile(it.result.user?.email ?: "", ProviderType.BASIC.toString())
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        //Login Button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text
            val pass = binding.etPassword.text
            //Only Check if the email and pass are not empty
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email.toString(), pass.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            PrefsProvider.saveData(
                                binding.etEmail.text.toString(),
                                requireContext(),
                                ProviderType.BASIC.toString()
                            )
                            showProfile(it.result.user?.email ?: "", ProviderType.BASIC.toString())
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        binding.icGoogle.setOnClickListener {
            val googleConf = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("378944346534-iq3pjq811hk2kvvaivsgpja9uglmp7f1.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(context, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        binding.icFacebook.setOnClickListener {
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
                                    if (it.isSuccessful) {
                                        showProfile(
                                            it.result.user?.email ?: "",
                                            ProviderType.FACEBOOK.toString()
                                        )
                                    } else {
                                        showAlert()
                                    }
                                }
                        }
                    }

                    override fun onError(error: FacebookException?) {
                        showAlert()
                    }

                    override fun onCancel() {
                    }
                })
        }

        binding.icPhone.setOnClickListener {
            showPhoneLogin()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
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
                            if (it.isSuccessful) {
                                showProfile(account.email ?: "", ProviderType.GOOGLE.toString())
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

    private fun showProfile(email: String, provider: String) {
        val loginAction =
            AuthFragmentDirections.actionAuthFragmentToUserProfileFragment(email, ProviderType.valueOf(provider))
        findNavController().navigate(loginAction)
    }

   private fun session() {
       val email : String? = PrefsProvider.readEmail(requireContext())
       val providerType : String? = PrefsProvider.readAuthProvider(requireContext())
        if (email != null && providerType != null) {
            showProfile(email.toString(), ProviderType.valueOf(providerType).toString())
        }
    }

    private fun showPhoneLogin() {
        val toPhoneAction =
            AuthFragmentDirections.actionAuthFragmentToPhoneLoginFragment()
        findNavController().navigate(toPhoneAction)
    }
}

