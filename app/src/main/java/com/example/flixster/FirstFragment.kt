package com.example.flixster

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flixster.databinding.FragmentFirstBinding
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/** Shows popular and top-rated movies from TMDB. */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val requestId = AtomicInteger()
    private var selectedCategory = MovieCategory.POPULAR
    private val movieAdapter = MovieAdapter(::openMovieDetails)

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
        binding.categoryToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedCategory = if (checkedId == R.id.top_rated_button) {
                    MovieCategory.TOP_RATED
                } else {
                    MovieCategory.POPULAR
                }
                loadMovies()
            }
        }
        loadMovies()
    }

    private fun loadMovies() {
        val thisRequest = requestId.incrementAndGet()
        binding.progressBar.visibility = View.VISIBLE
        binding.errorGroup.visibility = View.GONE
        binding.movieList.visibility = View.GONE

        executor.execute {
            try {
                val movies = TmdbApi.fetchMovies(selectedCategory)
                mainHandler.post {
                    if (_binding == null || thisRequest != requestId.get()) return@post
                    movieAdapter.submitList(movies)
                    binding.progressBar.visibility = View.GONE
                    binding.movieList.visibility = View.VISIBLE
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    if (_binding == null || thisRequest != requestId.get()) return@post
                    binding.progressBar.visibility = View.GONE
                    binding.errorGroup.visibility = View.VISIBLE
                    binding.errorMessage.text = exception.message ?: getString(R.string.load_error)
                }
            }
        }
    }

    private fun openMovieDetails(movie: Movie, poster: View) {
        val intent = Intent(requireContext(), MovieDetailActivity::class.java)
            .putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.id)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            poster,
            getString(R.string.movie_poster_transition)
        )
        startActivity(intent, options.toBundle())
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
}
