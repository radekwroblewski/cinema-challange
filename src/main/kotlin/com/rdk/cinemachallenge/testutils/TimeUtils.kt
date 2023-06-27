package com.rdk.cinemachallenge.testutils

import com.rdk.cinemachallenge.model.Movie
import com.rdk.cinemachallenge.model.Room
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun getEndTime(startTime: LocalDateTime, movie: Movie, room: Room) =
    startTime.plus(movie.duration).plus(room.cleaningDuration)

fun getEndTime(startTime: LocalDateTime, movie: Movie) =
    startTime.plus(movie.duration)

fun LocalDateTime.isBeforeOrEq(other: LocalDateTime) = this.isBefore(other) || this.isEqual(other)
fun LocalDateTime.isAfterOrEq(other: LocalDateTime) = this.isAfter(other) || this.isEqual(other)


data class TimePeriod(val startTime: LocalDateTime, val endTime: LocalDateTime) {
    fun contains(another: TimePeriod) =
        startTime.isBeforeOrEq(another.startTime) && endTime.isAfterOrEq(another.endTime)

    fun overlaps(another: TimePeriod) =
        contains(another) ||
                (startTime.isBefore(another.endTime) && startTime.isAfterOrEq(another.startTime)) ||
                (endTime.isAfter(another.startTime) && endTime.isBeforeOrEq(another.endTime))
}


object TimeFormatter {
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME


    fun String.toTime() = LocalTime.parse(this, TIME_FORMATTER)!!
    fun String.toDate() = LocalDate.parse(this, DATE_FORMATTER)!!
    fun String.toDateTime() = LocalDateTime.parse(this, DATE_TIME_FORMATTER)!!
    fun String.toDuration() = Duration.parse(this)!!
    fun Duration.toMinuteString() = "PT${this.toMinutes()}M"
}