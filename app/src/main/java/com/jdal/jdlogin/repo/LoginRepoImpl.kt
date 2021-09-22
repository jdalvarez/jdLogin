package com.jdal.jdlogin.repo

import com.jdal.jdlogin.data.remote.LoginDataSource
import com.jdal.jdlogin.ui.Client
import com.google.firebase.auth.FirebaseUser

class LoginRepoImpl(private val dataSource: LoginDataSource) : LoginRepo {

    override suspend fun signIn(email: String, password: String): FirebaseUser? = dataSource.signIn(email, password)
    override suspend fun signUp(email: String, password: String): FirebaseUser? = dataSource.signUp(email,password)
    override suspend fun saveData(
        firstName: String,
        lastName: String,
        age: String,
        birthDate: String,
        phoneNumber: String
    ) {
        dataSource.saveData(firstName, lastName, age , birthDate, phoneNumber)
    }

    override suspend fun readClient(): Client? {
        var result: Client? = null
        dataSource.readClient()?.let {
            if(it.exists()) {
                val firstName = it.child("firstName").value.toString()
                val lastName = it.child("lastName").value.toString()
                val age = it.child("age").value.toString()
                val dateOfBirth = it.child("dateOfBirth").value.toString()
                val displayName = it.child("displayName").value.toString()
                result = Client(firstName = firstName, lastName = lastName, age = age, dateOfBirth = dateOfBirth, displayName = displayName)
            }
        }
        return result
    }


}