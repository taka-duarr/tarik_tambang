package com.example.tarik_tambang.api

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserData?
)

data class UserData(
    val id: Int,
    val username: String
)
