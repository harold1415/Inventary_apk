package com.example.myinventarioapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    var userName by mutableStateOf("")
    var userRole by mutableStateOf("")
    var userEmail by mutableStateOf("")

    fun setUserData(name: String, role: String, email: String) {
        userName = name
        userRole = role
        userEmail = email
    }

    fun clearUserData() {
        userName = ""
        userRole = ""
        userEmail = ""
    }
}