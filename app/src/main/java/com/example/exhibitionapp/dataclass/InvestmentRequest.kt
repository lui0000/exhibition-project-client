package com.example.exhibitionapp.dataclass

import com.google.gson.annotations.SerializedName

data class InvestmentExhibitionRequest(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate")   val endDate:   String
)

data class InvestmentRequest(
    val exhibition: InvestmentExhibitionRequest,
    val amount: String,
    val investorId: Int
)