package de.superpuff.todo.erinnerung

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.superpuff.todo.daten.Aufgaben
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Nach einem Neustart, einem App-Update oder einer Zeitumstellung sind geplante Alarme weg
 * bzw. falsch. Hier wird alles neu geplant und der Übertrag nachgeholt.
 */
class StartEmpfaenger : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        Planer.planeAlles(app)
        val ergebnis = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                Aufgaben.uebertragen(app)
                Aufgaben.widgetAktualisieren(app)
            } finally {
                ergebnis.finish()
            }
        }
    }
}
