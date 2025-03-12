package com.example.exhibitionapp.services

import com.example.exhibitionapp.dataclass.LoginRequest
import com.example.exhibitionapp.dataclass.LoginResponse
import com.example.exhibitionapp.dataclass.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface  AuthService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>
}