package com.jdal.jdlogin.presentation

import androidx.lifecycle.*
import com.jdal.jdlogin.repo.LoginRepo
import com.jdal.jdlogin.ui.Client
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(private val repo: LoginRepo): ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    val navigateToLogin = SingleLiveEvent<Boolean>()
    val loadingLiveData = MutableLiveData<Boolean>()
    val clientLiveData = MutableLiveData<Client>()

    fun onViewCreated(){
        val currentUser = auth.currentUser
        if (currentUser == null){
            navigateToLogin.value = true
        }else {
            viewModelScope.launch (Dispatchers.IO){
                val result = repo.readClient()
                withContext(Dispatchers.Main){
                    result?.let { clientLiveData.value = it }
                }
            }
        }
    }
    fun logout(){
        loadingLiveData.value = true
        auth.signOut()
        navigateToLogin.value = true
    }

    fun saveData(firstName: String, lastName: String, age: String, birthDate: String) {
        loadingLiveData.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val phone: String = auth.currentUser?.phoneNumber ?: ""
            repo.saveData(firstName, lastName, age, birthDate, phone)
            withContext(Dispatchers.Main) {
                loadingLiveData.value = false
                readData()
            }
        }
    }

    private fun readData() {
        loadingLiveData.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val client = repo.readClient()
            withContext(Dispatchers.Main) {
                client?.let {  clientLiveData.value = it }
                loadingLiveData.value = false
            }
        }
    }
}

class ProfileViewModelFactory(private val repo: LoginRepo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(repo) as T
    }
}