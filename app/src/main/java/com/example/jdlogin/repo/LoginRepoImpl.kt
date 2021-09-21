package com.example.jdlogin.repo

import android.view.View
import com.example.jdlogin.core.Response
import com.example.jdlogin.data.remote.LoginDataSource
import com.example.jdlogin.ui.Client
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot

class LoginRepoImpl(private val dataSource: LoginDataSource) : LoginRepo {

    override suspend fun signIn(email: String, password: String): FirebaseUser? = dataSource.signIn(email, password)
    override suspend fun signUp(email: String, password: String): FirebaseUser? = dataSource.signUp(email,password)
    override suspend fun saveData(
        userName: String,
        firstName: String,
        lastName: String,
        age: String,
        birthDate: String,
        phoneNumber: String
    ) = dataSource.saveData(userName, firstName, lastName, age , birthDate, phoneNumber)

    override suspend fun readClient(): Client? {
        var result: Client? = null
        dataSource.readClient()?.let {
            if(it.exists()) {
                val firstName = it.child("firstName").value.toString()
                val lastName = it.child("lastName").value.toString()
                val age = it.child("age").value.toString()
                val dateOfBirth = it.child("dateOfBirth").value.toString()
                result = Client(firstName = firstName, lastName = lastName, age = age, dateOfBirth = dateOfBirth)
            }
        }
        return result
    }


}