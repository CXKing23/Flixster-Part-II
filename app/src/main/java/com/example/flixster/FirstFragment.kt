package com.example.flixster

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flixster.databinding.FragmentFirstBinding
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import org.json.JSONObject

/** Displays the movies currently playing in theaters. */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val movieAdapter = MovieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
        binding.movieList.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.movieList.adapter = movieAdapter
        binding.retryButton.setOnClickListener { loadMovies() }
        loadMovies()
    }

    private fun loadMovies() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorGroup.visibility = View.GONE
        binding.movieList.visibility = View.GONE

        executor.execute {
            try {
                val movies = fetchNowPlayingMovies()
                mainHandler.post {
                    if (_binding == null) return@post
                    movieAdapter.submitList(movies)
                    binding.progressBar.visibility = View.GONE
                    binding.movieList.visibility = View.VISIBLE
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    if (_binding == null) return@post
                    binding.progressBar.visibility = View.GONE
                    binding.errorGroup.visibility = View.VISIBLE
                    binding.errorMessage.text = exception.message ?: getString(R.string.load_error)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun fetchNowPlayingMovies(): List<Movie> {
        val endpoint = URL(
            "https://api.themoviedb.org/3/movie/now_playing" +
                "?api_key=$TMDB_API_KEY&language=en-US&page=1"
        )
        val connection = endpoint.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Accept", "application/json")

        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("TMDB request failed (${connection.responseCode})")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val results = JSONObject(response).getJSONArray("results")
            return buildList(results.length()) {
                for (index in 0 until results.length()) {
                    val movie = results.getJSONObject(index)
                    add(
                        Movie(
                            title = movie.optString("title", getString(R.string.untitled_movie)),
                            overview = movie.optString("overview", getString(R.string.no_overview)),
                            posterPath = movie.optString("poster_path").takeIf { it.isNotBlank() },
                            releaseDate = movie.optString("release_date")
                        )
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    override fun onDestroyView() {
        mainHandler.removeCallbacksAndMessages(null)
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private companion object {
        const val TMDB_API_KEY = "a07e22bc18f5cb106bfe4cc1f83ad8ed"
    }
}
