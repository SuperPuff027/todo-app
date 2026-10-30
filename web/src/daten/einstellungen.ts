import { db } from './db.ts';

export const STANDARD_ERINNERUNG = '18:00';

const ERINNERUNG = 'erinnerung';
const LETZTE_ERINNERUNG = 'letzte_erinnerung';

export async function leseErinnerung(): Promise<string> {
  const eintrag = await db.einstellungen.get(ERINNERUNG);
  return typeof eintrag?.wert === 'string' ? eintrag.wert : STANDARD_ERINNERUNG;
}

export async function setzeErinnerung(uhrzeit: string): Promise<void> {
  await db.einstellungen.put({ schluessel: ERINNERUNG, wert: uhrzeit });
}

/** Epoch-Tag, an dem zuletzt erinnert wurde (höchstens eine Erinnerung pro Tag). */
export async function leseLetzteErinnerung(): Promise<number | null> {
  const eintrag = await db.einstellungen.get(LETZTE_ERINNERUNG);
  return typeof eintrag?.wert === 'number' ? eintrag.wert : null;
}

export async function setzeLetzteErinnerung(tag: number): Promise<void> {
  await db.einstellungen.put({ schluessel: LETZTE_ERINNERUNG, wert: tag });
}
