import { describe, expect, it } from 'vitest';
import { farbVariable, naechsteFarbe } from './farben.ts';

describe('farben', () => {
  it('beginnt vorne und wechselt bei jeder neuen Aufgabe', () => {
    expect(naechsteFarbe([], null)).toBe(0);
    expect(naechsteFarbe([0], 0)).toBe(1);
    expect(naechsteFarbe([0, 1], 1)).toBe(2);
  });

  it('nimmt eine am Tag noch freie Farbe', () => {
    // Zuletzt Farbe 3 vergeben (anderer Tag); am Zieltag sind 4 und 5 belegt → 6
    expect(naechsteFarbe([4, 5], 3)).toBe(6);
    // Lücke nach dem Erledigen einer Aufgabe wird wieder genutzt
    expect(naechsteFarbe([0, 2, 3], 3, 4)).toBe(1);
  });

  it('verteilt gleichmäßig, wenn alle Farben vergeben sind', () => {
    const alle = [0, 1, 2, 3];
    expect(naechsteFarbe(alle, 3, 4)).toBe(0);
    expect(naechsteFarbe([...alle, 0], 0, 4)).toBe(1);
  });

  it('nie zwei gleiche Farben, solange es freie gibt', () => {
    const belegt: number[] = [];
    let letzte: number | null = null;
    for (let i = 0; i < 10; i++) {
      letzte = naechsteFarbe(belegt, letzte);
      belegt.push(letzte);
    }
    expect(new Set(belegt).size).toBe(10);
  });

  it('liefert die CSS-Variable', () => {
    expect(farbVariable(3)).toBe('var(--farbe-3)');
    expect(farbVariable(13)).toBe('var(--farbe-3)');
  });
});
