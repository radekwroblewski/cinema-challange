package com.rdk.cinemachallenge.controller

import com.rdk.cinemachallenge.service.MovieService
import com.rdk.cinemachallenge.testutils.TimeFormatter.toDuration
import com.rdk.cinemachallenge.validator.ValidDuration
import com.rdk.cinemachallenge.validator.ValidUUID
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/movie")
class MovieController(
    private val movieService: MovieService
) {
    @GetMapping("/")
    fun getAllMovies() =
        movieService.getAllMovies()

    @GetMapping("/{movieId}")
    fun getMovieDetails(@PathVariable @NotBlank @ValidUUID movieId: String) =
        movieService.getMovie(UUID.fromString(movieId))

    @PostMapping("/")
    fun addMovie(@RequestBody @Valid body: AddMovieDTO) =
        movieService.addMovie(
            title = body.title,
            duration = body.duration.toDuration(),
            requires3d = body.requires3d!!,
            premiere = body.premiere!!
        )

    @DeleteMapping("/{roomId}")
    fun deleteRoom(@PathVariable @NotBlank @ValidUUID roomId: String) =
        movieService.deleteMovie(UUID.fromString(roomId))

    data class AddMovieDTO(
        @field:NotBlank val title: String,
        @field:NotBlank @field:ValidDuration val duration: String,
        @field:NotNull val requires3d: Boolean?,
        @field:NotNull val premiere: Boolean?
    )
}