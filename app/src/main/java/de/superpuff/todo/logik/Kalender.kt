package de.superpuff.todo.logik

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/** Reine Datums- und Textlogik ohne Android-Abhängigkeiten, damit sie mit JUnit testbar ist. */
object Kalender {

    private val DEUTSCH: Locale = Locale.GERMANY

    /** Montag der Woche, in der [tag] liegt. */
    fun wochenStart(tag: LocalDate): LocalDate = tag.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Die sieben Tage Montag bis Sonntag ab [montag]. */
    fun wocheAb(montag: LocalDate): List<LocalDate> = (0L..6L).map { montag.plusDays(it) }

    /** ISO-Kalenderwoche (Woche 1 enthält den ersten Donnerstag des Jahres). */
    fun kalenderwoche(tag: LocalDate): Int = tag.get(WeekFields.ISO.weekOfWeekBasedYear())

    /**
     * Tage für das Label „seit X Tagen offen“: Abstand von `erstellt_am` bis zum aktuellen Tag der Aufgabe
     * (nach dem Übertrag ist das heute). 0, solange die Aufgabe noch an ihrem geplanten Tag steht,
     * damit vorausgeplante Aufgaben kein Label bekommen.
     */
    fun tageOffen(datum: Long, urspruenglichesDatum: Long, erstelltAm: Long, zone: ZoneId = ZoneId.systemDefault()): Int {
        if (datum <= urspruenglichesDatum) return 0
        val erstelltTag = Instant.ofEpochMilli(erstelltAm).atZone(zone).toLocalDate().toEpochDay()
        return (datum - erstelltTag).coerceAtLeast(1L).toInt()
    }

    /** Label für übertragene Aufgaben, oder `null`, wenn die Aufgabe noch an ihrem geplanten Tag steht. */
    fun offenLabel(tage: Int): String? = when {
        tage <= 0 -> null
        tage == 1 -> "seit 1 Tag offen"
        else -> "seit $tage Tagen offen"
    }

    /** Nächster Zeitpunkt mit Uhrzeit [uhrzeit], der echt nach [jetzt] liegt. */
    fun naechsterZeitpunkt(jetzt: LocalDateTime, uhrzeit: LocalTime): LocalDateTime {
        val heute = jetzt.toLocalDate().atTime(uhrzeit)
        return if (heute.isAfter(jetzt)) heute else heute.plusDays(1)
    }

    fun erinnerungsTitel(anzahl: Int): String =
        if (anzahl == 1) "Noch 1 Aufgabe offen" else "Noch $anzahl Aufgaben offen"

    /** Bis zu [maximal] Aufgabentexte, mit „…“, wenn es mehr gibt. */
    fun vorschau(texte: List<String>, maximal: Int = 3): String {
        val gezeigt = texte.take(maximal).joinToString(", ")
        return if (texte.size > maximal) "$gezeigt, …" else gezeigt
    }

    fun wochentag(tag: LocalDate): String = tag.format(DateTimeFormatter.ofPattern("EEEE", DEUTSCH))

    fun wochentagKurz(tag: LocalDate): String = tag.format(DateTimeFormatter.ofPattern("EE", DEUTSCH)).removeSuffix(".")

    fun tagUndMonat(tag: LocalDate): String = tag.format(DateTimeFormatter.ofPattern("d. MMMM", DEUTSCH))

    fun tagUndMonatKurz(tag: LocalDate): String = tag.format(DateTimeFormatter.ofPattern("d.M.", DEUTSCH))

    /** z. B. „26. Okt – 1. Nov“ */
    fun wochenspanne(montag: LocalDate): String {
        val format = DateTimeFormatter.ofPattern("d. MMM", DEUTSCH)
        return "${montag.format(format).removeSuffix(".")} – ${montag.plusDays(6).format(format).removeSuffix(".")}"
    }

    /** Hinweis über dem Eingabefeld, für welchen Tag eingetragen wird. */
    fun zielText(tag: LocalDate, heute: LocalDate): String = when (tag) {
        heute -> "Für heute"
        heute.plusDays(1) -> "Für morgen"
        else -> "Für ${wochentag(tag)}, ${tagUndMonat(tag)}"
    }

    fun uhrzeit(zeit: LocalTime): String = zeit.format(DateTimeFormatter.ofPattern("HH:mm"))
}
