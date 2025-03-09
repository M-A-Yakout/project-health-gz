package com.example.prject_gaza.data.model

data class User(
    var uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val status: String = "pending",
    val isAdmin: Boolean = false
)