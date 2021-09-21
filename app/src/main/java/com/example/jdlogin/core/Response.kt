package com.example.jdlogin.core

import java.lang.Exception

sealed class Response<out T>{
    class Loading<out T>:Response<T>()
    data class  Success <out T>(val data: T): Response<T>()
    data class  Failure (val exception: Exception): Response<Nothing>()
}
