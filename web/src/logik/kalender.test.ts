import { describe, expect, it } from 'vitest';
import {
  datumVon,
  epochTag,
  erinnerungFaellig,
  erinnerungsTitel,
  kalenderwoche,
  minutenVon,
  offenLabel,
  tagUndMonatKurz,
  tageOffen,
  vorschau,
  wocheAb,
  wochenStart,
  wochentag,
  zielText,
} from './kalender.ts';

const tag = (jahr: number, monat: number, t: number) => epochTag(new Date(jahr, monat - 1, t));

describe('kalender', () => {
  it('rechnet Epoch-Tage hin und zurück', () => {
    const donnerstag = tag(2026, 10, 29);
    expect(datumVon(donnerstag).getDate()).toBe(29);
    expect(datumVon(donnerstag).getMonth()).toBe(9);
    expect(tag(1970, 1, 1)).toBe(0);
  });

  it('beginnt Wochen am Montag', () => {
    expect(wochenStart(tag(2026, 10, 29))).toBe(tag(2026, 10, 26));
    expect(wochenStart(tag(2026, 10, 26))).toBe(tag(2026, 10, 26));
    // Sonntag gehört zur Woche davor
    expect(wochenStart(tag(2026, 11, 1))).toBe(tag(2026, 10, 26));
    expect(wocheAb(tag(2026, 10, 26)).at(-1)).toBe(tag(2026, 11, 1));
  });

  it('zählt Kalenderwochen nach ISO', () => {
    expect(kalenderwoche(tag(2026, 10, 29))).toBe(44);
    expect(kalenderwoche(tag(2027, 1, 1))).toBe(53);
    expect(kalenderwoche(tag(2026, 1, 1))).toBe(1);
  });

  it('zählt „seit X Tagen offen“ ab erstellt_am, nur bei übertragenen Aufgaben', () => {
    const montag = tag(2026, 10, 26);
    const erstellt = new Date(2026, 9, 26, 21, 30).getTime();
    expect(tageOffen(montag, montag, erstellt)).toBe(0);
    expect(tageOffen(montag + 3, montag, erstellt)).toBe(3);
    // Am Montag für Mittwoch geplant, am Freitag noch offen: zählt ab Eintrag
    expect(tageOffen(montag + 4, montag + 2, erstellt)).toBe(4);
    expect(tageOffen(montag + 2, montag + 2, erstellt)).toBe(0);
  });

  it('formuliert Labels und Texte', () => {
    expect(offenLabel(0)).toBeNull();
    expect(offenLabel(1)).toBe('seit 1 Tag offen');
    expect(offenLabel(3)).toBe('seit 3 Tagen offen');
    expect(wochentag(tag(2026, 10, 29))).toBe('Donnerstag');
    expect(tagUndMonatKurz(tag(2026, 10, 29))).toBe('29.10.');
    const heute = tag(2026, 10, 29);
    expect(zielText(heute, heute)).toBe('Für heute');
    expect(zielText(heute + 1, heute)).toBe('Für morgen');
    expect(zielText(heute + 2, heute)).toBe('Für Samstag, 31. Oktober');
    expect(erinnerungsTitel(1)).toBe('Noch 1 Aufgabe offen');
    expect(erinnerungsTitel(4)).toBe('Noch 4 Aufgaben offen');
    expect(vorschau(['A', 'B'])).toBe('A, B');
    expect(vorschau(['A', 'B', 'C', 'D'])).toBe('A, B, C, …');
  });

  it('liest Uhrzeiten', () => {
    expect(minutenVon('18:00')).toBe(1080);
    expect(minutenVon('7:05')).toBe(425);
    expect(minutenVon('24:00')).toBeNull();
    expect(minutenVon('abc')).toBeNull();
  });

  it('erinnert nur ab der Uhrzeit, nur bei Offenem und nur einmal am Tag', () => {
    const vorher = new Date(2026, 9, 29, 17, 59);
    const danach = new Date(2026, 9, 29, 18, 0);
    const heute = epochTag(danach);
    expect(erinnerungFaellig(vorher, '18:00', null, 2)).toBe(false);
    expect(erinnerungFaellig(danach, '18:00', null, 2)).toBe(true);
    expect(erinnerungFaellig(danach, '18:00', null, 0)).toBe(false);
    expect(erinnerungFaellig(danach, '18:00', heute, 2)).toBe(false);
    expect(erinnerungFaellig(danach, '18:00', heute - 1, 2)).toBe(true);
  });
});
