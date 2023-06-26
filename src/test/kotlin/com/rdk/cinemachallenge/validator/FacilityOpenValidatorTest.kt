package com.rdk.cinemachallenge.validator

import com.rdk.cinemachallenge.testutils.TestObjectProducer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class FacilityOpenValidatorTest {
    private val testee = FacilityOpenValidator("08:00", "22:00")

    private val room = TestObjectProducer.room(cleaningDuration = Duration.ofMinutes(10L))
    private val movie = TestObjectProducer.movie(duration = Duration.ofMinutes(50L))

    private val today = LocalDate.now()

    @Test
    fun `test middle of day`() {
        val showtime = today.atTime(12, 0)

        val result = testee.validate(movie, room, showtime)

        assertNull(result)
    }

    @Test
    fun `test end of day`() {
        val showtime = today.atTime(21, 0)

        val result = testee.validate(movie, room, showtime)

        assertNull(result)
    }

    @Test
    fun `test end of day cleaning overlaps`() {
        val showtime = today.atTime(21, 10)

        val result = testee.validate(movie, room, showtime)

        assertNull(result)
    }

    @Test
    fun `test end of day  overlaps`() {
        val showtime = today.atTime(21, 11)

        val result = testee.validate(movie, room, showtime)

        assertNotNull(result)
    }

    @Test
    fun `test start of day`() {
        val showtime = today.atTime(8, 0)

        val result = testee.validate(movie, room, showtime)

        assertNull(result)
    }

    @Test
    fun `test start of day overlaps`() {
        val showtime = today.atTime(7, 59)

        val result = testee.validate(movie, room, showtime)

        assertNotNull(result)
    }

    @Test
    fun `test start of day before`() {
        val showtime = today.atTime(6, 0)

        val result = testee.validate(movie, room, showtime)

        assertNotNull(result)
    }
    @Test
    fun `test end of day after`() {
        val showtime = today.atTime(23, 0)

        val result = testee.validate(movie, room, showtime)

        assertNotNull(result)
    }


}