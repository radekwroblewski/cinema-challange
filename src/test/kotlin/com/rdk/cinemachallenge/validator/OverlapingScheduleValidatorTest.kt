package com.rdk.cinemachallenge.validator

import com.rdk.cinemachallenge.model.Show
import com.rdk.cinemachallenge.model.ShowRepository
import com.rdk.cinemachallenge.testutils.TestObjectProducer.movie
import com.rdk.cinemachallenge.testutils.TestObjectProducer.room
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.LocalDate

class OverlapingScheduleValidatorTest {

    private lateinit var testee: OverlapingScheduleValidator
    private lateinit var showRepository: ShowRepository

    private val room = room(cleaningDuration = Duration.ofMinutes(10L))
    private val movie = movie(duration = Duration.ofMinutes(50L))
    private val today = LocalDate.now()
    private val showTime = today.atTime(18, 0)

    @BeforeEach
    fun init() {
        showRepository = mock()
        testee = OverlapingScheduleValidator(showRepository)
    }

    @Test
    fun `test ok on empty`() {

        whenever(showRepository.getShows(room.id, today)).thenReturn(emptyList())

        val result = testee.validate(movie, room, showTime)


        assertNull(result)
        verify(showRepository).getShows(room.id, today)

    }

    @Test
    fun `test ok on adjustant`() {

        whenever(showRepository.getShows(room.id, today)).thenReturn(
            listOf(
                Show(room, movie, showTime.minusMinutes(60L)),
                Show(room, movie, showTime.plusMinutes(60L))
            )
        )

        val result = testee.validate(movie, room, showTime)


        assertNull(result)
        verify(showRepository).getShows(room.id, today)

    }

    @Test
    fun `test not ok on start overlapping`() {

        whenever(showRepository.getShows(room.id, today)).thenReturn(
            listOf(
                Show(room, movie, showTime.minusMinutes(59L)),
                Show(room, movie, showTime.plusMinutes(60L))
            )
        )

        val result = testee.validate(movie, room, showTime)

        assertNotNull(result)
        verify(showRepository).getShows(room.id, today)

    }

    @Test
    fun `test not ok on end overlapping`() {

        whenever(showRepository.getShows(room.id, today)).thenReturn(
            listOf(
                Show(room, movie, showTime.minusMinutes(60L)),
                Show(room, movie, showTime.plusMinutes(59L))
            )
        )

        val result = testee.validate(movie, room, showTime)

        assertNotNull(result)
        verify(showRepository).getShows(room.id, today)

    }

    @Test
    fun `test not ok on containing`() {

        whenever(showRepository.getShows(room.id, today)).thenReturn(
            listOf(
                Show(room, movie(duration = Duration.ofMinutes(10)), showTime.plusMinutes(10L))

            )
        )

        val result = testee.validate(movie, room, showTime)

        assertNotNull(result)
        verify(showRepository).getShows(room.id, today)

    }


}