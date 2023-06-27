package com.rdk.cinemachallenge.controller

import com.rdk.cinemachallenge.controller.MovieControllerTest.Companion.movieDTO
import com.rdk.cinemachallenge.dto.CleaningPeriodDTO
import com.rdk.cinemachallenge.dto.ShowDTO
import com.rdk.cinemachallenge.dto.TimePeriodDTO
import com.rdk.cinemachallenge.exceptions.ValidationException
import com.rdk.cinemachallenge.service.SchedulingService
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDate
import java.util.*

@WebMvcTest(ScheduleController::class)
class ScheduleControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var schedulingService: SchedulingService

    private val roomId = UUID.randomUUID()
    private val movieId = UUID.randomUUID()
    private val today = LocalDate.now()
    private val showTime = today.atTime(18, 0)


    @Test
    fun `test put one`() {
        val showId = UUID.randomUUID().toString()
        whenever(
            schedulingService.scheduleShow(
                movieId = movieId,
                roomId = roomId,
                startTime = showTime
            )
        ).thenReturn(showId)

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("/schedule/room/$roomId/movie/$movieId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo(showId))
            )
    }

    @Test
    fun `test put one invalid roomId`() {


        this.mockMvc.perform(
            MockMvcRequestBuilders.post("/schedule/room/test/movie/$movieId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("scheduleShow.roomId: Invalid UUID"))
            )
    }

    @Test
    fun `test put one invalid data`() {
        whenever(
            schedulingService.scheduleShow(
                movieId = movieId,
                roomId = roomId,
                startTime = showTime
            )
        ).thenThrow(ValidationException(listOf("invalid1","invalid2")))

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("/schedule/room/$roomId/movie/$movieId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("invalid1, invalid2"))
            )
    }

    @Test
    fun `test put one invalid movieId`() {
        this.mockMvc.perform(
            MockMvcRequestBuilders.post("/schedule/room/$roomId/movie/test")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("scheduleShow.movieId: Invalid UUID"))
            )
    }

    @Test
    fun `test put one blank showtime`() {
        this.mockMvc.perform(
            MockMvcRequestBuilders.post("/schedule/room/$roomId/movie/$movieId")
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("scheduleShow.startTime: must not be blank"))
            )
    }

    @Test
    fun `test put one invalid showtime`() {
        this.mockMvc.perform(
            MockMvcRequestBuilders.post("/schedule/room/$roomId/movie/$movieId")
                .content("test").contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("scheduleShow.startTime: Invalid date time"))
            )
    }

    @Test
    fun `test put one wrong date showtime`() {
        this.mockMvc.perform(
            MockMvcRequestBuilders.post("/schedule/room/$roomId/movie/$movieId")
                .content("2023-13-62T25:72").contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("scheduleShow.startTime: Invalid date time"))
            )
    }

    @Test
    fun `test get daily shows`() {
        whenever(schedulingService.getRoomShows(roomId, today)).thenReturn((0..7).map {
            if (it % 2 == 0) {
                showDTO(it)
            } else {
                cleaningDTO(it)
            }
        })

        this.mockMvc.perform(
            MockMvcRequestBuilders.get("/schedule/room/$roomId/date/$today")
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(
                        Matchers.equalTo(
                            "[{\"id\":\"0\",\"movie\":{\"id\":\"0\",\"title\":\"title 0\",\"3d\":true,\"duration\":\"PT10M\",\"premiere\":true},\"time\":{\"start\":\"2023-06-27T08:00\",\"end\":\"2023-06-27T08:50\"}},{\"time\":{\"start\":\"2023-06-27T09:50\",\"end\":\"2023-06-27T10:00\"},\"name\":\"Cleaning\"},{\"id\":\"2\",\"movie\":{\"id\":\"2\",\"title\":\"title 2\",\"3d\":true,\"duration\":\"PT30M\",\"premiere\":false},\"time\":{\"start\":\"2023-06-27T10:00\",\"end\":\"2023-06-27T10:50\"}},{\"time\":{\"start\":\"2023-06-27T11:50\",\"end\":\"2023-06-27T12:00\"},\"name\":\"Cleaning\"},{\"id\":\"4\",\"movie\":{\"id\":\"4\",\"title\":\"title 4\",\"3d\":true,\"duration\":\"PT50M\",\"premiere\":false},\"time\":{\"start\":\"2023-06-27T12:00\",\"end\":\"2023-06-27T12:50\"}},{\"time\":{\"start\":\"2023-06-27T13:50\",\"end\":\"2023-06-27T14:00\"},\"name\":\"Cleaning\"},{\"id\":\"6\",\"movie\":{\"id\":\"6\",\"title\":\"title 6\",\"3d\":true,\"duration\":\"PT70M\",\"premiere\":true},\"time\":{\"start\":\"2023-06-27T14:00\",\"end\":\"2023-06-27T14:50\"}},{\"time\":{\"start\":\"2023-06-27T15:50\",\"end\":\"2023-06-27T16:00\"},\"name\":\"Cleaning\"}]"
                        )
                    )
            )
    }

    @Test
    fun `test get daily shows invalid date`() {

        this.mockMvc.perform(
            MockMvcRequestBuilders.get("/schedule/room/$roomId/date/test")
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(
                        Matchers.equalTo(
                            "getRoomSchedule.date: Invalid date"
                        )
                    )
            )
    }

    @Test
    fun `test get daily shows wrong date`() {

        this.mockMvc.perform(
            MockMvcRequestBuilders.get("/schedule/room/$roomId/date/2025-13-33")
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(
                        Matchers.equalTo(
                            "getRoomSchedule.date: Invalid date"
                        )
                    )
            )
    }

    @Test
    fun `test get daily shows invalid room`() {

        this.mockMvc.perform(
            MockMvcRequestBuilders.get("/schedule/room/test/date/$today")
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(
                        Matchers.equalTo(
                            "getRoomSchedule.roomId: Invalid UUID"
                        )
                    )
            )
    }


    @Test
    fun `test put one concurrent`() {

        whenever(
            schedulingService.scheduleShow(
                movieId = movieId,
                roomId = roomId,
                startTime = showTime
            )
        ).thenThrow(ConcurrentModificationException("Concurrent modification occurred; Please try again."))

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("/schedule/room/$roomId/movie/$movieId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("Concurrent modification occurred; Please try again."))
            )
    }

    @Test
    fun `test delete show`() {

        this.mockMvc.perform(
            MockMvcRequestBuilders.delete("/schedule/show/${UUID.randomUUID()}")
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

    }

    @Test
    fun `test delete invalid show`() {

        this.mockMvc.perform(
            MockMvcRequestBuilders.delete("/schedule/show/test")
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(
                        Matchers.equalTo(
                            "cancelShow.showId: Invalid UUID"
                        )
                    )
            )
    }

    @Test
    fun `test changing show both room and time`() {
        val showId = UUID.randomUUID()
        val newShowId = UUID.randomUUID().toString()
        whenever(
            schedulingService.changeShow(
                showId = showId,
                newRoomId = roomId,
                newStartTime = showTime
            )
        ).thenReturn(newShowId)

        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/$showId/room/$roomId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo(newShowId))
            )
    }

    @Test
    fun `test changing show room`() {
        val showId = UUID.randomUUID()
        val newShowId = UUID.randomUUID().toString()
        whenever(
            schedulingService.changeShow(
                showId = showId,
                newRoomId = roomId,
                newStartTime = null
            )
        ).thenReturn(newShowId)

        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/$showId/room/$roomId")
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo(newShowId))
            )
    }

    @Test
    fun `test changing show time`() {
        val showId = UUID.randomUUID()
        val newShowId = UUID.randomUUID().toString()
        whenever(
            schedulingService.changeShow(
                showId = showId,
                newRoomId = null,
                newStartTime = showTime
            )
        ).thenReturn(newShowId)

        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/$showId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo(newShowId))
            )
    }

    @Test
    fun `test change one invalid roomId`() {
        val showId = UUID.randomUUID()
        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/$showId/room/test")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("changeShow.roomId: Invalid UUID"))
            )
    }

    @Test
    fun `test change one invalid showId`() {
        val showId = UUID.randomUUID()
        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/test/room/$roomId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("changeShow.showId: Invalid UUID"))
            )
    }


    @Test
    fun `test change one invalid showtime`() {
        val showId = UUID.randomUUID()
        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/$showId/room/$roomId")
                .content("test").contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("changeShow.startTime: Invalid date time"))
            )
    }

    @Test
    fun `test change one wrong showtime`() {
        val showId = UUID.randomUUID()
        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/$showId/room/$roomId")
                .content("2023-13-36T27:72").contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("changeShow.startTime: Invalid date time"))
            )
    }

    @Test
    fun `test change one invalid data`() {
        val showId = UUID.randomUUID()
        whenever(
            schedulingService.changeShow(
                showId = showId,
                newRoomId = roomId,
                newStartTime = showTime
            )
        ).thenThrow(ValidationException(listOf("invalid1","invalid2")))

        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/$showId/room/$roomId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("invalid1, invalid2"))
            )
    }


    @Test
    fun `test change one concurrent`() {
        val showId = UUID.randomUUID()
        whenever(
            schedulingService.changeShow(
                showId = showId,
                newRoomId = roomId,
                newStartTime = showTime
            )
        ).thenThrow(ConcurrentModificationException("Concurrent modification occurred; Please try again."))

        this.mockMvc.perform(
            MockMvcRequestBuilders.put("/schedule/show/$showId/room/$roomId")
                .content(showTime.toString()).contentType(MediaType.APPLICATION_JSON)
        ).andDo(
            MockMvcResultHandlers.print()
        )
            .andExpect(MockMvcResultMatchers.status().`is`(400))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.equalTo("Concurrent modification occurred; Please try again."))
            )
    }

    companion object {
        fun showDTO(counter: Int) = ShowDTO(
            "$counter",
            movieDTO(counter),
            TimePeriodDTO(
                LocalDate.now().atTime(8 + counter, 0).toString(),
                LocalDate.now().atTime(8 + counter, 50).toString()
            )
        )

        fun cleaningDTO(counter: Int) = CleaningPeriodDTO(
            TimePeriodDTO(
                LocalDate.now().atTime(8 + counter, 50).toString(),
                LocalDate.now().atTime(9 + counter, 0).toString()
            )
        )
    }

}