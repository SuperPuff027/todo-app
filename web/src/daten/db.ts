import Dexie, { type EntityTable } from 'dexie';
import { ANZAHL_FARBEN } from '../logik/farben.ts';

/**
 * Eine Aufgabe für einen bestimmten Tag. Felder wie in der Android-App.
 *
 * - `datum`: Epoch-Tag, an dem die Aufgabe aktuell steht; der Übertrag setzt ihn auf heute.
 * - `urspruengliches_datum`: Tag, für den sie geplant war; bleibt gleich. Ist `datum` größer, wurde sie übertragen.
 * - `erledigt`: 0/1 statt Boolean, weil IndexedDB Booleans nicht indizieren kann.
 *   Erledigte Aufgaben verschwinden aus allen Ansichten, bleiben aber gespeichert.
 * - `erstellt_am`: Zeitpunkt der Eingabe in ms, bleibt unverändert; legt die Reihenfolge und das Label fest.
 * - `farbe`: Index der Aufgabenfarbe (0 … ANZAHL_FARBEN-1), beim Anlegen vergeben (nur Web-App).
 */
export interface Aufgabe {
  id?: number;
  text: string;
  datum: number;
  urspruengliches_datum: number;
  erledigt: 0 | 1;
  erledigt_am: number | null;
  erstellt_am: number;
  farbe: number;
}

/** Kleine Einstellungen. In IndexedDB statt localStorage, damit auch der Service Worker sie lesen kann. */
export interface Einstellung {
  schluessel: string;
  wert: string | number | null;
}

export const db = new Dexie('todo') as Dexie & {
  aufgaben: EntityTable<Aufgabe, 'id'>;
  einstellungen: EntityTable<Einstellung, 'schluessel'>;
};

db.version(1).stores({
  aufgaben: '++id, [erledigt+datum]',
  einstellungen: 'schluessel',
});

// Version 2: Aufgabenfarben. Vorhandene Aufgaben bekommen der Reihe nach je eine eigene Farbe.
db.version(2)
  .stores({
    aufgaben: '++id, [erledigt+datum]',
    einstellungen: 'schluessel',
  })
  .upgrade(async (tx) => {
    let nummer = 0;
    await tx
      .table<Aufgabe, number>('aufgaben')
      .toCollection()
      .modify((aufgabe) => {
        aufgabe.farbe = nummer++ % ANZAHL_FARBEN;
      });
  });
