package com.example.jdlogin.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jdlogin.databinding.FragmentUserProfileBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth

class UserProfileFragment : Fragment() {
    private lateinit var binding : FragmentUserProfileBinding
    private val args: UserProfileFragmentArgs by navArgs()
    lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding.userName.text = args.email
        setLogOut(args.provider)
        val currentUser = auth.currentUser

        if(currentUser == null){
            showLogin()
        }
    }

    private fun setLogOut(provider: ProviderType) {
        binding.btnLogout.setOnClickListener {
            if(provider == ProviderType.FACEBOOK){
                LoginManager.getInstance().logOut()
            }
            auth.signOut()
//            PrefsProvider.clear(requireContext())
//            val logoutAction = UserProfileFragmentDirections.actionUserProfileFragmentToAuthFragment()
//            findNavController().navigate(logoutAction)
            showLogin()
        }
    }

    private fun showLogin() {
        PrefsProvider.clear(requireContext())
        val logoutAction = UserProfileFragmentDirections.actionUserProfileFragmentToAuthFragment()
        findNavController().navigate(logoutAction)
    }


}