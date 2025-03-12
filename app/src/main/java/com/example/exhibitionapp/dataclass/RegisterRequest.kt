package com.example.exhibitionapp.dataclass

data class RegisterRequest(
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String
)