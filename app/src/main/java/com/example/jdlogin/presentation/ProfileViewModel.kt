package com.example.jdlogin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.example.jdlogin.core.Response
import com.example.jdlogin.repo.LoginRepo
import kotlinx.coroutines.Dispatchers

class ProfileViewModel(private val repo: LoginRepo): ViewModel() {

    fun saveData(userName :String, firstName: String, lastName: String, age: String, birthDate: String, phoneNumber: String) =  liveData(Dispatchers.IO){
        emit(Response.Loading())
        try {
            emit(Response.Success(repo.saveData(userName, firstName, lastName, age, birthDate, phoneNumber)))
        }catch (e: Exception){
            emit(Response.Failure(e))
        }
    }

    fun readData() =  liveData(Dispatchers.IO){
        emit(Response.Loading())
        try {
            val client = repo.readClient()
            if(client != null) {
                emit(Response.Success(client))
            } else {
                emit(Response.Failure(Exception("Could not find data")))
            }
        }catch (e: Exception){
            emit(Response.Failure(e))
        }
    }

}

class ProfileViewModelFactory(private val repo: LoginRepo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(repo) as T
    }
}