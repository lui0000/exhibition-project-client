package com.example.exhibitionapp.dataclass

data class LoginRequest(
    val email: String,
    val passwordHash: String
)