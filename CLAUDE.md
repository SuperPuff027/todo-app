# ToDo (Repo todo-app)

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
- Spracheingabe direkt in der App über `SpeechRecognizer` (Mikrofon antippen oder gedrückt halten, Zwischenergebnisse live im Feld); ohne Mikrofon-Erlaubnis oder Erkennungsdienst Rückfall auf den Systemdialog (`RecognizerIntent`)
- AlarmManager (`setAndAllowWhileIdle`) für Erinnerung und Mitternachts-Übertrag
- Signatur: fester Schlüssel in `app/signatur/`, damit jede APK über die vorige installiert werden kann

## Fachliche Regeln
- Tage als Epoch-Tag (`LocalDate.toEpochDay()`).
- Unerledigte Aufgaben wandern um Mitternacht auf heute (`datum` ändert sich, `urspruengliches_datum` nicht). Übertrag auch bei App-Start, Neustart und vor jeder Erinnerung.
- Label „seit X Tagen offen“ nur bei übertragenen Aufgaben (`datum > urspruengliches_datum`); X = `datum` minus Tag von `erstellt_am` (wie im Brief).
- Erledigte Aufgaben verschwinden aus allen Ansichten, bleiben aber gespeichert (`erledigt`, `erledigt_am`).
- Erinnerung (Standard 18:00, einstellbar) nur, wenn heute noch etwas offen ist.
- Design: minimal, keine eckigen Kanten, eine Akzentfarbe, Dark Mode nach System.

## Web-App (`web/`)
Zweite Fassung als offline-fähige PWA, weil die APK auf dem Handy des Nutzers nicht installierbar war. Gleiche fachliche Regeln und Feldnamen wie die Android-App.
- Stack wie olivenernte-app: Vite + TypeScript (strict), Preact, Dexie (IndexedDB), `vite-plugin-pwa` mit eigenem Service Worker (`src/sw.ts`, injectManifest), Vitest. `npm run check` = Typecheck + Lint + Tests.
- Reine Logik in `web/src/logik/` mit Tests; Datenzugriff in `web/src/daten/` (Tests mit fake-indexeddb).
- Einstellungen liegen in IndexedDB (nicht localStorage), damit der Service Worker sie lesen kann.
- Veröffentlicht über `.github/workflows/pages.yml` auf https://superpuff027.github.io/todo-app/ (nur bei Änderungen in `web/`). Die APK-Action ignoriert `web/`.
- Grenzen: kein Widget (Ersatz: App-Verknüpfungen im Manifest); Erinnerung im Hintergrund nur über Periodic Background Sync (Chrome, installierte App, Takt bestimmt Android), sonst beim Öffnen; Spracheingabe über Web Speech API braucht Netz, offline Hinweis auf Tastatur-Mikrofon.
- Aufgabenfarben (Wunsch des Nutzers): jede Aufgabe hat `farbe` (0–9), beim Anlegen so gewählt, dass sie sich von den offenen Aufgaben des Tages unterscheidet (`logik/farben.ts`). Darstellung: dünne Umrandung + durchscheinende Füllung (14 %). Palette `--farbe-0…9` in `style.css`, je eine helle und dunkle Variante. Nur in der Web-App, nicht in der Android-App.
- Darstellung umschaltbar (Automatisch → Hell → Dunkel) über den Knopf in der Tagesansicht; `data-theme` am `<html>`, Wahl in localStorage. Kein `light-dark()` (zu neu für ältere Android-Browser).
- Icons erzeugt `npm run icons` (`web/skripte/icons.mjs`).
- Lokal unter Git Bash `BASE_PATH` nicht setzen (MSYS macht daraus einen Windows-Pfad); in CI setzt der Workflow ihn.

## Getroffene Standards für offene Fragen im Brief
- Eine Erinnerung pro Tag zur eingestellten Uhrzeit.
- Erledigte Aufgaben werden ausgeblendet, nicht gelöscht.
- Wochenübersicht Montag bis Sonntag, mit Pfeilen in andere Wochen blätterbar.
- Spracheingabe: eine Aufgabe pro Aufnahme; der erkannte Text ist vor dem Speichern editierbar.
