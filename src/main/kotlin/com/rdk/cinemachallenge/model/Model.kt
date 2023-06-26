package com.rdk.cinemachallenge.model

import java.time.Duration
import java.time.LocalDateTime
import java.util.*

open class Identifiable (val id: UUID = UUID.randomUUID())

data class Room(val name: String, val cleaningDuration: Duration, val avaibable3D: Boolean): Identifiable()

data class Movie(val title:String, val duration: Duration, var premiere: Boolean, val requires3D: Boolean): Identifiable()

data class Show(val room: Room, val movie: Movie, var startTime: LocalDateTime): Identifiable()

