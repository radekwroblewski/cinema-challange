package com.rdk.cinemachallenge.controller

import com.rdk.cinemachallenge.service.RoomService
import com.rdk.cinemachallenge.testutils.TimeFormatter.toDuration
import com.rdk.cinemachallenge.validator.ValidDuration
import com.rdk.cinemachallenge.validator.ValidUUID
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/room")
@Validated
class RoomController(
    private val roomService: RoomService
) {

    @GetMapping("/")
    fun getAllRooms() =
        roomService.getAllRooms()

    @GetMapping("/{roomId}")
    fun getRoom(@PathVariable @NotBlank @ValidUUID roomId: String) =
        roomService.getRoom(UUID.fromString(roomId))

    @PostMapping("/")
    fun addRoom(@RequestBody @Valid body: AddRoomDTO) =
        roomService.addRoom(
            name = body.name,
            cleaningDuration = body.cleaningDuration.toDuration(),
            available3d = body.available3d!!
        )

    @DeleteMapping("/{roomId}")
    fun deleteRoom(@PathVariable @NotBlank @ValidUUID roomId: String) =
        roomService.deleteRoom(UUID.fromString(roomId))

    data class AddRoomDTO(
        @field:NotBlank val name: String,
        @field:NotBlank @field:ValidDuration val cleaningDuration: String,
        @field:NotNull val available3d: Boolean?
    )
}
