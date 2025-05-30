package com.example.exhibitionapp.services

import com.example.exhibitionapp.dataclass.UserResponse
import com.google.android.gms.common.api.Response
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
