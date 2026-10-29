package de.superpuff.todo.daten

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AufgabeDao {

    @Query("SELECT * FROM aufgaben WHERE datum = :tag AND erledigt = 0 ORDER BY erstellt_am, id")
    fun offeneAmTag(tag: Long): Flow<List<Aufgabe>>

    @Query("SELECT * FROM aufgaben WHERE datum = :tag AND erledigt = 0 ORDER BY erstellt_am, id")
    suspend fun offeneAmTagJetzt(tag: Long): List<Aufgabe>

    @Query("SELECT * FROM aufgaben WHERE datum BETWEEN :von AND :bis AND erledigt = 0 ORDER BY datum, erstellt_am, id")
    fun offeneZwischen(von: Long, bis: Long): Flow<List<Aufgabe>>

    @Insert
    suspend fun einfuegen(aufgabe: Aufgabe): Long

    @Query("UPDATE aufgaben SET erledigt = :erledigt, erledigt_am = :am WHERE id = :id")
    suspend fun setzeErledigt(id: Long, erledigt: Boolean, am: Long?)

    /** Schiebt alle offenen Aufgaben aus vergangenen Tagen auf [heute]. Gibt die Anzahl verschobener Aufgaben zurück. */
    @Query("UPDATE aufgaben SET datum = :heute WHERE erledigt = 0 AND datum < :heute")
    suspend fun uebertrage(heute: Long): Int
}
