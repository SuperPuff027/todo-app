/**
 * Reine Datums- und Textlogik ohne Browser-Abhängigkeiten, damit sie mit Vitest testbar ist.
 * Tage sind Epoch-Tage (Tage seit 1970-01-01 in lokaler Zeit), wie in der Android-App.
 */

const MS_PRO_TAG = 86_400_000;
const SPRACHE = 'de-DE';

/** Epoch-Tag des lokalen Kalenderdatums von [zeitpunkt]. */
export function epochTag(zeitpunkt: Date): number {
  return Math.round(Date.UTC(zeitpunkt.getFullYear(), zeitpunkt.getMonth(), zeitpunkt.getDate()) / MS_PRO_TAG);
}

/** Lokales Datum (00:00) zu einem Epoch-Tag. */
export function datumVon(tag: number): Date {
  const utc = new Date(tag * MS_PRO_TAG);
  return new Date(utc.getUTCFullYear(), utc.getUTCMonth(), utc.getUTCDate());
}

/** Montag der Woche, in der [tag] liegt. Der 1.1.1970 war ein Donnerstag. */
export function wochenStart(tag: number): number {
  const seitMontag = (((tag + 3) % 7) + 7) % 7;
  return tag - seitMontag;
}

/** Die sieben Tage Montag bis Sonntag ab [montag]. */
export function wocheAb(montag: number): number[] {
  return Array.from({ length: 7 }, (_, i) => montag + i);
}

/** ISO-Kalenderwoche (Woche 1 enthält den ersten Donnerstag des Jahres). */
export function kalenderwoche(tag: number): number {
  const donnerstag = wochenStart(tag) + 3;
  const jahr = datumVon(donnerstag).getFullYear();
  const ersterJanuar = epochTag(new Date(jahr, 0, 1));
  return Math.floor((donnerstag - ersterJanuar) / 7) + 1;
}

/**
 * Tage für das Label „seit X Tagen offen“: Abstand vom Eingabetag (`erstellt_am`) bis zum aktuellen Tag
 * der Aufgabe (nach dem Übertrag ist das heute). 0, solange die Aufgabe noch an ihrem geplanten Tag steht,
 * damit vorausgeplante Aufgaben kein Label bekommen.
 */
export function tageOffen(datum: number, urspruenglichesDatum: number, erstelltAm: number): number {
  if (datum <= urspruenglichesDatum) return 0;
  return Math.max(1, datum - epochTag(new Date(erstelltAm)));
}

/** Label für übertragene Aufgaben, oder `null`, wenn es keins gibt. */
export function offenLabel(tage: number): string | null {
  if (tage <= 0) return null;
  return tage === 1 ? 'seit 1 Tag offen' : `seit ${tage} Tagen offen`;
}

export function wochentag(tag: number): string {
  return datumVon(tag).toLocaleDateString(SPRACHE, { weekday: 'long' });
}

/** z. B. „29. Oktober“ */
export function tagUndMonat(tag: number): string {
  return datumVon(tag).toLocaleDateString(SPRACHE, { day: 'numeric', month: 'long' });
}

/** z. B. „29.10.“ */
export function tagUndMonatKurz(tag: number): string {
  const d = datumVon(tag);
  return `${d.getDate()}.${d.getMonth() + 1}.`;
}

/** z. B. „26. Okt. – 1. Nov.“ */
export function wochenspanne(montag: number): string {
  const format = (tag: number) => datumVon(tag).toLocaleDateString(SPRACHE, { day: 'numeric', month: 'short' });
  return `${format(montag)} – ${format(montag + 6)}`;
}

/** Hinweis über dem Eingabefeld, für welchen Tag eingetragen wird. */
export function zielText(tag: number, heute: number): string {
  if (tag === heute) return 'Für heute';
  if (tag === heute + 1) return 'Für morgen';
  return `Für ${wochentag(tag)}, ${tagUndMonat(tag)}`;
}

export function erinnerungsTitel(anzahl: number): string {
  return anzahl === 1 ? 'Noch 1 Aufgabe offen' : `Noch ${anzahl} Aufgaben offen`;
}

/** Bis zu [maximal] Aufgabentexte, mit „…“, wenn es mehr gibt. */
export function vorschau(texte: string[], maximal = 3): string {
  const gezeigt = texte.slice(0, maximal).join(', ');
  return texte.length > maximal ? `${gezeigt}, …` : gezeigt;
}

/** „HH:MM“ in Minuten seit Mitternacht; ungültige Angaben ergeben `null`. */
export function minutenVon(uhrzeit: string): number | null {
  const treffer = /^(\d{1,2}):(\d{2})$/.exec(uhrzeit.trim());
  if (!treffer) return null;
  const stunde = Number(treffer[1]);
  const minute = Number(treffer[2]);
  if (stunde > 23 || minute > 59) return null;
  return stunde * 60 + minute;
}

/**
 * Ist jetzt eine Erinnerung fällig? Ja, wenn die eingestellte Uhrzeit heute erreicht ist,
 * heute noch etwas offen ist und heute noch nicht erinnert wurde.
 */
export function erinnerungFaellig(jetzt: Date, uhrzeit: string, letzteErinnerung: number | null, anzahlOffen: number): boolean {
  const ziel = minutenVon(uhrzeit);
  if (ziel === null || anzahlOffen <= 0) return false;
  if (letzteErinnerung === epochTag(jetzt)) return false;
  return jetzt.getHours() * 60 + jetzt.getMinutes() >= ziel;
}
