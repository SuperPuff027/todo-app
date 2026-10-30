import { useEffect, useRef, useState } from 'preact/hooks';
import { zielText } from '../logik/kalender.ts';
import { IconMikrofon, IconPlus } from './icons.tsx';
import { spracheFehlertext, spracheVerfuegbar, starteAufnahme, type Aufnahme } from './sprache.ts';

/** Ab dieser Druckdauer gilt das Mikrofon als „gedrückt gehalten“: Loslassen beendet die Aufnahme. */
const HALTEN_AB_MS = 500;

interface Props {
  zielTag: number;
  heute: number;
  mitSprache: boolean;
  onSpeichern: (text: string) => void;
  onSchliessen: () => void;
  onHinweis: (text: string) => void;
}

const gross = (text: string) => text.charAt(0).toLocaleUpperCase('de') + text.slice(1);

/**
 * Eingabe als Blatt von unten. Enter speichert für den gewählten Tag; das Feld bleibt offen und leer,
 * so lassen sich mehrere Aufgaben hintereinander eintragen.
 * Mikrofon antippen: hört zu, bis man aufhört zu sprechen. Gedrückt halten: hört zu, bis man loslässt.
 * Gesprochenes erscheint schon beim Sprechen im Feld und wird erst mit Enter gespeichert.
 */
export function Eingabe({ zielTag, heute, mitSprache, onSpeichern, onSchliessen, onHinweis }: Props) {
  const [text, setText] = useState('');
  const [hoertZu, setHoertZu] = useState(false);
  const feld = useRef<HTMLInputElement>(null);
  const aufnahme = useRef<Aufnahme | null>(null);
  const druckBeginn = useRef(0);

  const zuhoeren = (vorher: string) => {
    if (!spracheVerfuegbar() || !navigator.onLine) {
      onHinweis('Ohne Internet: ins Feld tippen und das Mikrofon der Tastatur nutzen.');
      feld.current?.focus();
      return;
    }
    const basis = vorher.trim();
    aufnahme.current = starteAufnahme(
      (erkannt) => setText(gross(basis ? `${basis} ${erkannt}` : erkannt)),
      (fehler) => {
        aufnahme.current = null;
        setHoertZu(false);
        const hinweis = spracheFehlertext(fehler);
        if (hinweis) onHinweis(hinweis);
      },
    );
    setHoertZu(aufnahme.current !== null);
    if (!aufnahme.current) onHinweis('Spracherkennung ist hier nicht verfügbar');
  };

  useEffect(() => {
    if (mitSprache) zuhoeren('');
    else feld.current?.focus();
    return () => aufnahme.current?.abbrechen();
  }, []);

  const speichern = () => {
    // Sonst würde ein spätes Ergebnis der Aufnahme das geleerte Feld wieder füllen.
    aufnahme.current?.abbrechen();
    // Direkt aus dem Feld, falls Enter schneller kommt als das letzte Neuzeichnen.
    const aktuell = feld.current?.value ?? text;
    if (!aktuell.trim()) return;
    onSpeichern(aktuell);
    setText('');
    feld.current?.focus();
  };

  return (
    <div class="blatt-hintergrund" onClick={onSchliessen}>
      <form
        class="blatt"
        onClick={(e) => e.stopPropagation()}
        onSubmit={(e) => {
          e.preventDefault();
          speichern();
        }}
        onKeyDown={(e) => {
          if (e.key === 'Escape') onSchliessen();
        }}
      >
        <p class="ziel">{zielText(zielTag, heute)}</p>
        <div class="eingabe-zeile">
          <div class="feld">
            <input
              ref={feld}
              value={text}
              onInput={(e) => setText(e.currentTarget.value)}
              placeholder={hoertZu ? 'Ich höre zu …' : 'Neue Aufgabe'}
              enterKeyHint="done"
              autocapitalize="sentences"
              autocomplete="off"
              aria-label="Neue Aufgabe"
            />
            <button
              type="button"
              class={`mikro${hoertZu ? ' aktiv' : ''}`}
              aria-label={hoertZu ? 'Aufnahme beenden' : 'Per Sprache eintragen'}
              onPointerDown={(e) => {
                e.preventDefault();
                if (aufnahme.current) {
                  aufnahme.current.stoppen();
                  return;
                }
                druckBeginn.current = Date.now();
                zuhoeren(text);
              }}
              onPointerUp={() => {
                if (aufnahme.current && Date.now() - druckBeginn.current >= HALTEN_AB_MS) aufnahme.current.stoppen();
              }}
              onClick={(e) => {
                // Nur Tastatur-Bedienung (Enter/Leertaste); Maus und Finger laufen über die Pointer-Ereignisse.
                if (e.detail !== 0) return;
                if (aufnahme.current) aufnahme.current.stoppen();
                else zuhoeren(text);
              }}
            >
              <IconMikrofon />
            </button>
          </div>
          <button type="submit" class="senden" disabled={!text.trim()} aria-label="Speichern">
            <IconPlus />
          </button>
        </div>
        <button type="button" class="textknopf fertig" onClick={onSchliessen}>
          Fertig
        </button>
      </form>
    </div>
  );
}
