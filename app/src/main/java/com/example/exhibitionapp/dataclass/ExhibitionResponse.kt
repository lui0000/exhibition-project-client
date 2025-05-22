package com.example.exhibitionapp.dataclass

data class ExhibitionResponse(
    val id: Int,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val organizer: UserResponse
)
