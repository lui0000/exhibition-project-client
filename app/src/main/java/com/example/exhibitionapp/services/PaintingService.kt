package com.example.exhibitionapp.services

import com.example.exhibitionapp.dataclass.PaintingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PaintingService {
    @POST("painting/add")
    suspend fun createPainting(
        @Header("Authorization") token: String,
        @Body request: PaintingRequest
    ): Response<Void>
}