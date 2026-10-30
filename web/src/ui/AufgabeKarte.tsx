import { useState } from 'preact/hooks';
import type { Aufgabe } from '../daten/db.ts';
import { offenLabel, tageOffen } from '../logik/kalender.ts';
import { IconHaken } from './icons.tsx';

/** Eine Aufgabe als abgerundete Karte. Tipp auf den runden Haken: kurz gefüllter Haken, dann gleitet die Karte weg. */
export function AufgabeKarte({ aufgabe, onErledigen }: { aufgabe: Aufgabe; onErledigen: () => void }) {
  const [zustand, setZustand] = useState<'offen' | 'abgehakt' | 'weg'>('offen');
  const label = offenLabel(tageOffen(aufgabe.datum, aufgabe.urspruengliches_datum, aufgabe.erstellt_am));

  const abhaken = () => {
    if (zustand !== 'offen') return;
    setZustand('abgehakt');
    setTimeout(() => setZustand('weg'), 260);
    setTimeout(onErledigen, 520);
  };

  return (
    <li class={`karte-huelle ${zustand}`}>
      <div class="karte">
        <button
          class="haken"
          role="checkbox"
          aria-checked={zustand !== 'offen'}
          aria-label={`Erledigt: ${aufgabe.text}`}
          onClick={abhaken}
        >
          <span class="kreis">
            <IconHaken />
          </span>
        </button>
        <div class="karte-text">
          <p>{aufgabe.text}</p>
          {label && <span class="label">{label}</span>}
        </div>
      </div>
    </li>
  );
}
