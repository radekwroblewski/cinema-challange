package com.rdk.cinemachallenge.validator

import com.rdk.cinemachallenge.model.Movie
import com.rdk.cinemachallenge.model.Room
import com.rdk.cinemachallenge.model.Show
import com.rdk.cinemachallenge.model.ShowRepository
import com.rdk.cinemachallenge.testutils.*
import com.rdk.cinemachallenge.testutils.TimeFormatter.toTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalTime

interface ScheduleValidator {
    fun validate(movie: Movie, room: Room, startTime: LocalDateTime): String?

}

private fun Show.toTimePeriod() =
    TimePeriod(startTime, getEndTime(startTime, movie, room))

@Component
class OverlapingScheduleValidator(private val showRepository: ShowRepository) : ScheduleValidator {
    override fun validate(movie: Movie, room: Room, startTime: LocalDateTime): String? {

        val planned = TimePeriod(startTime, getEndTime(startTime, movie, room))
        val dailyShows = showRepository.getShows(room.id, startTime.toLocalDate())

        return dailyShows.map(Show::toTimePeriod).find {
            it.overlaps(planned)
        }?.let { "Planned show overlaps an existing one" }

    }
}

@Component
class Required3DGlassesValidator() : ScheduleValidator {
    override fun validate(movie: Movie, room: Room, startTime: LocalDateTime): String? {
        return if (movie.requires3D && !room.avaibable3D) {
            "Selected room does not have 3D available"
        } else null
    }
}

// I assume that cleaning time may be after the facility opening hours (sad truth)
@Component
class FacilityOpenValidator(
    @Value("\${openHours.from}")
    openingHourParam: String,
    @Value("\${openHours.to}")
    closingHourParam: String
) : ScheduleValidator {

    private val openingHour: LocalTime = openingHourParam.toTime()
    private val closingHour: LocalTime = closingHourParam.toTime()
    override fun validate(movie: Movie, room: Room, startTime: LocalDateTime): String? {
        val facilityOpenPeriod = TimePeriod(
            startTime.toLocalDate().atTime(openingHour),
            startTime.toLocalDate().atTime(closingHour)
        )
        val plannedTime = TimePeriod(startTime, getEndTime(startTime, movie))

        return if (!facilityOpenPeriod.contains(plannedTime)) {
            "Show cannot start before opening time"
        } else null

    }

}

// I assume cleaning time may be after the premiere time
@Component
class PremiereTimeValidator(
    @Value("\${premiereHours.from}")
    premiereFrom: String,
    @Value("\${premiereHours.to}")
    premiereTo: String
) : ScheduleValidator {

    private val premiereSlotStart: LocalTime = premiereFrom.toTime()
    private val premiereSlotEnd: LocalTime = premiereTo.toTime()
    override fun validate(movie: Movie, room: Room, startTime: LocalDateTime): String? {
        if (!movie.premiere) {
            return null
        }

        val premiereSlot = TimePeriod(
            startTime.toLocalDate().atTime(premiereSlotStart),
            startTime.toLocalDate().atTime(premiereSlotEnd)
        )

        val plannedTime = TimePeriod(startTime, getEndTime(startTime, movie))

        return if (!premiereSlot.contains(plannedTime)) {
            "The premiere movies must start within premiere slot"
        } else null
    }

}