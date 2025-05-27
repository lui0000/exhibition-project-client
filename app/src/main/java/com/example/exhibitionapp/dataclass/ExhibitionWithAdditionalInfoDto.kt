package com.example.exhibitionapp.dataclass

data class ExhibitionWithAdditionalInfoDto(
    val photoData: String?,
    val exhibitionDto: ExhibitionDto,
    val paintingImages: List<String>,
    val artists: List<String>,
    val investors: List<String>
)

data class ExhibitionDto(
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val organizer: OrganizerDto
)

data class OrganizerDto(
    val name: String,
    val email: String,
    val role: String
)