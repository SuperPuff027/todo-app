package de.superpuff.todo.daten

import android.content.Context
import androidx.glance.appwidget.updateAll
import de.superpuff.todo.widget.AufgabenWidget
import java.time.LocalDate

/**
 * Alle schreibenden Zugriffe auf Aufgaben laufen hier durch,
 * damit danach immer auch das Homescreen-Widget aktualisiert wird.
 */
object Aufgaben {

    fun dao(context: Context): AufgabeDao = Datenbank.hole(context).aufgaben()

    suspend fun hinzufuegen(context: Context, text: String, tag: LocalDate) {
        val bereinigt = text.trim()
        if (bereinigt.isEmpty()) return
        val epochTag = tag.toEpochDay()
        dao(context).einfuegen(
            Aufgabe(
                text = bereinigt.replaceFirstChar { it.uppercaseChar() },
                datum = epochTag,
                urspruenglichesDatum = epochTag,
                erstelltAm = System.currentTimeMillis(),
            ),
        )
        widgetAktualisieren(context)
    }

    suspend fun erledigen(context: Context, id: Long) {
        dao(context).setzeErledigt(id, true, System.currentTimeMillis())
        widgetAktualisieren(context)
    }

    suspend fun rueckgaengig(context: Context, id: Long) {
        dao(context).setzeErledigt(id, false, null)
        widgetAktualisieren(context)
    }

    /** Legt unerledigte Aufgaben vergangener Tage auf heute. Wird bei jedem Start, um Mitternacht und vor jeder Erinnerung aufgerufen. */
    suspend fun uebertragen(context: Context): Int {
        val anzahl = dao(context).uebertrage(LocalDate.now().toEpochDay())
        if (anzahl > 0) widgetAktualisieren(context)
        return anzahl
    }

    suspend fun widgetAktualisieren(context: Context) {
        runCatching { AufgabenWidget().updateAll(context.applicationContext) }
    }
}
