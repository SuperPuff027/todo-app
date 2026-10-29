package de.superpuff.todo.logik

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class KalenderTest {

    @Test
    fun wochenStartIstMontag() {
        // Donnerstag, 29.10.2026
        assertEquals(LocalDate.of(2026, 10, 26), Kalender.wochenStart(LocalDate.of(2026, 10, 29)))
        // Montag bleibt Montag
        assertEquals(LocalDate.of(2026, 10, 26), Kalender.wochenStart(LocalDate.of(2026, 10, 26)))
        // Sonntag gehört zur Woche davor
        assertEquals(LocalDate.of(2026, 10, 26), Kalender.wochenStart(LocalDate.of(2026, 11, 1)))
    }

    @Test
    fun wocheHatSiebenTageVonMontagBisSonntag() {
        val woche = Kalender.wocheAb(LocalDate.of(2026, 10, 26))
        assertEquals(7, woche.size)
        assertEquals(LocalDate.of(2026, 11, 1), woche.last())
    }

    @Test
    fun kalenderwocheNachIso() {
        assertEquals(44, Kalender.kalenderwoche(LocalDate.of(2026, 10, 29)))
        assertEquals(53, Kalender.kalenderwoche(LocalDate.of(2027, 1, 1)))
    }

    @Test
    fun tageOffenNieNegativ() {
        assertEquals(0, Kalender.tageOffen(100, 100))
        assertEquals(3, Kalender.tageOffen(103, 100))
        assertEquals(0, Kalender.tageOffen(99, 100))
    }

    @Test
    fun labelNurBeiUebertragenenAufgaben() {
        assertNull(Kalender.offenLabel(0))
        assertEquals("seit 1 Tag offen", Kalender.offenLabel(1))
        assertEquals("seit 3 Tagen offen", Kalender.offenLabel(3))
    }

    @Test
    fun naechsteErinnerungHeuteOderMorgen() {
        val achtzehn = LocalTime.of(18, 0)
        assertEquals(
            LocalDateTime.of(2026, 10, 29, 18, 0),
            Kalender.naechsterZeitpunkt(LocalDateTime.of(2026, 10, 29, 9, 30), achtzehn),
        )
        assertEquals(
            LocalDateTime.of(2026, 10, 30, 18, 0),
            Kalender.naechsterZeitpunkt(LocalDateTime.of(2026, 10, 29, 18, 0), achtzehn),
        )
        assertEquals(
            LocalDateTime.of(2026, 10, 30, 18, 0),
            Kalender.naechsterZeitpunkt(LocalDateTime.of(2026, 10, 29, 22, 15), achtzehn),
        )
    }

    @Test
    fun zieltextFuerEingabe() {
        val heute = LocalDate.of(2026, 10, 29)
        assertEquals("Für heute", Kalender.zielText(heute, heute))
        assertEquals("Für morgen", Kalender.zielText(heute.plusDays(1), heute))
        assertEquals("Für Samstag, 31. Oktober", Kalender.zielText(heute.plusDays(2), heute))
    }

    @Test
    fun erinnerungstexte() {
        assertEquals("Noch 1 Aufgabe offen", Kalender.erinnerungsTitel(1))
        assertEquals("Noch 4 Aufgaben offen", Kalender.erinnerungsTitel(4))
        assertEquals("A, B", Kalender.vorschau(listOf("A", "B")))
        assertEquals("A, B, C, …", Kalender.vorschau(listOf("A", "B", "C", "D")))
    }
}
