package com.rdk.cinemachallenge.dto


data class ShowDTO(val id: String, val movie: MovieDTO, override val time: TimePeriodDTO) : ScheduledItemDTO

data class CleaningPeriodDTO(override val time: TimePeriodDTO) : ScheduledItemDTO {
    val name: String = "Cleaning"
}

interface ScheduledItemDTO {
    val time: TimePeriodDTO
}
