package com.rdk.cinemachallenge.service

import com.rdk.cinemachallenge.dto.MovieDTO
import com.rdk.cinemachallenge.model.Movie
import com.rdk.cinemachallenge.model.MovieRepository
import com.rdk.cinemachallenge.testutils.TestObjectProducer.movie
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.kotlin.*
import java.rmi.UnexpectedException
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

class MovieServiceTest {
    private lateinit var testee: MovieService


    private lateinit var movieRepository: MovieRepository


    @BeforeEach
    fun init() {
        movieRepository = mock()
        testee = MovieService(movieRepository)
    }

    @Test
    fun `test getting all from empty repo`() {
        whenever(movieRepository.getAll()).thenReturn(emptyList())

        val result = testee.getAllMovies()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getting movies`() {
        whenever(movieRepository.getAll()).thenReturn((1..10).map { movie(it) })

        val result = testee.getAllMovies()

        assertEquals(10, result.size)
    }

    @Test
    fun `test getting single`() {
        val movie = movie(20)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        val result: MovieDTO = testee.getMovie(movie.id)

        assertEquals(movie.title, result.title)
        assertEquals(movie.requires3D, result.`3d`)
        assertEquals("PT210M", result.duration)
        assertEquals(movie.premiere, result.premiere)
    }

    @Test
    fun `test add movie`() {
        var movieAdded: Movie? = null
        whenever(movieRepository.add(any())).thenAnswer {
            movieAdded = it.arguments[0] as? Movie
            movieAdded?.id
        }

        val result = testee.addMovie("New and awesome movie", Duration.ofMinutes(90L), true, premiere = true)

        assertNotNull(movieAdded)
        assertEquals(movieAdded?.id?.toString(), result)
        assertEquals("New and awesome movie", movieAdded?.title)
        assertTrue(movieAdded?.requires3D ?: false)
        assertTrue(movieAdded?.premiere ?: false)
        assertEquals("PT1H30M", movieAdded?.duration?.toString())
        verify(movieRepository).add(eq(movieAdded!!))
    }

    @Test
    fun `test delete movie`() {
        val movieId = UUID.randomUUID()
        whenever(movieRepository.remove(any())).thenAnswer {
            if (it.arguments[0]!=movieId) {
                fail("Not expected here")
            }
        }

        testee.deleteMovie(movieId)
        verify(movieRepository).remove(eq(movieId))

    }




}