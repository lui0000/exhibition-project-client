package com.example.exhibitionapp.services

import com.example.exhibitionapp.dataclass.ExhibitionRequest
import com.example.exhibitionapp.dataclass.ExhibitionResponse
import com.example.exhibitionapp.dataclass.ExhibitionWithPaintingResponse
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpStatus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ExhibitionService {
    @GET("exhibition")
    suspend fun getExhibitions(@Header("Authorization") token: String): Response<List<ExhibitionWithPaintingResponse>>

    @GET("exhibition/{id}")
    suspend fun getExhibition(@Path("id") id: Int): Response<ExhibitionResponse>

    @POST("exhibition/add")
    suspend fun createExhibition(
        @Header("Authorization") token: String,
        @Body request: ExhibitionRequest
    ): Response<Void>

    @GET("exhibition/by-title/{title}")
    suspend fun getExhibitionByTitle(
        @Header("Authorization") token: String,
        @Path("title") title: String
    ): Response<ExhibitionResponse>

}
