package com.rdk.cinemachallenge.model

import com.rdk.cinemachallenge.testutils.TestObjectProducer.movie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class MovieRepositoryTest {

    private lateinit var testee: MovieRepository

    @BeforeEach
    fun init() {
        testee = MovieRepository()
    }

    @Test
    fun `test add movie`() {
        assertTrue(testee.getAll().isEmpty())

        val movie = movie()
        testee.add(movie)

        assertTrue(testee.getAll().isNotEmpty())
        assertEquals(1, testee.getAll().size)
        assertEquals(movie, testee.getOne(movie.id))


    }

    @Test
    fun `test add 10 movies`() {
        assertTrue(testee.getAll().isEmpty())


        for (i in 1..10) {
            testee.add(movie(i))
        }

        assertTrue(testee.getAll().isNotEmpty())
        assertEquals(10, testee.getAll().size)


    }

    @Test
    fun `test delete from empty testee`() {
        assertTrue(testee.getAll().isEmpty())

        testee.remove(UUID.randomUUID())

        assertTrue(testee.getAll().isEmpty())


    }

    @Test
    fun `test delete non existent`() {
        assertTrue(testee.getAll().isEmpty())
        (1..10).forEach {
            testee.add(movie(it))
        }

        testee.remove(UUID.randomUUID())

        assertEquals(10, testee.getAll().size)
    }

    @Test
    fun `test add and delete all`() {
        assertTrue(testee.getAll().isEmpty())

        val movies = (1..10).map { movie(it) }

        movies.forEach(testee::add)

        assertEquals(10, testee.getAll().size)

        movies.forEach { testee.remove(it.id) }


        assertTrue(testee.getAll().isEmpty())

    }

    @Test
    fun `test add and delete half`() {
        assertTrue(testee.getAll().isEmpty())

        val movies = (1..10).map { movie(it) }

        movies.forEach(testee::add)

        assertEquals(10, testee.getAll().size)

        val toDelete = movies.subList(0, 5)

        toDelete.forEach { testee.remove(it.id) }


        assertEquals(5, testee.getAll().size)


    }


}