package de.superpuff.todo

import android.app.Application
import de.superpuff.todo.erinnerung.Benachrichtigung
import de.superpuff.todo.erinnerung.Planer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Benachrichtigung.kanalAnlegen(this)
        Planer.planeAlles(this)
    }

    companion object {
        /** Für Speichervorgänge, die auch dann zu Ende laufen sollen, wenn ein Bildschirm schon geschlossen ist. */
        val hintergrund = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
