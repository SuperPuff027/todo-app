/** Anzahl der Aufgabenfarben (`--farbe-0` … `--farbe-9` in style.css). */
export const ANZAHL_FARBEN = 10;

/**
 * Farbe für eine neue Aufgabe: möglichst eine, die unter [belegt] (Farben der offenen Aufgaben
 * desselben Tages) noch nicht oder am seltensten vorkommt. Bei Gleichstand die nächste nach [letzte]
 * (Farbe der zuletzt angelegten Aufgabe), damit aufeinanderfolgende Aufgaben verschieden aussehen.
 */
export function naechsteFarbe(belegt: number[], letzte: number | null, anzahl = ANZAHL_FARBEN): number {
  const haeufigkeit = Array.from({ length: anzahl }, () => 0);
  for (const farbe of belegt) haeufigkeit[((farbe % anzahl) + anzahl) % anzahl]!++;
  const start = letzte === null ? 0 : (letzte + 1) % anzahl;
  let beste = start;
  for (let schritt = 0; schritt < anzahl; schritt++) {
    const farbe = (start + schritt) % anzahl;
    if (haeufigkeit[farbe]! < haeufigkeit[beste]!) beste = farbe;
  }
  return beste;
}

/** CSS-Wert für die Farbe einer Aufgabe. */
export const farbVariable = (farbe: number): string => `var(--farbe-${((farbe % ANZAHL_FARBEN) + ANZAHL_FARBEN) % ANZAHL_FARBEN})`;
