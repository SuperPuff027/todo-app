import { epochTag, erinnerungFaellig, erinnerungsTitel, vorschau } from '../logik/kalender.ts';
import { offeneAmTag, uebertragen } from './aufgaben.ts';
import { leseErinnerung, leseLetzteErinnerung, setzeLetzteErinnerung } from './einstellungen.ts';

export const ERINNERUNG_TAG = 'erinnerung';

/**
 * Holt den Übertrag nach und zeigt die Benachrichtigung „Noch X Aufgaben offen“, wenn sie fällig ist.
 * Läuft in der Seite (solange sie offen ist) und im Service Worker (Periodic Background Sync).
 */
export async function pruefeErinnerung(registrierung: ServiceWorkerRegistration, jetzt = new Date()): Promise<void> {
  const heute = epochTag(jetzt);
  await uebertragen(heute);
  if (typeof Notification === 'undefined' || Notification.permission !== 'granted') return;

  const [uhrzeit, letzte, offen] = await Promise.all([leseErinnerung(), leseLetzteErinnerung(), offeneAmTag(heute)]);
  if (!erinnerungFaellig(jetzt, uhrzeit, letzte, offen.length)) return;

  // Zuerst merken, damit zwei gleichzeitige Prüfungen (Seite und Service Worker) nicht doppelt erinnern.
  await setzeLetzteErinnerung(heute);
  await registrierung.showNotification(erinnerungsTitel(offen.length), {
    body: vorschau(offen.map((a) => a.text)),
    tag: ERINNERUNG_TAG,
    icon: 'icon-192.png',
    badge: 'badge-96.png',
    lang: 'de',
  });
}
