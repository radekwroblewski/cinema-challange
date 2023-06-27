package com.rdk.cinemachallenge.validator

import com.rdk.cinemachallenge.testutils.TestObjectProducer.movie
import com.rdk.cinemachallenge.testutils.TestObjectProducer.room
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class Required3DGlassesValidatorTest {

    private val testee = Required3DGlassesValidator()

    private val today = LocalDate.now()
    private val showTime = today.atTime(18, 0)

    @Test
    fun `test movie 3d room 3d`() {
        val room = room(avaibable3D = true)
        val movie = movie(requires3D = true)

        val result = testee.validate(movie, room, showTime)

        assertNull(result)
    }

    @Test
    fun `test movie 3d room not 3d`() {
        val room = room(avaibable3D = false)
        val movie = movie(requires3D = true)

        val result = testee.validate(movie, room, showTime)

        assertNotNull(result)
    }

    @Test
    fun `test movie not 3d room 3d`() {
        val room = room(avaibable3D = true)
        val movie = movie(requires3D = false)

        val result = testee.validate(movie, room, showTime)

        assertNull(result)
    }

    @Test
    fun `test movie not 3d room not 3d`() {
        val room = room(avaibable3D = false)
        val movie = movie(requires3D = false)

        val result = testee.validate(movie, room, showTime)

        assertNull(result)
    }


}