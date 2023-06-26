package com.rdk.cinemachallenge.model

import com.rdk.cinemachallenge.testutils.isBeforeOrEq
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

abstract class SimpleRepository<T : Identifiable>(private val entityName: String) {
    private var entries: Map<UUID, T> = emptyMap()

    fun add(entry: T): UUID {
        entries += entry.id to entry
        return entry.id
    }

    fun remove(id: UUID) {
        entries -= id
    }

    fun getOne(id: UUID) = entries[id] ?: throw NoSuchElementException("Unknown $entityName")

    fun getAll() = entries.values.toList()
}

@Repository
class RoomRepository : SimpleRepository<Room>("room")

@Repository
class MovieRepository : SimpleRepository<Movie>("movie")

@Repository
class ShowRepository {

    private data class RoomShowDay(val roomId: UUID, val date: LocalDate)

    private var shows: Map<RoomShowDay, List<Show>> = emptyMap()

    private var allShows: Map<UUID, Show> = emptyMap()

    @Volatile
    private var showVersion: Map<RoomShowDay, Int> = emptyMap()

    fun getShows(roomId: UUID, date: LocalDate) = shows[RoomShowDay(roomId, date)] ?: emptyList()

    fun addShow(show: Show, version: Int): UUID {
        val roomShowDay = RoomShowDay(show.room.id, show.startTime.toLocalDate())
        // in multi-instance environment the synchronization would not work. Would use database lock mechanism.
        synchronized(this.javaClass) {
            if (getShowVersion(roomShowDay) != version) {
                throw ConcurrentModificationException("Concurrent modification occurred; Please try again.")
            }
            val dailyShows = shows[roomShowDay] ?: emptyList()
            shows += roomShowDay to dailyShows + show
            allShows += show.id to show
            setShowVersion(roomShowDay, version + 1)
        }
        return show.id
    }

    private fun setShowVersion(roomShowDay: RoomShowDay, version: Int) {
        this.showVersion += roomShowDay to version
    }

    fun getShowVersion(roomId: UUID, date: LocalDate) = getShowVersion(RoomShowDay(roomId, date))

    private fun getShowVersion(roomShowDay: RoomShowDay): Int = showVersion[roomShowDay] ?: 0

    fun removeShow(showId: UUID) {
        allShows[showId]?.let {
            val roomShowDay = RoomShowDay(it.room.id, it.startTime.toLocalDate())
            val dailyShows = shows[roomShowDay] ?: emptyList()
            shows += roomShowDay to (dailyShows - it)
        }
        allShows -= showId
    }

    fun getShow(showId: UUID) = allShows[showId] ?: throw NoSuchElementException("Unknown show")

}
