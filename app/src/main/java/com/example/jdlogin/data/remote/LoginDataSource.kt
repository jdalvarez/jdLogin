package com.example.jdlogin.data.remote

import com.example.jdlogin.ui.Client
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class LoginDataSource {

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        return withContext(Dispatchers.IO) {
            val authResult =
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
            authResult.user
        }
    }

    suspend fun signUp(email: String, password: String): FirebaseUser? {
        return withContext(Dispatchers.IO) {
            val authResult =
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
            authResult.user
        }
    }

    suspend fun saveData(
        userName: String, firstName: String, lastName: String, age: String,
        birthDate: String, phoneNumber: String
    ) {
        val authResult = FirebaseAuth.getInstance().currentUser
        if (authResult != null) {
            withContext(Dispatchers.IO) {
                val database = FirebaseDatabase.getInstance().getReference("Clients")
                val client = Client(userName, firstName, lastName, age, birthDate, phoneNumber)
                database.child(authResult.uid).setValue(client).await()
            }
        }else{
            throw (Exception("Something was wrang"))
        }
    }

    suspend fun readClient(): DataSnapshot? {
        val authResult = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance().getReference("Clients")
        var result: DataSnapshot? = null
        withContext(Dispatchers.IO) {
            if (authResult != null) {
                database.child(authResult.uid).get().addOnSuccessListener {
                    result = it
                }.await()
            }
        }
        return result
    }
}

