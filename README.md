# Flixster — Project 4, Part II

Submitted by: **Christian King**

Flixster lets you browse popular and top-rated movies, then open a movie to see its artwork, rating, genres, release date, runtime, and overview.

Time spent: **4 hours**

## Required features

- [x] Choose a TMDB endpoint other than `now_playing`
  - Chosen endpoint: `movie/popular`
- [x] Request the selected endpoint and display results in a RecyclerView
- [x] Use Glide to display a poster for each movie
- [x] Open movie details in a separate Activity using an Intent

## Optional features

- [x] Add a second endpoint and let users switch between Popular and Top Rated movies (`movie/top_rated`)
- [x] Round poster images with Glide transformations
- [x] Add a shared-element transition from the movie poster to the detail screen

## Additional features

- Full movie detail data includes runtime, genres, rating, release date, and overview.
- Loading indicators, retry actions, and readable error states are included for network requests.
- The movie list adapts to portrait and landscape orientations.

## Video walkthrough

**TODO:** Add a walkthrough GIF and replace this note with its repository path.

## Notes

Movie list and detail requests run off the main thread. The detail screen receives a movie ID through the Intent and requests the full record from TMDB.

## License

Copyright 2026 [Your name]

Licensed under the Apache License, Version 2.0. You may obtain a copy at <https://www.apache.org/licenses/LICENSE-2.0>.
