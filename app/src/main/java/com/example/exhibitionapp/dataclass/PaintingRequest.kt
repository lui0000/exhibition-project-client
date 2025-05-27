package com.example.exhibitionapp.dataclass

import com.google.gson.annotations.SerializedName

data class ArtistRequest(
    val userId: Int
)



data class ExhibitionInPaintingRequest(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate")   val endDate:   String
)



data class PaintingRequest(
    val title: String,
    val style: String?,
    val description: String?,
    @SerializedName("photoData") val photoData: String,
    val artist: ArtistRequest,
    val exhibition: ExhibitionInPaintingRequest
)