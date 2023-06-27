package com.rdk.cinemachallenge.service

import com.rdk.cinemachallenge.dto.RoomDTO
import com.rdk.cinemachallenge.model.Room
import com.rdk.cinemachallenge.model.RoomRepository
import com.rdk.cinemachallenge.testutils.TestObjectProducer.room
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.kotlin.*
import java.time.Duration
import java.util.*

class RoomServiceTest {
    private lateinit var testee: RoomService


    private lateinit var roomRepository: RoomRepository


    @BeforeEach
    fun init() {
        roomRepository = mock()
        testee = RoomService(roomRepository)
    }

    @Test
    fun `test getting all from empty repo`() {
        whenever(roomRepository.getAll()).thenReturn(emptyList())

        val result = testee.getAllRooms()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getting rooms`() {
        whenever(roomRepository.getAll()).thenReturn((1..10).map { room(it) })

        val result = testee.getAllRooms()

        assertEquals(10, result.size)
    }

    @Test
    fun `test getting single`() {
        val room = room(20)
        whenever(roomRepository.getOne(room.id)).thenReturn(room)

        val result: RoomDTO = testee.getRoom(room.id)

        assertEquals(room.name, result.name)
        assertEquals(room.avaibable3D, result.`3dAvailable`)
    }

    @Test
    fun `test add room`() {
        var roomAdded: Room? = null
        whenever(roomRepository.add(any())).thenAnswer {
            roomAdded = it.arguments[0] as? Room
            roomAdded?.id
        }

        val result = testee.addRoom("New and awesome room", true, Duration.ofMinutes(10L))

        assertNotNull(roomAdded)
        assertEquals(roomAdded?.id?.toString(), result)
        assertEquals("New and awesome room", roomAdded?.name)
        assertTrue(roomAdded?.avaibable3D ?: false)
        assertEquals("PT10M", roomAdded?.cleaningDuration?.toString())
        verify(roomRepository).add(eq(roomAdded!!))
    }

    @Test
    fun `test delete room`() {
        val roomId = UUID.randomUUID()
        whenever(roomRepository.remove(any())).thenAnswer {
            if (it.arguments[0] != roomId) {
                fail("Not expected here")
            }
        }

        testee.deleteRoom(roomId)
        verify(roomRepository).remove(eq(roomId))

    }


}