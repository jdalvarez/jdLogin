package com.example.jdlogin.repo

import com.example.jdlogin.core.Response
import com.example.jdlogin.ui.Client
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot

interface LoginRepo {
    suspend fun signIn(email: String, password: String): FirebaseUser?
    suspend fun signUp(email: String, password: String): FirebaseUser?
    suspend fun saveData(firstName: String, lastName: String, age: String, birthDate: String, phoneNumber: String)
    suspend fun readClient(): Client?
}

