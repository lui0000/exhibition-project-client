package com.example.exhibitionapp.dataclass

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class ExhibitionRequest(
    val title: String,
    val description: String,
    @SerializedName("startDate")
    val startDateStr: String,
    @SerializedName("endDate")
    val endDateStr: String,
    val organizer: OrganizerRequest
)

data class OrganizerRequest(
    @SerializedName("userId")
    val userId: Int
)

