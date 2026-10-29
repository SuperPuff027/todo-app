package de.superpuff.todo.daten

import android.content.Context
import java.time.LocalTime

/** Kleine Einstellungen, die keine Datenbank brauchen. */
object Einstellungen {
    private const val DATEI = "einstellungen"
    private const val STUNDE = "erinnerung_stunde"
    private const val MINUTE = "erinnerung_minute"
    private const val BERECHTIGUNG_GEFRAGT = "berechtigung_gefragt"

    val STANDARD_ERINNERUNG: LocalTime = LocalTime.of(18, 0)

    private fun prefs(context: Context) = context.getSharedPreferences(DATEI, Context.MODE_PRIVATE)

    fun erinnerungszeit(context: Context): LocalTime {
        val p = prefs(context)
        return LocalTime.of(
            p.getInt(STUNDE, STANDARD_ERINNERUNG.hour),
            p.getInt(MINUTE, STANDARD_ERINNERUNG.minute),
        )
    }

    fun setzeErinnerungszeit(context: Context, zeit: LocalTime) {
        prefs(context).edit().putInt(STUNDE, zeit.hour).putInt(MINUTE, zeit.minute).apply()
    }

    fun berechtigungGefragt(context: Context): Boolean = prefs(context).getBoolean(BERECHTIGUNG_GEFRAGT, false)

    fun merkeBerechtigungGefragt(context: Context) {
        prefs(context).edit().putBoolean(BERECHTIGUNG_GEFRAGT, true).apply()
    }
}
