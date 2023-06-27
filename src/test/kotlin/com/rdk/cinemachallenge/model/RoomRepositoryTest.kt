package com.rdk.cinemachallenge.model

import com.rdk.cinemachallenge.testutils.TestObjectProducer.room
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class RoomRepositoryTest {

    private lateinit var testee: RoomRepository

    @BeforeEach
    fun init() {
        testee = RoomRepository()
    }

    @Test
    fun `test add room`() {
        assertTrue(testee.getAll().isEmpty())

        val room = room()
        testee.add(room)

        assertTrue(testee.getAll().isNotEmpty())
        assertEquals(1, testee.getAll().size)
        assertEquals(room, testee.getOne(room.id))
    }

    @Test
    fun `test add 10 rooms`() {
        assertTrue(testee.getAll().isEmpty())

        for (i in 1..10) {
            testee.add(room())
        }

        assertTrue(testee.getAll().isNotEmpty())
        assertEquals(10, testee.getAll().size)

    }

    @Test
    fun `test delete from empty testee`() {
        assertTrue(testee.getAll().isEmpty())

        testee.remove(UUID.randomUUID())

        assertTrue(testee.getAll().isEmpty())
    }

    @Test
    fun `test delete non existent`() {
        assertTrue(testee.getAll().isEmpty())
        (1..10).forEach {
            testee.add(room(it))
        }

        testee.remove(UUID.randomUUID())


        assertEquals(10, testee.getAll().size)
    }

    @Test
    fun `test add and delete all`() {
        assertTrue(testee.getAll().isEmpty())

        val rooms = (1..10).map { room(it) }

        rooms.forEach(testee::add)

        assertEquals(10, testee.getAll().size)

        rooms.forEach { testee.remove(it.id) }


        assertTrue(testee.getAll().isEmpty())
    }

    @Test
    fun `test add and delete half`() {
        assertTrue(testee.getAll().isEmpty())

        val rooms = (1..10).map { room(it) }

        rooms.forEach(testee::add)

        assertEquals(10, testee.getAll().size)

        val toDelete = rooms.subList(0, 5)

        toDelete.forEach { testee.remove(it.id) }


        assertEquals(5, testee.getAll().size)
    }


}