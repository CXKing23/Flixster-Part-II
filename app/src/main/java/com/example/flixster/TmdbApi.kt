package com.example.flixster

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

enum class MovieCategory(val endpoint: String) {
    POPULAR("popular"),
    TOP_RATED("top_rated")
}

object TmdbApi {
    private const val API_KEY = "a07e22bc18f5cb106bfe4cc1f83ad8ed"
    private const val API_BASE = "https://api.themoviedb.org/3/movie"

    fun fetchMovies(category: MovieCategory): List<Movie> {
        val payload = getJson("$API_BASE/${category.endpoint}?language=en-US&page=1")
        val results = payload.getJSONArray("results")
        return buildList(results.length()) {
            for (index in 0 until results.length()) {
                add(results.getJSONObject(index).toMovie())
            }
        }
    }

    fun fetchMovieDetails(movieId: Int): Movie {
        val movie = getJson("$API_BASE/$movieId?language=en-US")
        val genreArray = movie.optJSONArray("genres")
        val genres = buildList {
            if (genreArray != null) {
                for (index in 0 until genreArray.length()) {
                    add(genreArray.getJSONObject(index).optString("name"))
                }
            }
        }

        return movie.toMovie().copy(
            runtimeMinutes = movie.optInt("runtime").takeIf { it > 0 },
            genres = genres,
            tagline = movie.optString("tagline")
        )
    }

    private fun JSONObject.toMovie(): Movie = Movie(
        id = optInt("id"),
        title = optString("title").ifBlank { "Untitled movie" },
        overview = optString("overview").ifBlank { "No description available." },
        posterPath = optString("poster_path").takeIf { it.isNotBlank() && it != "null" },
        releaseDate = optString("release_date"),
        voteAverage = optDouble("vote_average"),
        backdropPath = optString("backdrop_path").takeIf { it.isNotBlank() && it != "null" }
    )

    private fun getJson(path: String): JSONObject {
        val separator = if ('?' in path) "&" else "?"
        val connection = URL("$path${separator}api_key=$API_KEY")
            .openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Accept", "application/json")

        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("TMDB request failed (${connection.responseCode})")
            }
            return connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
        } finally {
            connection.disconnect()
        }
    }
}
