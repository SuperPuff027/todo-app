package de.superpuff.todo.daten

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Eine Aufgabe für einen bestimmten Tag.
 *
 * Tage werden als Epoch-Tag gespeichert (Tage seit 1970-01-01, `LocalDate.toEpochDay()`).
 *
 * @property datum Tag, an dem die Aufgabe aktuell steht. Wird beim Übertrag um Mitternacht auf heute gesetzt.
 * @property urspruenglichesDatum Tag, für den die Aufgabe ursprünglich geplant war. Bleibt beim Übertrag gleich;
 *   Ist `datum` größer, wurde die Aufgabe übertragen und bekommt das Label „seit X Tagen offen“.
 * @property erledigt Erledigte Aufgaben verschwinden aus allen Ansichten, bleiben aber in der Datenbank.
 * @property erstelltAm Zeitpunkt der Eingabe in Millisekunden, bleibt unverändert. Legt die Reihenfolge fest;
 *   der Abstand vom Eingabetag bis `datum` ergibt das X im Label.
 */
@Entity(tableName = "aufgaben")
data class Aufgabe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val datum: Long,
    @ColumnInfo(name = "urspruengliches_datum") val urspruenglichesDatum: Long,
    val erledigt: Boolean = false,
    @ColumnInfo(name = "erledigt_am") val erledigtAm: Long? = null,
    @ColumnInfo(name = "erstellt_am") val erstelltAm: Long,
)
