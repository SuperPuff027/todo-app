package de.superpuff.todo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.superpuff.todo.TodoApp
import de.superpuff.todo.daten.Aufgabe
import de.superpuff.todo.daten.Aufgaben
import de.superpuff.todo.daten.Einstellungen
import de.superpuff.todo.erinnerung.Planer
import de.superpuff.todo.logik.Kalender
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

enum class Ansicht { TAG, WOCHE }

@OptIn(ExperimentalCoroutinesApi::class)
class HauptViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = Aufgaben.dao(app)

    private val _heute = MutableStateFlow(LocalDate.now())
    val heute: StateFlow<LocalDate> = _heute.asStateFlow()

    private val _tag = MutableStateFlow(LocalDate.now())
    /** Der Tag, der in der Tagesansicht gezeigt wird. */
    val tag: StateFlow<LocalDate> = _tag.asStateFlow()

    private val _ansicht = MutableStateFlow(Ansicht.TAG)
    val ansicht: StateFlow<Ansicht> = _ansicht.asStateFlow()

    private val _wochenStart = MutableStateFlow(Kalender.wochenStart(LocalDate.now()))
    val wochenStart: StateFlow<LocalDate> = _wochenStart.asStateFlow()

    private val _erinnerung = MutableStateFlow(Einstellungen.erinnerungszeit(app))
    val erinnerung: StateFlow<LocalTime> = _erinnerung.asStateFlow()

    val aufgabenDesTages: StateFlow<List<Aufgabe>> = _tag
        .flatMapLatest { dao.offeneAmTag(it.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Offene Aufgaben der gezeigten Woche, nach Tag gruppiert. */
    val aufgabenDerWoche: StateFlow<Map<LocalDate, List<Aufgabe>>> = _wochenStart
        .flatMapLatest { montag ->
            dao.offeneZwischen(montag.toEpochDay(), montag.plusDays(6).toEpochDay())
                .map { liste -> liste.groupBy { LocalDate.ofEpochDay(it.datum) } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** Beim Zurückkehren in die App: Datum prüfen und Übertrag nachholen. */
    fun beimFortsetzen() {
        val jetzt = LocalDate.now()
        if (jetzt != _heute.value) {
            val warHeute = _tag.value == _heute.value
            _heute.value = jetzt
            if (warHeute || _tag.value.isBefore(jetzt)) _tag.value = jetzt
            _wochenStart.value = Kalender.wochenStart(_tag.value)
        }
        viewModelScope.launch { Aufgaben.uebertragen(getApplication<Application>()) }
    }

    fun hinzufuegen(text: String) {
        val ziel = _tag.value
        TodoApp.hintergrund.launch { Aufgaben.hinzufuegen(getApplication<Application>(), text, ziel) }
    }

    fun erledigen(aufgabe: Aufgabe) {
        TodoApp.hintergrund.launch { Aufgaben.erledigen(getApplication<Application>(), aufgabe.id) }
    }

    fun rueckgaengig(aufgabe: Aufgabe) {
        TodoApp.hintergrund.launch { Aufgaben.rueckgaengig(getApplication<Application>(), aufgabe.id) }
    }

    fun zurWoche() {
        _wochenStart.value = Kalender.wochenStart(_tag.value)
        _ansicht.value = Ansicht.WOCHE
    }

    fun zumTag(tag: LocalDate = _tag.value) {
        _tag.value = tag
        _ansicht.value = Ansicht.TAG
    }

    fun zuHeute() {
        _tag.value = _heute.value
        _wochenStart.value = Kalender.wochenStart(_heute.value)
        _ansicht.value = Ansicht.TAG
    }

    fun dieseWoche() {
        _wochenStart.value = Kalender.wochenStart(_heute.value)
    }

    fun wocheVerschieben(wochen: Long) {
        _wochenStart.value = _wochenStart.value.plusWeeks(wochen)
    }

    fun setzeErinnerung(zeit: LocalTime) {
        val app = getApplication<Application>()
        Einstellungen.setzeErinnerungszeit(app, zeit)
        _erinnerung.value = zeit
        Planer.planeErinnerung(app)
    }
}
