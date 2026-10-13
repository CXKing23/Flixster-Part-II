package com.example.flixster

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.flixster.databinding.ActivityMovieDetailBinding
import java.util.concurrent.Executors

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.detailToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.movie_details)
        binding.detailToolbar.setNavigationOnClickListener { finishAfterTransition() }
        binding.detailRetryButton.setOnClickListener { loadMovieDetails() }
        binding.detailMovieBackdrop.transitionName = getString(R.string.movie_poster_transition)

        if (intent.getIntExtra(EXTRA_MOVIE_ID, -1) <= 0) {
            finish()
            return
        }
        loadMovieDetails()
    }

    private fun loadMovieDetails() {
        val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, -1)
        binding.detailProgress.visibility = View.VISIBLE
        binding.detailErrorGroup.visibility = View.GONE
        binding.detailScroll.visibility = View.VISIBLE

        executor.execute {
            try {
                val movie = TmdbApi.fetchMovieDetails(movieId)
                mainHandler.post {
                    if (isFinishing || isDestroyed) return@post
                    showMovie(movie)
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    if (isFinishing || isDestroyed) return@post
                    binding.detailProgress.visibility = View.GONE
                    binding.detailScroll.visibility = View.GONE
                    binding.detailErrorGroup.visibility = View.VISIBLE
                    binding.detailErrorMessage.text = exception.message ?: getString(R.string.load_error)
                }
            }
        }
    }

    private fun showMovie(movie: Movie) {
        binding.detailMovieTitle.text = movie.title
        binding.detailMovieTagline.text = movie.tagline
        binding.detailMovieTagline.visibility = if (movie.tagline.isBlank()) View.GONE else View.VISIBLE

        val release = movie.releaseDate.ifBlank { getString(R.string.release_date_unknown) }
        val runtime = movie.runtimeMinutes?.let { getString(R.string.runtime_minutes, it) }
            ?: getString(R.string.runtime_unknown)
        binding.detailMovieMeta.text = getString(
            R.string.movie_detail_meta,
            release,
            movie.voteAverage,
            runtime
        )
        binding.detailMovieGenres.text = movie.genres.joinToString(" • ")
        binding.detailMovieGenres.visibility = if (movie.genres.isEmpty()) View.GONE else View.VISIBLE
        binding.detailMovieOverview.text = movie.overview

        val radius = (18 * resources.displayMetrics.density).toInt()
        Glide.with(binding.detailMovieBackdrop)
            .load(movie.backdropUrl ?: movie.posterUrl)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(radius)))
            .placeholder(R.drawable.ic_movie_placeholder)
            .error(R.drawable.ic_movie_placeholder)
            .centerCrop()
            .into(binding.detailMovieBackdrop)

        binding.detailProgress.visibility = View.GONE
        binding.detailScroll.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        finishAfterTransition()
        return true
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        executor.shutdownNow()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_MOVIE_ID = "com.example.flixster.EXTRA_MOVIE_ID"
    }
}
