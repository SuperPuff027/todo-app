# ToDo

Minimale To-Do-App: heutige Aufgaben bildschirmfüllend, Eingabe per Text oder Sprache, Wochenübersicht per Pinch-Zoom, Erinnerung bei offenen Aufgaben und Übertrag um Mitternacht.

Es gibt zwei Fassungen mit derselben Bedienung:

- **Web-App** (`web/`): läuft im Browser, lässt sich als Icon auf den Startbildschirm legen und funktioniert offline.
- **Android-App** (`app/`): native App mit Homescreen-Widget und pünktlicher Erinnerung.

Beide speichern nur auf dem Gerät. Sie teilen keine Daten miteinander.

## Web-App installieren

1. Auf dem Handy in Chrome **https://superpuff027.github.io/todo-app/** öffnen.
2. Menü (⋮) → **Zum Startbildschirm hinzufügen** bzw. **App installieren**. Auf dem iPhone: Safari → Teilen → **Zum Home-Bildschirm**.
3. Ab jetzt über das Icon starten. Nach dem ersten Öffnen läuft die App auch ohne Internet.

Langes Drücken auf das Icon bietet „Neue Aufgabe“ und „Aufgabe sprechen“ an.

Grenzen gegenüber der Android-App:

- **Kein Widget:** Web-Apps können keine Homescreen-Widgets haben.
- **Erinnerung nicht minutengenau:** Bei geschlossener App kommt sie, wenn Android die App im Hintergrund weckt (meist alle paar Stunden), sonst beim nächsten Öffnen.
- **Spracheingabe braucht Internet:** Offline geht das Mikrofon der Tastatur.

## Android-App installieren

1. Auf dem Handy die [neueste APK](../../releases/download/neueste/ToDo.apk) herunterladen (Android 8.0 oder neuer).
2. Öffnen und installieren. Beim ersten Mal fragt Android, ob der Browser bzw. Dateimanager Apps installieren darf: erlauben.
3. Updates genauso: neue APK herunterladen und über die alte installieren. Die Aufgaben bleiben erhalten.

Das Widget fügst du über langes Drücken auf den Homescreen → Widgets → „ToDo heute“ hinzu.

## Bauen

- Push auf `main` mit Änderungen in `web/`: GitHub Action „Web-App auf GitHub Pages“ (Typecheck, Lint, Tests, Build, Veröffentlichen).
- Jeder andere Push auf `main`: GitHub Action „APK bauen“ (Unit-Tests, APK, Release `neueste`).
- Lokal für die Web-App: `cd web`, `npm install`, dann `npm run dev` bzw. `npm run check`.
