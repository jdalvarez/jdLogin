package com.example.jdlogin.presentation

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.example.jdlogin.repo.LoginRepo
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(private val repo: LoginRepo) : ViewModel() {

    val navigateToPhoneLogin = SingleLiveEvent<Boolean>()
    val navigateToProfile = SingleLiveEvent<Boolean>()
    val showErrorLiveData = SingleLiveEvent<String>()
    val loadingLiveData = MutableLiveData<Boolean>()

    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var facebookCallbackManager: CallbackManager

    fun onViewCreated(callbackManager: CallbackManager) {
        facebookCallbackManager = callbackManager
        if (auth.currentUser != null) {
            navigateToProfile.value = true
        }
    }

    fun signIn(email: String, password: String) {
        loadingLiveData.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.signIn(email, password)
            withContext(Dispatchers.Main) {
                loadingLiveData.value = false
                if (result != null) {
                    navigateToProfile.value = true
                } else {
                    showErrorLiveData.value = "Please enter a valid Email / Password combination"
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        loadingLiveData.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.signUp(email, password)
            withContext(Dispatchers.Main) {
                loadingLiveData.value = false
                if (result != null) {
                    navigateToProfile.value = true
                } else {
                    showErrorLiveData.value = "Unable to sign up"
                }
            }
        }
    }

    fun doPhoneLogin() {
        navigateToPhoneLogin.value = true
    }

    fun doFacebookLogin(fragment: Fragment) {
        loadingLiveData.value = true
        LoginManager.getInstance().logInWithReadPermissions(fragment, listOf("email"))
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    result?.let {
                        val token = it.accessToken
                        val credential = FacebookAuthProvider.getCredential(token.token)
                        FirebaseAuth.getInstance()
                            .signInWithCredential(credential)
                            .addOnCompleteListener {
                                loadingLiveData.value = false
                                if (it.isSuccessful) {
                                    LoginManager.getInstance().logOut()
                                    navigateToProfile.value = true
                                } else {
                                    showErrorLiveData.value = "Unable to signin to facebook"
                                }
                            }
                    }
                }
                override fun onCancel() {}
                override fun onError(error: FacebookException?) {
                    showErrorLiveData.value = "Unable to signin to facebook"
                }
            })
    }

    fun onGoogleSignInResult(googleSignInAccount: GoogleSignInAccount) {
        try {
            val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
            FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener {
                    loadingLiveData.value = false
                    if (it.isSuccessful) {
                        navigateToProfile.value = true
                    } else {
                        showErrorLiveData.value = "Error in google signin"
                    }
                }
        } catch (e: ApiException) {
            showErrorLiveData.value = "Error in google signin"
        }
    }
}

class AuthViewModelFactory(private val repo: LoginRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repo) as T
    }
}