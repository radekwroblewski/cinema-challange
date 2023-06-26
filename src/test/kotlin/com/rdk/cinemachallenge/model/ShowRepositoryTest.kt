package com.rdk.cinemachallenge.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.NoSuchElementException

class ShowRepositoryTest {

    private lateinit var testee: ShowRepository

    private val room = Room("Test room", Duration.ofMinutes(15L), true)
    private val movie =
        Movie("Long History of Unit Tests", Duration.ofMinutes(120L), premiere = true, requires3D = false)
    private val today = LocalDate.now()

    @BeforeEach
    fun init() {
        testee = ShowRepository()
    }

    @Test
    fun `test getting shows from empty`() {
        val shows = testee.getShows(room.id, today)

        assertTrue(shows.isEmpty())
    }

    @Test
    fun `test adding a show`() {
        val show = Show(room, movie, today.atTime(8, 0))

        val showId = testee.addShow(show, 0)

        assertEquals(show.id, showId)
        val todaysShows = testee.getShows(room.id, today)

        assertEquals(1, todaysShows.size)
        assertTrue(todaysShows.contains(show))
    }

    @Test
    fun `test adding 5 shows`() {
        val shows =
            (0..4).map {
                val show = Show(room, movie, today.atTime(8 + (2 * it), 0))
                testee.addShow(show, it)
                show
            }

        val todaysShows = testee.getShows(room.id, today)

        assertEquals(shows, todaysShows)
    }

    @Test
    fun `test adding 2 shows on same room same day simultainiously`() {
        val show1 = Show(room, movie, today.atTime(8, 0))
        val show2 = Show(room, movie, today.atTime(11, 0))

        var show1Version = testee.getShowVersion(show1.room.id, show1.startTime.toLocalDate())
        var show2Version = testee.getShowVersion(show2.room.id, show2.startTime.toLocalDate())


        assertThrows(ConcurrentModificationException::class.java) {
            testee.addShow(show1, show1Version)
            testee.addShow(show2, show2Version)
        }

        assertEquals(show1Version, show2Version)
    }

    @Test
    fun `test adding 2 shows on other days simultainiously`() {
        val show1 = Show(room, movie, today.atTime(8, 0))
        val show2 = Show(room, movie, today.plusDays(1).atTime(11, 0))

        var show1Version = testee.getShowVersion(show1.room.id, show1.startTime.toLocalDate())
        var show2Version = testee.getShowVersion(show2.room.id, show2.startTime.toLocalDate())

        assertDoesNotThrow {
            testee.addShow(show1, show1Version)
            testee.addShow(show2, show2Version)
        }

        assertNotEquals(show1Version, testee.getShowVersion(show1.room.id, show1.startTime.toLocalDate()))
        assertNotEquals(show2Version, testee.getShowVersion(show2.room.id, show2.startTime.toLocalDate()))
    }

    @Test
    fun `test adding 2 shows on other rooms simultainiously`() {
        val show1 = Show(room, movie, today.atTime(8, 0))
        val show2 = Show(Room("Other room", Duration.ofMinutes(10L), false), movie, today.atTime(8, 0))

        var show1Version = testee.getShowVersion(show1.room.id, show1.startTime.toLocalDate())
        var show2Version = testee.getShowVersion(show2.room.id, show2.startTime.toLocalDate())

        assertDoesNotThrow {
            testee.addShow(show1, show1Version)
            testee.addShow(show2, show2Version)
        }
        assertNotEquals(show1Version, testee.getShowVersion(show1.room.id, show1.startTime.toLocalDate()))
        assertNotEquals(show2Version, testee.getShowVersion(show2.room.id, show2.startTime.toLocalDate()))
    }

    @Test
    fun `test remove show from empty`() {
        val showId = UUID.randomUUID()
        testee.removeShow(showId)

        assertThrows(NoSuchElementException::class.java) {
            testee.getShow(showId)
        }
    }

    @Test
    fun `test add and remove show`() {
        val show = Show(room, movie, today.atTime(8, 0))
        val showId = testee.addShow(show, 0)

        testee.removeShow(showId)
        val todaysShows = testee.getShows(room.id, today)

        assertTrue(todaysShows.isEmpty())
    }
}