package com.example.flixster

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.flixster.databinding.MovieItemBinding

class MovieAdapter(
    private val onMovieClick: (Movie, android.view.View) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    private val movies = mutableListOf<Movie>()

    fun submitList(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = MovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding, onMovieClick)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    class MovieViewHolder(
        private val binding: MovieItemBinding,
        private val onMovieClick: (Movie, android.view.View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.movieTitle.text = movie.title
            binding.movieOverview.text = movie.overview
            binding.movieReleaseDate.text = if (movie.releaseDate.isBlank()) {
                binding.root.context.getString(R.string.release_date_unknown)
            } else {
                binding.root.context.getString(R.string.release_date, movie.releaseDate)
            }
            binding.movieRating.text = binding.root.context.getString(
                R.string.movie_rating,
                movie.voteAverage
            )
            binding.root.setOnClickListener { onMovieClick(movie, binding.moviePoster) }
            binding.moviePoster.transitionName = binding.root.context.getString(R.string.movie_poster_transition)

            val radius = (12 * binding.root.resources.displayMetrics.density).toInt()
            Glide.with(binding.moviePoster)
                .load(movie.posterUrl)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(radius)))
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .centerCrop()
                .into(binding.moviePoster)
        }
    }
}
