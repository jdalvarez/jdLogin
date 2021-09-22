package com.jdal.jdlogin.presentation

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneLoginViewModel(private val firebaseAuth: FirebaseAuth): ViewModel() {

    val loadingLiveData = MutableLiveData<Boolean>()
    val navigateToProfile = SingleLiveEvent<Boolean>()
    val navigateToVerify = SingleLiveEvent<String>()
    val navigateToLogin = SingleLiveEvent<Boolean>()
    val onShowVerificationError = SingleLiveEvent<String>()

    fun onViewCreated() {
        val user = firebaseAuth.currentUser
        loadingLiveData.value = false

        if (user != null){
            navigateToLogin.value = true
            firebaseAuth.signOut()
        }
    }

    fun sendVerificationCodeOTP(countryCode: String?, phone: String?, activity: Activity){
        loadingLiveData.value = true
        val phoneNumber = "+$countryCode $phone"
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)                    // Phone number to verify
            .setTimeout(5L, TimeUnit.SECONDS)      // Timeout and unit
            .setActivity(activity)                // Activity (for callback binding)
            .setCallbacks(callbacks)                       // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            loadingLiveData.value = false
            signIn(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            loadingLiveData.value = false
            onShowVerificationError.value = e.message
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            loadingLiveData.value = false
            navigateToVerify.value = verificationId
        }

    }

    private fun signIn(credential: PhoneAuthCredential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful) {
                navigateToProfile.value = true
            } else {
                onShowVerificationError.value = it.exception?.message
            }
        }
    }

}

class PhoneLoginViewModelFactory(private val firebaseAuth: FirebaseAuth): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhoneLoginViewModel(firebaseAuth) as T
    }
}