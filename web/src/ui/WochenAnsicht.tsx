import type { Aufgabe } from '../daten/db.ts';
import { farbVariable } from '../logik/farben.ts';
import { kalenderwoche, tagUndMonatKurz, wocheAb, wochenStart as wochenStartVon, wochenspanne, wochentag } from '../logik/kalender.ts';

interface Props {
  wochenStart: number;
  heute: number;
  aufgaben: Aufgabe[];
  onTag: (tag: number) => void;
  onVerschieben: (wochen: number) => void;
  onHeute: () => void;
}

/** Sieben abgerundete Tageskacheln, Montag bis Sonntag, je mit Anzahl offener Aufgaben. */
export function WochenAnsicht({ wochenStart, heute, aufgaben, onTag, onVerschieben, onHeute }: Props) {
  const istDieseWoche = wochenStart === wochenStartVon(heute);
  return (
    <section class="seite">
      <header class="kopf">
        <div class="kopf-text">
          <h1>Woche {kalenderwoche(wochenStart)}</h1>
          <p class="unterzeile">{wochenspanne(wochenStart)}</p>
        </div>
        <button class="rundknopf pfeil" onClick={() => onVerschieben(-1)} aria-label="Vorige Woche">
          ‹
        </button>
        <button class="rundknopf pfeil" onClick={() => onVerschieben(1)} aria-label="Nächste Woche">
          ›
        </button>
      </header>

      <div class="pillen">
        <button class="pille" onClick={onHeute}>
          Heute
        </button>
        {!istDieseWoche && <span class="leise">Andere Woche</span>}
      </div>
      <p class="leise abstand">Tippe auf einen Tag, um ihn zu planen</p>

      <ul class="raster">
        {wocheAb(wochenStart).map((tag) => {
          const offen = aufgaben.filter((a) => a.datum === tag);
          const istHeute = tag === heute;
          return (
            <li key={tag}>
              <button
                class={`kachel${istHeute ? ' heute' : ''}${tag < heute ? ' vergangen' : ''}`}
                onClick={() => onTag(tag)}
                aria-label={`${wochentag(tag)}, ${offen.length} offen`}
              >
                <span class="kachel-kopf">
                  <span>
                    <span class="kachel-tag">{istHeute ? 'Heute' : wochentag(tag)}</span>
                    <span class="kachel-datum">{tagUndMonatKurz(tag)}</span>
                  </span>
                  {offen.length > 0 && <span class="zahl">{offen.length}</span>}
                </span>
                {offen.slice(0, 3).map((a) => (
                  <span key={a.id} class="kachel-zeile" style={`--k: ${farbVariable(a.farbe)}`}>
                    {a.text}
                  </span>
                ))}
                {offen.length > 3 && <span class="kachel-mehr">+ {offen.length - 3} weitere</span>}
              </button>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
