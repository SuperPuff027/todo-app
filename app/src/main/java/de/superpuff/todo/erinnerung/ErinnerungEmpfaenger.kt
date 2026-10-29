package de.superpuff.todo.erinnerung

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.superpuff.todo.daten.Aufgaben
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Empfängt die Weckrufe aus [Planer]. */
class ErinnerungEmpfaenger : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val ergebnis = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    Planer.AKTION_ERINNERUNG -> {
                        Aufgaben.uebertragen(app)
                        val offen = Aufgaben.dao(app).offeneAmTagJetzt(LocalDate.now().toEpochDay())
                        if (offen.isNotEmpty()) Benachrichtigung.zeige(app, offen.map { it.text })
                        Planer.planeErinnerung(app)
                    }
                    Planer.AKTION_MITTERNACHT -> {
                        Aufgaben.uebertragen(app)
                        // Auch ohne Übertrag muss das Widget das neue Datum zeigen.
                        Aufgaben.widgetAktualisieren(app)
                        Planer.planeMitternacht(app)
                    }
                }
            } finally {
                ergebnis.finish()
            }
        }
    }
}
