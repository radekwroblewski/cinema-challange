package com.rdk.cinemachallenge.controller

import com.rdk.cinemachallenge.dto.MovieDTO
import com.rdk.cinemachallenge.service.MovieService
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

@WebMvcTest(MovieController::class)
class MovieControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var movieService: MovieService

    @Test
    fun `test get all`() {
        whenever(movieService.getAllMovies()).thenReturn((0..10).map(::movieDTO))

        this.mockMvc.perform(get("/movie")).andDo(print()).andExpect(status().isOk)
            .andExpect(
                content().string(
                    equalTo(
                        "[{\"id\":\"0\",\"title\":\"title 0\",\"3d\":true,\"duration\":\"PT10M\",\"premiere\":true},{\"id\":\"1\",\"title\":\"title 1\",\"3d\":false,\"duration\":\"PT20M\",\"premiere\":false},{\"id\":\"2\",\"title\":\"title 2\",\"3d\":true,\"duration\":\"PT30M\",\"premiere\":false},{\"id\":\"3\",\"title\":\"title 3\",\"3d\":false,\"duration\":\"PT40M\",\"premiere\":true},{\"id\":\"4\",\"title\":\"title 4\",\"3d\":true,\"duration\":\"PT50M\",\"premiere\":false},{\"id\":\"5\",\"title\":\"title 5\",\"3d\":false,\"duration\":\"PT60M\",\"premiere\":false},{\"id\":\"6\",\"title\":\"title 6\",\"3d\":true,\"duration\":\"PT70M\",\"premiere\":true},{\"id\":\"7\",\"title\":\"title 7\",\"3d\":false,\"duration\":\"PT80M\",\"premiere\":false},{\"id\":\"8\",\"title\":\"title 8\",\"3d\":true,\"duration\":\"PT90M\",\"premiere\":false},{\"id\":\"9\",\"title\":\"title 9\",\"3d\":false,\"duration\":\"PT100M\",\"premiere\":true},{\"id\":\"10\",\"title\":\"title 10\",\"3d\":true,\"duration\":\"PT110M\",\"premiere\":false}]"
                    )
                )
            )
    }

    @Test
    fun `test get one`() {
        var movieId = UUID.randomUUID()
        whenever(movieService.getMovie(movieId)).thenReturn(movieDTO(5, movieId.toString()))

        this.mockMvc.perform(get("/movie/$movieId")).andDo(print()).andExpect(status().isOk)
            .andExpect(
                content().string(equalTo("{\"id\":\"$movieId\",\"title\":\"title 5\",\"3d\":false,\"duration\":\"PT60M\",\"premiere\":false}"))
            )
    }

    @Test
    fun `test get one invalid id`() {
        this.mockMvc.perform(get("/movie/test")).andDo(print()).andExpect(status().`is`(400))
    }

    @Test
    fun `test get non existing`() {
        var movieId = UUID.randomUUID()
        whenever(movieService.getMovie(movieId)).thenThrow(NoSuchElementException("Unknown movie"))

        this.mockMvc.perform(get("/movie/$movieId")).andDo(print()).andExpect(status().`is`(404))
            .andExpect(
                content().string(equalTo("Unknown movie"))
            )
    }

    @Test
    fun `test put one`() {
        val body = """
            {
                "title":"Neverending Testing",
                "duration": "PT120M",
                "requires3d": true,
                "premiere": true
            }
        """.trimIndent()
        var movieId = UUID.randomUUID()
        whenever(
            movieService.addMovie(
                title = "Neverending Testing",
                duration = Duration.ofMinutes(120L),
                requires3d = true,
                premiere = true
            )
        ).thenReturn(movieId.toString())

        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk)
            .andExpect(
                content().string(equalTo("$movieId"))
            )
    }

    @Test
    fun `test put one false bools`() {
        val body = """
            {
                "title":"Neverending Testing",
                "duration": "PT120M",
                "requires3d": false,
                "premiere": false
            }
        """.trimIndent()
        var movieId = UUID.randomUUID()
        whenever(
            movieService.addMovie(
                title = "Neverending Testing",
                duration = Duration.ofMinutes(120L),
                requires3d = false,
                premiere = false
            )
        ).thenReturn(movieId.toString())

        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk)
            .andExpect(
                content().string(equalTo("$movieId"))
            )
    }

    @Test
    fun `test put one no title`() {
        val body = """
            {
                "duration": "PT120M",
                "requires3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"title\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one blank title`() {
        val body = """
            {
                "title": "",
                "duration": "PT120M",
                "requires3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"title\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one null title`() {
        val body = """
            {
                "title": null,
                "duration": "PT120M",
                "requires3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"title\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one no duration`() {
        val body = """
            {
                "title": "Neverending Testing",
                "requires3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"duration\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one blank duration`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": "",
                "requires3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"duration\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one null duration`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": null,
                "requires3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"duration\":\"must not be blank\"}"))
            )
    }

    @Test
    fun `test put one invalid duration`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": "test",
                "requires3d": true,
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"duration\":\"Invalid duration\"}"))
            )
    }

    @Test
    fun `test put one no requires3d`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": "PT120M",
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"requires3d\":\"must not be null\"}"))
            )
    }

    @Test
    fun `test put one null requires3d`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": "PT120M",
                "requires3d": null
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("Unable to read request")) //TODO NotNull validator should catch this. Investigate.
            )
    }


    @Test
    fun `test put one string requires3d`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": "PT120M",
                "requires3d": "test"
                "premiere": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("Unable to read request"))
            )
    }

    @Test
    fun `test put one no premiere`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": "PT120M",
                "requires3d": true
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("{\"premiere\":\"must not be null\"}"))
            )
    }

    @Test
    fun `test put one null premiere`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": "PT120M",
                "requires3d": false
                "premiere": null
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("Unable to read request")) //TODO NotNull validator should catch this. Investigate.
            )
    }

    @Test
    fun `test put one string premiere`() {
        val body = """
            {
                "title": "Neverending Testing",
                "duration": "PT120M",
                "requires3d": false
                "premiere": "test"
            }
        """.trimIndent()
        this.mockMvc.perform(post("/movie").content(body).contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
            .andExpect(
                content().string(equalTo("Unable to read request"))
            )
    }

    @Test
    fun `test delete one`() {
        val movieId = UUID.randomUUID()
        this.mockMvc.perform(delete("/movie/$movieId").contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun `test delete one invalid id`() {
        this.mockMvc.perform(delete("/movie/test").contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().`is`(400))
    }


    companion object {
        fun movieDTO(counter: Int = 0, id: String? = null) =
            MovieDTO(
                id = id ?: "$counter",
                title = "title $counter",
                `3d` = counter % 2 == 0,
                duration = "PT${(counter + 1) * 10}M",
                premiere = counter % 3 == 0
            )
    }

}