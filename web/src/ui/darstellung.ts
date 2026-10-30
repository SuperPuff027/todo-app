/** Hell, dunkel oder automatisch nach Systemeinstellung. Nur eine Bequemlichkeit pro Gerät, daher localStorage. */
export type Darstellung = 'auto' | 'hell' | 'dunkel';

const SCHLUESSEL = 'darstellung';
const REIHENFOLGE: Darstellung[] = ['auto', 'hell', 'dunkel'];

export const DARSTELLUNG_NAME: Record<Darstellung, string> = {
  auto: 'Automatisch',
  hell: 'Hell',
  dunkel: 'Dunkel',
};

export function leseDarstellung(): Darstellung {
  try {
    const wert = localStorage.getItem(SCHLUESSEL);
    return wert === 'hell' || wert === 'dunkel' ? wert : 'auto';
  } catch {
    return 'auto';
  }
}

export const naechsteDarstellung = (jetzt: Darstellung): Darstellung =>
  REIHENFOLGE[(REIHENFOLGE.indexOf(jetzt) + 1) % REIHENFOLGE.length]!;

/** Setzt `data-theme` (die Farben in style.css folgen über `color-scheme`) und merkt sich die Wahl. */
export function wendeDarstellungAn(darstellung: Darstellung): void {
  const html = document.documentElement;
  if (darstellung === 'auto') delete html.dataset.theme;
  else html.dataset.theme = darstellung === 'hell' ? 'light' : 'dark';

  // Statusleiste des Handys passend einfärben.
  const hintergrund = getComputedStyle(document.body).backgroundColor;
  document.querySelectorAll('meta[name="theme-color"]').forEach((meta) => meta.setAttribute('content', hintergrund));

  try {
    if (darstellung === 'auto') localStorage.removeItem(SCHLUESSEL);
    else localStorage.setItem(SCHLUESSEL, darstellung);
  } catch {
    // Ohne Speicher gilt die Wahl nur bis zum Schließen.
  }
}
