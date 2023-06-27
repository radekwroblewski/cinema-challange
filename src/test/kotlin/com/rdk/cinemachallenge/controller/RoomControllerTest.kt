package com.rdk.cinemachallenge.controller

import com.rdk.cinemachallenge.dto.RoomDTO
import com.rdk.cinemachallenge.service.RoomService
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.util.*

@WebMvcTest(RoomController::class)
class RoomControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var roomService: RoomService

    @Test
    fun `test get all`() {
        whenever(roomService.getAllRooms()).thenReturn((0..10).map(::roomDTO))

        this.mockMvc.perform(get("/room")).andDo(print()).andExpect(status().isOk)
            .andExpect(
                content().string(
                    equalTo(
                        "[{\"id\":\"0\",\"name\":\"room 0\",\"3dAvailable\":true},{\"id\":\"1\",\"name\":\"room 1\",\"3dAvailable\":false},{\"id\":\"2\",\"name\":\"room 2\",\"3dAvailable\":true},{\"id\":\"3\",\"name\":\"room 3\",\"3dAvailable\":false},{\"id\":\"4\",\"name\":\"room 4\",\"3dAvailable\":true},{\"id\":\"5\",\"name\":\"room 5\",\"3dAvailable\":false},{\"id\":\"6\",\"name\":\"room 6\",\"3dAvailable\":true},{\"id\":\"7\",\"name\":\"room 7\",\"3dAvailable\":false},{\"id\":\"8\",\"name\":\"room 8\",\"3dAvailable\":true},{\"id\":\"9\",\"name\":\"room 9\",\"3dAvailable\":false},{\"id\":\"10\",\"name\":\"room 10\",\"3dAvailable\":true}]"
                    )
                )
            )
    }

    @Test
    fun `test get one`() {
        var roomId = UUID.randomUUID()
        whenever(roomService.getRoom(roomId)).thenReturn(roomDTO(5, roomId.toString()))

        this.mockMvc.perform(get("/room/$roomId")).andDo(print()).andExpect(status().isOk)
            .andExpect(
                content().string(equalTo("{\"id\":\"$roomId\",\"name\":\"room 5\",\"3dAvailable\":false}"))
            )
    }

    @Test
    fun `test get one invalid id`() {
        this.mockMvc.perform(get("/room/test")).andDo(print()).andExpect(status().`is`(400))
    }

    @Test
    fun `test get non existing`() {
        var roomId = UUID.randomUUID()
        whenever(roomService.getRoom(roomId)).thenThrow(NoSuchElementException("Unknown room"))

        this.mockMvc.perform(get("/room/$roomId")).andDo(print()).andExpect(status().`is`(404))
            .andExpect(
                content().string(equalTo("Unknown room"))
            )
    }

    @Test
    fun `test put one`() {
        val body = """
            {
                "name":"Awesome Room",
                "cleaningDuration": "PT10M",
                "available3d": true
            }
        """.trimIndent()
        var roomId = UUID.randomUUID()
        whenever(
            roomService.addRoom(
                name = "Awesome Room",
                cleaningDuration = Duration.ofMinutes(10L),
                available3d = true
            )
        ).thenReturn(roomId.toString())

        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk)
            .andExpect(
                content().string(equalTo("$roomId"))
            )
    }

    @Test
    fun `test put one false bools`() {
        val body = """
            {
                "name":"Awesome Room",
                "cleaningDuration": "PT10M",
                "available3d": false
            }
        """.trimIndent()
        var roomId = UUID.randomUUID()
        whenever(
            roomService.addRoom(
                name = "Awesome Room",
                cleaningDuration = Duration.ofMinutes(10L),
                available3d = false
            )
        ).thenReturn(roomId.toString())

        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk)
            .andExpect(
                content().string(equalTo("$roomId"))
            )
    }

    @Test
    fun `test put one no name`() {
        val body = """
            {
                "cleaningDuration": "PT15M",
                "available3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"name\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one blank name`() {
        val body = """
            {
                "name": "",
                "cleaningDuration": "PT15M",
                "available3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"name\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one null name`() {
        val body = """
            {
                "name": null,
                "cleaningDuration": "PT15M",
                "available3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"name\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one no cleaningDuration`() {
        val body = """
            {
                "name": "Awesome Room",
                "available3d": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"cleaningDuration\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one blank cleaningDuration`() {
        val body = """
            {
                "name": "Awesome Room",
                "cleaningDuration": "",
                "available3d": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"cleaningDuration\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one null cleaningDuration`() {
        val body = """
            {
                "name": "Awesome Room",
                "cleaningDuration": null,
                "available3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"cleaningDuration\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one invalid cleaningDuration`() {
        val body = """
            {
                "name": "Awesome Room",
                "cleaningDuration": "test",
                "available3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"cleaningDuration\":\"Invalid duration\"}"))
            )
    }

    @Test
    fun `test put one no available3d`() {
        val body = """
            {
                "name": "Awesome Room",
                "cleaningDuration": "PT15M",
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"available3d\":\"must not be null\"}"))
            )
    }

    @Test
    fun `test put one null available3d`() {
        val body = """
            {
                "name": "Awesome Room",
                "cleaningDuration": "PT15M",
                "available3d": null
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("Unable to read request")) //TODO NotNull validator should catch this. Investigate.
            )
    }


    @Test
    fun `test put one string available3d`() {
        val body = """
            {
                "name": "Awesome Room",
                "cleaningDuration": "PT15M",
                "available3d": "test"
            }
        """.trimIndent()
        this.mockMvc.perform(post("/room").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("Unable to read request"))
            )
    }

    @Test
    fun `test delete one`() {
        val roomId = UUID.randomUUID()
        this.mockMvc.perform(delete("/room/$roomId").contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun `test delete one invalid id`() {
        this.mockMvc.perform(delete("/room/test").contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
    }


    companion object {
        fun roomDTO(counter: Int = 0, id: String? = null) =
            RoomDTO(
                id = id ?: "$counter",
                name = "room $counter",
                `3dAvailable` = counter % 2 == 0
            )
    }

}