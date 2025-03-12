package com.example.exhibitionapp.services

import com.example.exhibitionapp.dataclass.ExhibitionRequest
import com.example.exhibitionapp.dataclass.ExhibitionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ExhibitionService {
    @GET("exhibitions")
    suspend fun getExhibitions(): Response<List<ExhibitionResponse>>

    @GET("exhibitions/{id}")
    suspend fun getExhibition(): Response<ExhibitionResponse>

    @POST("exhibitions/add")
    suspend fun createExhibition(@Body request: ExhibitionRequest): Response<ExhibitionResponse>

//    @DELETE("exhibitions/{id}")
//    suspend fun deleteExhibition(@Path("id") id: Int): Response<Void>
}