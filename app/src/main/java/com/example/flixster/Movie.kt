package com.example.flixster

data class Movie(
    val title: String,
    val overview: String,
    val posterPath: String?,
    val releaseDate: String
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500/$it" }
}
