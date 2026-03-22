package com.example.film.database

data class FilmDTO(
    val id: Int,
    val poster_path : String?,
    val overview : String?,
    val vote_count : Int?,
    val original_title : String?,
    val runtime: Int? = null,
    val release_date: String? = null,
    val genres: List<GenreDTO>? = null,
    val production_countries: List<CountryDTO>? = null,
    val videos: VideoResponse? = null,
    val credits: CreditResponse? = null
)

data class GenreDTO(val name: String)
data class CountryDTO(val name: String)
data class VideoResponse(val results: List<VideoDTO>)
data class VideoDTO(val site: String, val type: String, val key: String)
data class CreditResponse(
    val crew: List<CrewDTO>,
    val cast: List<CastDTO>
)
data class CastDTO(val name: String, val character: String)
data class CrewDTO(val job: String, val name: String)