package com.rdk.cinemachallenge.testutils

import com.rdk.cinemachallenge.model.Movie
import com.rdk.cinemachallenge.model.Room
import java.time.Duration

object TestObjectProducer {
    fun room(
        counter: Int = 0,
        cleaningDuration: Duration? = null,
        avaibable3D: Boolean? = null
    ) = Room(
        name = "test room $counter",
        cleaningDuration = cleaningDuration ?: Duration.ofMinutes((counter + 1).toLong()),
        avaibable3D = avaibable3D ?: (counter % 2 == 0)
    )

    fun movie(
        counter: Int = 0,
        duration: Duration? = null,
        premiere: Boolean? = null,
        requires3D: Boolean? = null
    ) = Movie(
        title = "test movie $counter",
        duration = duration ?: Duration.ofMinutes(((counter + 1) * 10).toLong()),
        premiere = premiere ?: (counter % 2 == 0),
        requires3D = requires3D ?: (counter % 3 == 0)
    )

}