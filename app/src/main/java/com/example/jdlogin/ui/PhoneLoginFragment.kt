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
import androidx.navigation.fragment.findNavController
import com.example.jdlogin.R
import com.example.jdlogin.databinding.FragmentPhoneLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class PhoneLoginFragment : Fragment() {
  private lateinit var binding:FragmentPhoneLoginBinding
  lateinit var auth: FirebaseAuth
  lateinit var storedVerificationId:String
  lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
  private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentPhoneLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null){
            showLogin()
            auth.signOut()
        }

        binding.loginBtn.setOnClickListener {
            sendOTP()
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.textProcess.apply {
                    text = e.message
                    setTextColor(Color.RED)
                    visibility = View.VISIBLE
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
                binding.textProcess.apply {
                    text = getString(R.string.OTP_sent)
                    setTextColor(Color.GREEN)
                    visibility = View.VISIBLE
                }
                showVerify(storedVerificationId)
            }

        }


    }

    private fun showLogin() {
        PrefsProvider.clear(requireContext())
        val logoutAction = UserProfileFragmentDirections.actionUserProfileFragmentToAuthFragment()
        findNavController().navigate(logoutAction)
    }

    private fun sendOTP(){
        val countryCode = binding.countryCode.text
        val phone = binding.phoneNumber.text
        val phoneNumber = "+$countryCode $phone"
        if (countryCode.isNotEmpty() || phone.isNotEmpty()){
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)                    // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS)      // Timeout and unit
                .setActivity(requireActivity())                // Activity (for callback binding)
                .setCallbacks(callbacks)                       // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }else{
            binding.textProcess.apply {
                text = getString(R.string.process_text_fail)
                setTextColor(Color.RED)
                visibility = View.VISIBLE
            }
        }
    }

    private fun showVerify(storedVerificationId: String) {
        val goVerifyAction = PhoneLoginFragmentDirections.actionPhoneLoginFragmentToVerifyFragment(storedVerificationId)
        findNavController().navigate(goVerifyAction)
    }

    private fun showProfile(email: String, provider: ProviderType) {
        val loginAction = PhoneLoginFragmentDirections.actionPhoneLoginFragmentToUserProfileFragment(email,provider)
        findNavController().navigate(loginAction)
    }
    private fun signIn(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful) {
                showProfile( "", ProviderType.PHONE)
            } else {
                binding.textProcess.apply {
                    text = it.exception?.message
                    setTextColor(Color.RED)
                    visibility = View.VISIBLE
                }
            }
        }
    }


}