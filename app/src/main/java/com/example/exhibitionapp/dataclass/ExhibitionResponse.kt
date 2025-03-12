package com.example.exhibitionapp.dataclass

data class ExhibitionResponse(
    val id: Int,
    val title: String,
    val description: String,
    val startDate: String,  // Формат: "yyyy-MM-dd"
    val endDate: String,    // Формат: "yyyy-MM-dd"
    val organizer: UserResponse
)
