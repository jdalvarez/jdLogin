package com.jdal.jdlogin.repo

import com.jdal.jdlogin.ui.Client
import com.google.firebase.auth.FirebaseUser

interface LoginRepo {
    suspend fun signIn(email: String, password: String): FirebaseUser?
    suspend fun signUp(email: String, password: String): FirebaseUser?
    suspend fun saveData(firstName: String, lastName: String, age: String, birthDate: String, phoneNumber: String)
    suspend fun readClient(): Client?
}

