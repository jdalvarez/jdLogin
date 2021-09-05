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
import com.example.jdlogin.R
import com.example.jdlogin.databinding.FragmentAuthBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class AuthFragment : Fragment() {
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var binding:FragmentAuthBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAuthBinding.inflate(inflater, container, false)

        // Obtain the FirebaseAnalytics instance.
        analytics = Firebase.analytics
        //Analytics Events
        val bundle = Bundle()
        bundle.putString("message", "Integracion de firebase complete")
        analytics.logEvent("InitScreen", bundle)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        binding.btnLogin.setOnClickListener { navController.navigate(R.id.userProfileFragment) }
    }

    //setup
   // setup()

    private fun setup(){
        binding.btnLogin.setOnClickListener {
            if (binding.etEmail.text.isNotEmpty() && binding.etPassword.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(context,"User Created", Toast.LENGTH_SHORT).show()
                    }else{
                        showAlert()
                    }
                }
            }
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        builder.setMessage("Something has hapend, please try again")
        builder.setPositiveButton("Accept", null)
        val dialog:AlertDialog = builder.create()
        dialog.show()
    }

    private fun showProfile(email:String){
        val profileIntent = Intent(context, UserProfileFragment::class.java).apply {
            putExtra("email", email)
        }
        startActivity(profileIntent)
    }


}