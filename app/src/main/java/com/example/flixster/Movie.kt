package com.example.flixster

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val backdropPath: String?,
    val runtimeMinutes: Int? = null,
    val genres: List<String> = emptyList(),
    val tagline: String = ""
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500/$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780/$it" }
}
