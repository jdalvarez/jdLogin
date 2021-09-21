package com.example.jdlogin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.example.jdlogin.core.Response
import com.example.jdlogin.repo.LoginRepo
import com.facebook.CallbackManager
import kotlinx.coroutines.Dispatchers

class AuthViewModel(private val repo: LoginRepo): ViewModel() {

    fun signIn(email: String, password: String) =  liveData(Dispatchers.IO){
        emit(Response.Loading())
        try {
            emit(Response.Success(repo.signIn(email, password)))
        }catch (e: Exception){
            emit(Response.Failure(e))
        }
    }

    fun signUp(email: String, password: String) =  liveData(Dispatchers.IO){
        emit(Response.Loading())
        try {
            emit(Response.Success(repo.signUp(email, password)))
        }catch (e: Exception){
            emit(Response.Failure(e))
        }
    }



}

class AuthViewModelFactory(private val repo: LoginRepo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repo) as T
    }
}