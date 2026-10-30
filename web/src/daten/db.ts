import Dexie, { type EntityTable } from 'dexie';

/**
 * Eine Aufgabe für einen bestimmten Tag. Felder wie in der Android-App.
 *
 * - `datum`: Epoch-Tag, an dem die Aufgabe aktuell steht; der Übertrag setzt ihn auf heute.
 * - `urspruengliches_datum`: Tag, für den sie geplant war; bleibt gleich. Ist `datum` größer, wurde sie übertragen.
 * - `erledigt`: 0/1 statt Boolean, weil IndexedDB Booleans nicht indizieren kann.
 *   Erledigte Aufgaben verschwinden aus allen Ansichten, bleiben aber gespeichert.
 * - `erstellt_am`: Zeitpunkt der Eingabe in ms, bleibt unverändert; legt die Reihenfolge und das Label fest.
 */
export interface Aufgabe {
  id?: number;
  text: string;
  datum: number;
  urspruengliches_datum: number;
  erledigt: 0 | 1;
  erledigt_am: number | null;
  erstellt_am: number;
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
