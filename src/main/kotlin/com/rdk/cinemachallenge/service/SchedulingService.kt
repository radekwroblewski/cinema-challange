package com.rdk.cinemachallenge.service

import com.rdk.cinemachallenge.dto.ScheduledItemDTO
import com.rdk.cinemachallenge.exceptions.ValidationException
import com.rdk.cinemachallenge.model.*
import com.rdk.cinemachallenge.testutils.mapCleaningPeriod
import com.rdk.cinemachallenge.testutils.mapShow
import com.rdk.cinemachallenge.validator.ScheduleValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class SchedulingService(
    private val movieRepository: MovieRepository,
    private val roomRepository: RoomRepository,
    private val showRepository: ShowRepository,
    private val scheduleValidators: List<ScheduleValidator>,
    @Value("\${numberOfFailedRetries}") private val numberOfFailedRetries: Int
) {

    fun getRoomShows(roomId: UUID, date: LocalDate): List<ScheduledItemDTO> =
        roomRepository.getOne(roomId).let {//to get 404 if non-existing room
            showRepository.getShows(roomId, date).sortedBy { it.startTime }.flatMap {
                listOf(mapShow(it), mapCleaningPeriod(it))
            }
        }

    fun scheduleShow(movieId: UUID, roomId: UUID, startTime: LocalDateTime): String {
        val room = roomRepository.getOne(roomId)
        val movie = movieRepository.getOne(movieId)
        return schedule(startTime, movie, room, 0).toString()
    }

    private fun schedule(
        startTime: LocalDateTime,
        movie: Movie,
        room: Room,
        tryCounter: Int
    ): UUID {
        try {
            val scheduleVersion = showRepository.getShowVersion(room.id, startTime.toLocalDate())

            val errors = scheduleValidators.mapNotNull { it.validate(movie, room, startTime) }
            if (errors.isNotEmpty()) throw ValidationException(errors)

            return showRepository.addShow(Show(room, movie, startTime), scheduleVersion)
        } catch (ex: ConcurrentModificationException) {
            if (tryCounter < numberOfFailedRetries) {
                return schedule(startTime, movie, room, tryCounter + 1)
            }
            throw ex
        }
    }

    fun cancelShow(showId: UUID) {
        showRepository.removeShow(showId)
    }

    fun changeShow(showId: UUID, newRoomId: UUID? = null, newStartTime: LocalDateTime? = null): String {
        val show = showRepository.getShow(showId)
        val startTime = newStartTime ?: show.startTime
        val room = newRoomId?.let { roomRepository.getOne(it) } ?: show.room

        return schedule(startTime, show.movie, room, 0).toString().also {
            showRepository.removeShow(showId)
        }

    }


}