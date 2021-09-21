package com.example.jdlogin.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class VerifyViewModel(private val firebaseAuth: FirebaseAuth): ViewModel() {

    val loadingLiveData = MutableLiveData<Boolean>()
    val navigateToProfile = MutableLiveData<Boolean>()
    val onLoginError = MutableLiveData<String>()

    fun onViewCreated() {
        loadingLiveData.value = false
    }


    fun signInWithPhoneAuthCredential(otp: String, storedVerificationId: String) {
        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
            storedVerificationId, otp)
        loadingLiveData.value = true
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                navigateToProfile.value = true
            }else if(it.exception is FirebaseAuthInvalidCredentialsException){
                    onLoginError.value = "Invalid OTP"
                    loadingLiveData.value = false
            }
        }
    }



}

class VerifyViewModelFactory(private val firebaseAuth: FirebaseAuth): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VerifyViewModel(firebaseAuth) as T
    }
}