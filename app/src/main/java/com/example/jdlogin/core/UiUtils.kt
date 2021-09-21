package com.example.jdlogin.ui

import android.widget.EditText

object UiUtils {
    fun validateField(field: EditText?, errorMsg: String): Boolean {
        var result = true
        field?.let {
            if(it.text.isNullOrEmpty()) {
                it.error = errorMsg
                result = false
            }
        }
        return result
    }
}