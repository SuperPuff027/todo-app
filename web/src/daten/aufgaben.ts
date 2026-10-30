import Dexie from 'dexie';
import { naechsteFarbe } from '../logik/farben.ts';
import { db, type Aufgabe } from './db.ts';

const nachEingabe = (a: Aufgabe, b: Aufgabe) => a.erstellt_am - b.erstellt_am || (a.id ?? 0) - (b.id ?? 0);

/** Legt eine Aufgabe an, mit einer Farbe, die sich von den anderen offenen Aufgaben des Tages unterscheidet. */
export async function hinzufuegen(text: string, tag: number, jetzt = Date.now()): Promise<void> {
  const bereinigt = text.trim();
  if (!bereinigt) return;
  await db.transaction('rw', db.aufgaben, async () => {
    const amTag = await db.aufgaben.where('[erledigt+datum]').equals([0, tag]).toArray();
    const zuletzt = await db.aufgaben.orderBy(':id').last();
    await db.aufgaben.add({
      text: bereinigt.charAt(0).toLocaleUpperCase('de') + bereinigt.slice(1),
      datum: tag,
      urspruengliches_datum: tag,
      erledigt: 0,
      erledigt_am: null,
      erstellt_am: jetzt,
      farbe: naechsteFarbe(
        amTag.map((a) => a.farbe),
        zuletzt?.farbe ?? null,
      ),
    });
  });
}

export async function erledigen(id: number, jetzt = Date.now()): Promise<void> {
  await db.aufgaben.update(id, { erledigt: 1, erledigt_am: jetzt });
}

export async function rueckgaengig(id: number): Promise<void> {
  await db.aufgaben.update(id, { erledigt: 0, erledigt_am: null });
}

export async function offeneAmTag(tag: number): Promise<Aufgabe[]> {
  const liste = await db.aufgaben.where('[erledigt+datum]').equals([0, tag]).toArray();
  return liste.sort(nachEingabe);
}

/** Offene Aufgaben von [von] bis einschließlich [bis], nach Tag und Eingabe sortiert. */
export async function offeneZwischen(von: number, bis: number): Promise<Aufgabe[]> {
  const liste = await db.aufgaben.where('[erledigt+datum]').between([0, von], [0, bis], true, true).toArray();
  return liste.sort((a, b) => a.datum - b.datum || nachEingabe(a, b));
}

/**
 * Legt alle offenen Aufgaben vergangener Tage auf [heute]. `urspruengliches_datum` und `erstellt_am`
 * bleiben unverändert. Läuft bei jedem Start, beim Datumswechsel und vor jeder Erinnerung.
 * Gibt die Anzahl übertragener Aufgaben zurück.
 */
export async function uebertragen(heute: number): Promise<number> {
  return db.aufgaben.where('[erledigt+datum]').between([0, Dexie.minKey], [0, heute], true, false).modify({ datum: heute });
}
