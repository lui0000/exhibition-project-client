package com.example.exhibitionapp.repositories

import com.example.exhibitionapp.dataclass.UserResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserService {
    @GET("user/{id}")
    suspend fun getUserInfo(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): UserResponse
}
