package com.rdk.cinemachallenge.service

import com.rdk.cinemachallenge.dto.CleaningPeriodDTO
import com.rdk.cinemachallenge.dto.ScheduledItemDTO
import com.rdk.cinemachallenge.dto.ShowDTO
import com.rdk.cinemachallenge.exceptions.ValidationException
import com.rdk.cinemachallenge.model.MovieRepository
import com.rdk.cinemachallenge.model.RoomRepository
import com.rdk.cinemachallenge.model.Show
import com.rdk.cinemachallenge.model.ShowRepository
import com.rdk.cinemachallenge.testutils.TestObjectProducer.movie
import com.rdk.cinemachallenge.testutils.TestObjectProducer.room
import com.rdk.cinemachallenge.testutils.TimeFormatter.toDateTime
import com.rdk.cinemachallenge.validator.ScheduleValidator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Duration
import java.time.LocalDate
import java.util.*


class SchedulingServiceTest {

    private lateinit var movieRepository: MovieRepository
    private lateinit var roomRepository: RoomRepository
    private lateinit var showRepository: ShowRepository
    private lateinit var scheduleValidators: List<ScheduleValidator>

    private lateinit var testee: SchedulingService

    @BeforeEach
    fun init() {
        movieRepository = mock()
        roomRepository = mock()
        showRepository = mock()
        scheduleValidators = listOf(mock(), mock())

        testee = SchedulingService(movieRepository, roomRepository, showRepository, scheduleValidators, 2)
    }

    @Test
    fun `test get room shows`() {
        val room = room()
        val today = LocalDate.now()
        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        val shows = (8..20).reversed().map {
            Show(room, movie(it, Duration.ofMinutes(30)), today.atTime(it, 0))
        }
        whenever(showRepository.getShows(room.id, today)).thenReturn(shows)

        val roomShows: List<ScheduledItemDTO> = testee.getRoomShows(room.id, today)

        assertEquals(26, roomShows.size)

        (0..25).forEach { counter ->
            if (counter % 2 == 0) {
                val show = roomShows[counter] as? ShowDTO
                assertNotNull(show)
                show?.let {
                    assertEquals(today.atTime(8 + (counter / 2), 0).toString(), it.time.start)
                    assertEquals("test movie ${8 + (counter / 2)}", it.movie.title)
                }
            } else {
                val cleaning = roomShows[counter] as? CleaningPeriodDTO
                assertNotNull(cleaning)
                cleaning?.let {
                    val expectedStartTime = roomShows[counter - 1].time.end.toDateTime()
                    assertEquals(
                        expectedStartTime.toString(),
                        it.time.start, "$counter"
                    )
                    assertEquals(expectedStartTime.plus(room.cleaningDuration).toString(), it.time.end)
                }

            }
        }

    }

    @Test
    fun `test getting shows from non-existant room`() {
        val room = room()
        val today = LocalDate.now()
        whenever(roomRepository.getOne(any())).thenThrow(NoSuchElementException("No room"))

        assertThrows(NoSuchElementException::class.java) {
            testee.getRoomShows(room.id, today)
        }
        verify(roomRepository).getOne(room.id)
    }

    @Test
    fun `test scheduling a show`() {
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()
        val showtime = today.atTime(20, 0)

        var show: Show? = null

        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0)
        whenever(showRepository.addShow(any(), eq(0))).thenAnswer {
            show = (it.arguments[0] as? Show)
            show?.id
        }

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        scheduleValidators.forEach {
            whenever(it.validate(any(), any(), any())).thenReturn(null)
        }

        val result = testee.scheduleShow(movieId = movie.id, roomId = room.id, startTime = showtime)

        assertNotNull(show)
        assertEquals(show?.id?.toString(), result)
        verify(roomRepository).getOne(room.id)
        verify(movieRepository).getOne(movie.id)
        verify(showRepository, times(1)).getShowVersion(room.id, today)
        verify(showRepository, times(1)).addShow(show!!, 0)
    }


    @Test
    fun `test scheduling concurent retry 3 times`() {
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()
        val showtime = today.atTime(20, 0)

        var show: Show? = null

        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0, 1, 2)
        whenever(showRepository.addShow(any(), eq(0))).thenThrow(ConcurrentModificationException())
        whenever(showRepository.addShow(any(), eq(1))).thenThrow(ConcurrentModificationException())
        whenever(showRepository.addShow(any(), eq(2))).thenAnswer {
            show = (it.arguments[0] as? Show)
            show?.id
        }

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        scheduleValidators.forEach {
            whenever(it.validate(any(), any(), any())).thenReturn(null)
        }

        val result = testee.scheduleShow(movieId = movie.id, roomId = room.id, startTime = showtime)

        assertNotNull(show)
        assertEquals(show?.id?.toString(), result)
        verify(roomRepository).getOne(room.id)
        verify(movieRepository).getOne(movie.id)
        verify(showRepository, times(3)).getShowVersion(room.id, today)
        verify(showRepository, times(3)).addShow(any(), any())
    }

    @Test
    fun `test scheduling concurent retry 3 times and fail`() {
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()
        val showtime = today.atTime(20, 0)

        var show: Show? = null

        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0, 1, 2, 3, 4, 5, 6)
        whenever(showRepository.addShow(any(), any())).thenThrow(ConcurrentModificationException())
        whenever(showRepository.addShow(any(), eq(3))).then {
            show = it.arguments[0] as Show
            show?.id
        }

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        scheduleValidators.forEach {
            whenever(it.validate(any(), any(), any())).thenReturn(null)
        }

        assertThrows(ConcurrentModificationException::class.java) {
            testee.scheduleShow(movieId = movie.id, roomId = room.id, startTime = showtime)
        }

        assertNull(show)
        verify(roomRepository).getOne(room.id)
        verify(movieRepository).getOne(movie.id)
        verify(showRepository, times(3)).getShowVersion(room.id, today)
        verify(showRepository, times(3)).addShow(any(), any())
    }

    @Test
    fun `test cancelling the show`() {
        val showId = UUID.randomUUID()

        testee.cancelShow(showId)

        verify(showRepository).removeShow(showId)
    }


    @Test
    fun `test changing a show`() {
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()
        val showtime = today.atTime(20, 0)

        var newShow: Show? = null
        val show = Show(room(10), movie, today.atTime(18, 0))

        whenever(showRepository.getShow(show.id)).thenReturn(show)

        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0)
        whenever(showRepository.addShow(any(), eq(0))).thenAnswer {
            newShow = (it.arguments[0] as? Show)
            newShow?.id
        }

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        scheduleValidators.forEach {
            whenever(it.validate(any(), any(), any())).thenReturn(null)
        }

        val result = testee.changeShow(show.id, room.id, showtime)

        assertNotNull(newShow)
        assertEquals(newShow?.id?.toString(), result)
        verify(roomRepository).getOne(room.id)
        verify(showRepository, times(1)).getShowVersion(room.id, today)
        verify(showRepository, times(1)).addShow(newShow!!, 0)
    }
    @Test
    fun `test changing a show room only`() {
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()
        val showtime = today.atTime(20, 0)

        var newShow: Show? = null
        val show = Show(room, movie, today.atTime(18, 0))

        whenever(showRepository.getShow(show.id)).thenReturn(show)

        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0)
        whenever(showRepository.addShow(any(), eq(0))).thenAnswer {
            newShow = (it.arguments[0] as? Show)
            newShow?.id
        }

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        scheduleValidators.forEach {
            whenever(it.validate(any(), any(), any())).thenReturn(null)
        }

        val result = testee.changeShow(showId = show.id, newStartTime = showtime)

        assertNotNull(newShow)
        assertEquals(newShow?.id?.toString(), result)
        verify(showRepository, times(1)).getShowVersion(room.id, today)
        verify(showRepository, times(1)).addShow(newShow!!, 0)
    }

    @Test
    fun `test changing a show time only`() {
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()

        var newShow: Show? = null
        val show = Show(room(10), movie, today.atTime(18, 0))

        whenever(showRepository.getShow(show.id)).thenReturn(show)

        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0)
        whenever(showRepository.addShow(any(), eq(0))).thenAnswer {
            newShow = (it.arguments[0] as? Show)
            newShow?.id
        }

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        scheduleValidators.forEach {
            whenever(it.validate(any(), any(), any())).thenReturn(null)
        }

        val result = testee.changeShow(show.id, room.id)

        assertNotNull(newShow)
        assertEquals(newShow?.id?.toString(), result)
        verify(roomRepository).getOne(room.id)
        verify(showRepository, times(1)).getShowVersion(room.id, today)
        verify(showRepository, times(1)).addShow(newShow!!, 0)
    }


    @Test
    fun `test changing concurent retry 3 times`() {
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()
        val showtime = today.atTime(20, 0)

        val show = Show(room(10), movie, today.atTime(18, 0))
        var newShow: Show? = null

        whenever(showRepository.getShow(show.id)).thenReturn(show)
        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0, 1, 2)
        whenever(showRepository.addShow(any(), any())).thenThrow(ConcurrentModificationException())
        whenever(showRepository.addShow(any(), eq(2))).thenAnswer {
            newShow = (it.arguments[0] as? Show)
            newShow?.id
        }

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        scheduleValidators.forEach {
            whenever(it.validate(any(), any(), any())).thenReturn(null)
        }

        val result = testee.changeShow(show.id, room.id, showtime)

        assertNotNull(newShow)
        assertEquals(newShow?.id?.toString(), result)
        verify(showRepository, times(3)).getShowVersion(room.id, today)
        verify(showRepository, times(3)).addShow(any(), any())
    }

    @Test
    fun `test changing concurent retry 3 times and fail`() {
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()
        val showtime = today.atTime(20, 0)

        val show = Show(room(10), movie, today.atTime(18, 0))
        var newShow: Show? = null

        whenever(showRepository.getShow(show.id)).thenReturn(show)
        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0, 1, 2, 3, 4, 5, 6)
        whenever(showRepository.addShow(any(), any())).thenThrow(ConcurrentModificationException())
        whenever(showRepository.addShow(any(), eq(3))).then {
            newShow = it.arguments[0] as Show
            newShow?.id
        }

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        scheduleValidators.forEach {
            whenever(it.validate(any(), any(), any())).thenReturn(null)
        }

        assertThrows(ConcurrentModificationException::class.java) {
            testee.changeShow(show.id, room.id, showtime)
        }

        assertNull(newShow)
        verify(showRepository, times(3)).getShowVersion(room.id, today)
        verify(showRepository, times(3)).addShow(any(), any())
    }

    @Test
    fun `test validation exception`() {
        (scheduleValidators.indices).forEach {
            whenever(scheduleValidators[it].validate(any(), any(), any())).thenReturn("error $it")
        }
        val room = room()
        val movie = movie(5, Duration.ofMinutes(60L))
        val today = LocalDate.now()
        val showtime = today.atTime(20, 0)

        whenever(showRepository.getShowVersion(room.id, today)).thenReturn(0)

        whenever(roomRepository.getOne(room.id)).thenReturn(room)
        whenever(movieRepository.getOne(movie.id)).thenReturn(movie)

        assertThrows(ValidationException::class.java) {
            try {
                testee.scheduleShow(movie.id, room.id, showtime)
            } catch (ex: ValidationException) {
                assertEquals(listOf("error 0", "error 1"), ex.errors)
                throw ex;
            }
        }
    }

}