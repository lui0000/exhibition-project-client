package com.example.exhibitionapp.services

import com.example.exhibitionapp.dataclass.InvestmentRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.Response

interface InvestmentService {
    @POST("investment/add")
    suspend fun createInvestment(
        @Header("Authorization") token: String,
        @Body request: InvestmentRequest
    ): Response<Void>
}
