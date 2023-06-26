package com.rdk.cinemachallenge.service

import com.rdk.cinemachallenge.dto.RoomDTO
import com.rdk.cinemachallenge.model.Room
import com.rdk.cinemachallenge.model.RoomRepository
import com.rdk.cinemachallenge.testutils.mapRoom
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class RoomService(
    private val roomRepository: RoomRepository
) {

    fun getAllRooms(): List<RoomDTO> =
        roomRepository.getAll().map(::mapRoom)

    fun getRoom(roomId: UUID): RoomDTO = mapRoom(roomRepository.getOne(roomId))

    fun addRoom(name: String, available3d: Boolean, cleaningDuration: Duration): String =
        Room(
            name = name,
            cleaningDuration = cleaningDuration,
            avaibable3D = available3d
        ).also { roomRepository.add(it) }.id.toString()

    fun deleteRoom(roomId: UUID) {
        roomRepository.remove(roomId)
    }

}
