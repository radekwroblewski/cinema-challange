package com.rdk.cinemachallenge.testutils

import com.rdk.cinemachallenge.dto.*
import com.rdk.cinemachallenge.model.Movie
import com.rdk.cinemachallenge.model.Room
import com.rdk.cinemachallenge.model.Show
import com.rdk.cinemachallenge.testutils.TimeFormatter.toMinuteString

fun mapShow(show: Show): ShowDTO =
    ShowDTO(
        id = show.id.toString(),
        movie = mapMovie(show.movie),
        time = mapTimePeriod(TimePeriod(show.startTime, getEndTime(show.startTime, show.movie)))
    )

fun mapMovie(movie: Movie): MovieDTO =
    MovieDTO(
        id = movie.id.toString(),
        title = movie.title,
        `3d` = movie.requires3D,
        premiere = movie.premiere,
        duration = movie.duration.toMinuteString()
    )

fun mapTimePeriod(timePeriod: TimePeriod): TimePeriodDTO =
    TimePeriodDTO(start = timePeriod.startTime.toString(), end = timePeriod.endTime.toString())

fun mapCleaningPeriod(show: Show): CleaningPeriodDTO =
    getEndTime(show.startTime, show.movie)
        .let {
            TimePeriod(it, it.plus(show.room.cleaningDuration))
        }.let {
            CleaningPeriodDTO(mapTimePeriod(it))
        }

fun mapRoom(room: Room): RoomDTO =
    RoomDTO(id = room.id.toString(), name = room.name, `3dAvailable` = room.avaibable3D)


