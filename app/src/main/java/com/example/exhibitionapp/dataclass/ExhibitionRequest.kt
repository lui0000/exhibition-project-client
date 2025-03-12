package com.example.exhibitionapp.dataclass

data class ExhibitionRequest(
    val title: String,
    val description: String,
    val startDate: String,  // Формат: "yyyy-MM-dd"
    val endDate: String,    // Формат: "yyyy-MM-dd"
    val organizerId: Int    // ID организатора вместо целого объекта User
)
