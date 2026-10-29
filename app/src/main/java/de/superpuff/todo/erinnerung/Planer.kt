package de.superpuff.todo.erinnerung

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.superpuff.todo.daten.Einstellungen
import de.superpuff.todo.logik.Kalender
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Plant zwei wiederkehrende Weckrufe über den AlarmManager:
 *  - die tägliche Erinnerung zur eingestellten Uhrzeit,
 *  - den Übertrag kurz nach Mitternacht.
 * Jeder Weckruf plant nach dem Auslösen den nächsten.
 *
 * `setAndAllowWhileIdle` braucht keine Sonderberechtigung für exakte Alarme; Android darf ihn im
 * Energiesparmodus um einige Minuten verschieben.
 */
object Planer {
    const val AKTION_ERINNERUNG = "de.superpuff.todo.ERINNERUNG"
    const val AKTION_MITTERNACHT = "de.superpuff.todo.MITTERNACHT"

    private val KURZ_NACH_MITTERNACHT: LocalTime = LocalTime.of(0, 0, 30)

    fun planeAlles(context: Context) {
        planeErinnerung(context)
        planeMitternacht(context)
    }

    fun planeErinnerung(context: Context) {
        plane(context, AKTION_ERINNERUNG, 1, Einstellungen.erinnerungszeit(context))
    }

    fun planeMitternacht(context: Context) {
        plane(context, AKTION_MITTERNACHT, 2, KURZ_NACH_MITTERNACHT)
    }

    private fun plane(context: Context, aktion: String, anfrageCode: Int, uhrzeit: LocalTime) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val zeitpunkt = Kalender.naechsterZeitpunkt(LocalDateTime.now(), uhrzeit)
        val millis = zeitpunkt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent = Intent(context, ErinnerungEmpfaenger::class.java).setAction(aktion)
        val ausstehend = PendingIntent.getBroadcast(
            context,
            anfrageCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, ausstehend)
    }
}
