package com.example.tarik_tambang.api

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: RegisterData?
)

data class RegisterData(
    val id: Int,
    val username: String
)
