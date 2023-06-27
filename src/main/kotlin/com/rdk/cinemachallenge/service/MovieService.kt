package com.rdk.cinemachallenge.service

import com.rdk.cinemachallenge.dto.MovieDTO
import com.rdk.cinemachallenge.model.Movie
import com.rdk.cinemachallenge.model.MovieRepository
import com.rdk.cinemachallenge.testutils.mapMovie
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class MovieService(
    private val movieRepository: MovieRepository
) {
    fun getAllMovies(): List<MovieDTO> = movieRepository.getAll().map(::mapMovie)

    fun getMovie(movieId: UUID): MovieDTO = mapMovie(movieRepository.getOne(movieId))
    fun addMovie(title: String, duration: Duration, requires3d: Boolean, premiere: Boolean): String =
        movieRepository.add(
            Movie(
                title = title,
                duration = duration,
                premiere = premiere,
                requires3D = requires3d
            )
        ).toString()

    fun deleteMovie(movieId: UUID) {
        movieRepository.remove(movieId)
    }

}
