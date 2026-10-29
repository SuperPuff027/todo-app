# To-Do-App

Persönliche To-Do-App für ein Android-Handy. Nur ein Nutzer, kein Konto, kein Sync, kein Store. Native App (nicht PWA wie CJ6-Repair und olivenernte-app), weil Homescreen-Widget und Erinnerung bei geschlossener App gebraucht werden.

Projekt-Brief: Claude-Docs-Dokument „Project Brief: To-Do-App (Android)“, https://claude.ai/artifact/Xe7eGJe25Zo3t4SKn27V1u (nur über den Docs-Connector lesen).

## Arbeitsweise
- Antworten an den Nutzer auf Deutsch. Code-Bezeichner deutsch, wie in den anderen Apps.
- Kein lokales Android-SDK nötig: Die GitHub Action baut bei jedem Push auf `main`, führt die Tests aus und hängt die APK an das Release `neueste`.
- Nach jedem Push warten, bis die Action grün ist.
- Reine Logik (Datum, Texte, Übertrag-Regeln) liegt in `logik/` ohne Android-Abhängigkeiten und hat JUnit-Tests.
- Keine neuen Abhängigkeiten außerhalb des Stacks ohne Rückfrage.
- Commits nur mit der GitHub-noreply-Adresse.

## Stack
- Kotlin, Jetpack Compose, Material 3, minSdk 26, targetSdk 35
- Room (SQLite) lokal, Jetpack Glance für das Widget
- Spracheingabe über `RecognizerIntent` (System-Spracherkennung)
- AlarmManager (`setAndAllowWhileIdle`) für Erinnerung und Mitternachts-Übertrag
- Signatur: fester Schlüssel in `app/signatur/`, damit jede APK über die vorige installiert werden kann

## Fachliche Regeln
- Tage als Epoch-Tag (`LocalDate.toEpochDay()`).
- Unerledigte Aufgaben wandern um Mitternacht auf heute (`datum` ändert sich, `urspruengliches_datum` nicht). Übertrag auch bei App-Start, Neustart und vor jeder Erinnerung.
- Label „seit X Tagen offen“ = `datum - urspruengliches_datum`.
- Erledigte Aufgaben verschwinden aus allen Ansichten, bleiben aber gespeichert (`erledigt`, `erledigt_am`).
- Erinnerung (Standard 18:00, einstellbar) nur, wenn heute noch etwas offen ist.
- Design: minimal, keine eckigen Kanten, eine Akzentfarbe, Dark Mode nach System.

## Getroffene Standards für offene Fragen im Brief
- Eine Erinnerung pro Tag zur eingestellten Uhrzeit.
- Erledigte Aufgaben werden ausgeblendet, nicht gelöscht.
- Wochenübersicht Montag bis Sonntag, mit Pfeilen in andere Wochen blätterbar.
- Spracheingabe: eine Aufgabe pro Aufnahme; der erkannte Text ist vor dem Speichern editierbar.
