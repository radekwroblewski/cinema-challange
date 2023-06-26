package com.rdk.cinemachallenge.controller

import com.rdk.cinemachallenge.dto.ScheduledItemDTO
import com.rdk.cinemachallenge.service.SchedulingService
import com.rdk.cinemachallenge.testutils.TimeFormatter.toDate
import com.rdk.cinemachallenge.testutils.TimeFormatter.toDateTime
import com.rdk.cinemachallenge.validator.ValidDate
import com.rdk.cinemachallenge.validator.ValidDateTime
import com.rdk.cinemachallenge.validator.ValidTime
import com.rdk.cinemachallenge.validator.ValidUUID
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/schedule")
@Validated
class ScheduleController(
    private val schedulingService: SchedulingService
) {

    @GetMapping("/room/{roomId}/day/{date}")
    fun getRoomSchedule(
        @PathVariable @NotBlank @ValidUUID roomId: String,
        @PathVariable @NotNull @ValidDate date: String
    ): List<ScheduledItemDTO> {
        return schedulingService.getRoomShows(
            roomId = UUID.fromString(roomId),
            date = date.toDate()
        )
    }

    //Delete function returns status 200 for non-existant shows ON PURPOSE
    @DeleteMapping("/show/{showId}")
    fun cancelShow(@PathVariable @NotBlank @ValidUUID showId: String) {
        schedulingService.cancelShow(UUID.fromString(showId))
    }

    @PostMapping("/room/{roomId}/movie/{movieId}/startTime/{startTime}")
    fun scheduleShow(
        @PathVariable @NotBlank @ValidUUID roomId: String,
        @PathVariable @NotBlank @ValidUUID movieId: String,
        @PathVariable @ValidDateTime startTime: String
    ) =
        schedulingService.scheduleShow(
            movieId = UUID.fromString(movieId), roomId = UUID.fromString(roomId),
            startTime = startTime.toDateTime()
        )

    @PutMapping("/show/{showId}/startTime/{startTime}")
    fun rescheduleShowTime(
        @PathVariable @NotBlank @ValidUUID showId: String,
        @PathVariable @NotBlank @ValidTime startTime: String
    ) =
        schedulingService.rescheduleShow(
            showId = UUID.fromString(showId),
            newStartTime = startTime.toDateTime()
        )

    @PutMapping("/show/{showId}/roomId/{roomId}")
    fun moveShow(
        @PathVariable @NotBlank @ValidUUID showId: String,
        @PathVariable @NotBlank @ValidUUID roomId: String
    ) =
        schedulingService.moveShow(
            showId = UUID.fromString(showId),
            newRoomId = UUID.fromString(roomId)
        )

    @PutMapping("/show/{showId}/roomId/{roomId}/startTime/{startTime}")
    fun changeShow(
        @PathVariable @NotBlank @ValidUUID showId: String,
        @PathVariable @NotBlank @ValidUUID roomId: String,
        @PathVariable @NotBlank @ValidTime startTime: String
    ) =
        schedulingService.changeShow(
            showId = UUID.fromString(showId),
            newRoomId = UUID.fromString(roomId),
            newStartTime = startTime.toDateTime()
        )
}