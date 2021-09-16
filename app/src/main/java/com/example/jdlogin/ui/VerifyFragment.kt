package com.example.jdlogin.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jdlogin.R
import com.example.jdlogin.databinding.FragmentAuthBinding
import com.example.jdlogin.databinding.FragmentVerifyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class VerifyFragment : Fragment() {
    private lateinit var binding: FragmentVerifyBinding
    lateinit var auth: FirebaseAuth
    private val args: VerifyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerifyBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth=FirebaseAuth.getInstance()
        val otpGiven = binding.pinView
        val storedVerificationId = args.storeVerificationId

        binding.btnVerify.setOnClickListener {
            val otp = otpGiven.text.toString().trim()
            if(otp.isNotEmpty()){
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId, otp
                )
                signInWithPhoneAuthCredential(credential)
            }else{
                Toast.makeText(context,"Enter OTP",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                phoneLogin()
            }else{
                if(it.exception is FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(context,"Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun phoneLogin() {
        val phoneLoginAction =
            VerifyFragmentDirections.actionVerifyFragmentToUserProfileFragment("", ProviderType.PHONE)
        findNavController().navigate(phoneLoginAction)
    }

}