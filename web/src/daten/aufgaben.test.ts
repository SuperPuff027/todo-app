import 'fake-indexeddb/auto';
import { beforeEach, describe, expect, it } from 'vitest';
import { db } from './db.ts';
import { erledigen, hinzufuegen, offeneAmTag, offeneZwischen, rueckgaengig, uebertragen } from './aufgaben.ts';

const MONTAG = 20_752; // 26.10.2026

beforeEach(async () => {
  await db.aufgaben.clear();
});

describe('aufgaben', () => {
  it('speichert bereinigt, mit großem Anfangsbuchstaben, und ignoriert Leeres', async () => {
    await hinzufuegen('  äpfel kaufen ', MONTAG, 1);
    await hinzufuegen('   ', MONTAG, 2);
    const liste = await offeneAmTag(MONTAG);
    expect(liste.map((a) => a.text)).toEqual(['Äpfel kaufen']);
    expect(liste[0]?.urspruengliches_datum).toBe(MONTAG);
  });

  it('blendet Erledigtes aus und holt es mit Rückgängig zurück', async () => {
    await hinzufuegen('A', MONTAG, 1);
    await hinzufuegen('B', MONTAG, 2);
    const [a] = await offeneAmTag(MONTAG);
    await erledigen(a!.id!);
    expect((await offeneAmTag(MONTAG)).map((x) => x.text)).toEqual(['B']);
    expect(await db.aufgaben.count()).toBe(2);
    await rueckgaengig(a!.id!);
    expect((await offeneAmTag(MONTAG)).map((x) => x.text)).toEqual(['A', 'B']);
  });

  it('überträgt nur offene Aufgaben vergangener Tage auf heute', async () => {
    await hinzufuegen('Alt offen', MONTAG, 1);
    await hinzufuegen('Alt erledigt', MONTAG, 2);
    await hinzufuegen('Heute geplant', MONTAG + 3, 3);
    await hinzufuegen('Zukunft', MONTAG + 5, 4);
    const erledigt = (await offeneAmTag(MONTAG)).find((a) => a.text === 'Alt erledigt')!;
    await erledigen(erledigt.id!);

    expect(await uebertragen(MONTAG + 3)).toBe(1);

    const heute = await offeneAmTag(MONTAG + 3);
    // Übertragene stehen zusammen mit den geplanten, in Reihenfolge der Eingabe
    expect(heute.map((a) => a.text)).toEqual(['Alt offen', 'Heute geplant']);
    expect(heute[0]?.urspruengliches_datum).toBe(MONTAG);
    expect((await offeneAmTag(MONTAG + 5)).map((a) => a.text)).toEqual(['Zukunft']);
    expect((await db.aufgaben.get(erledigt.id!))?.datum).toBe(MONTAG);
    // Zweiter Lauf ändert nichts
    expect(await uebertragen(MONTAG + 3)).toBe(0);
  });

  it('gibt jeder neuen Aufgabe eines Tages eine andere Farbe', async () => {
    for (const text of ['A', 'B', 'C', 'D']) await hinzufuegen(text, MONTAG);
    const farben = (await offeneAmTag(MONTAG)).map((a) => a.farbe);
    expect(new Set(farben).size).toBe(4);
    // Nach dem Erledigen wird die frei gewordene Farbe wieder genutzt, statt eine doppelt zu vergeben
    const [erste] = await offeneAmTag(MONTAG);
    await erledigen(erste!.id!);
    await hinzufuegen('E', MONTAG);
    const danach = (await offeneAmTag(MONTAG)).map((a) => a.farbe);
    expect(new Set(danach).size).toBe(4);
  });

  it('liefert eine Woche nach Tag sortiert', async () => {
    await hinzufuegen('Mi', MONTAG + 2, 1);
    await hinzufuegen('Mo', MONTAG, 2);
    await hinzufuegen('Nächste Woche', MONTAG + 7, 3);
    expect((await offeneZwischen(MONTAG, MONTAG + 6)).map((a) => a.text)).toEqual(['Mo', 'Mi']);
  });
});
