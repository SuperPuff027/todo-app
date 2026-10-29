package de.superpuff.todo.daten

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Aufgabe::class], version = 1, exportSchema = false)
abstract class Datenbank : RoomDatabase() {
    abstract fun aufgaben(): AufgabeDao

    companion object {
        @Volatile
        private var instanz: Datenbank? = null

        fun hole(context: Context): Datenbank =
            instanz ?: synchronized(this) {
                instanz ?: Room.databaseBuilder(
                    context.applicationContext,
                    Datenbank::class.java,
                    "aufgaben.db",
                ).build().also { instanz = it }
            }
    }
}
