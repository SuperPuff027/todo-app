import type { ComponentChildren } from 'preact';
import type { Aufgabe } from '../daten/db.ts';
import { tagUndMonat, wochentag } from '../logik/kalender.ts';
import { AufgabeKarte } from './AufgabeKarte.tsx';
import { IconKalender } from './icons.tsx';

interface Props {
  tag: number;
  heute: number;
  aufgaben: Aufgabe[];
  erinnerung: string;
  onErledigen: (aufgabe: Aufgabe) => void;
  onWoche: () => void;
  onHeute: () => void;
  onErinnerung: () => void;
  /** Hinweise unter der Kopfzeile (Installieren, Benachrichtigungen). */
  hinweise?: ComponentChildren;
}

/** Die Liste eines Tages, bildschirmfüllend. */
export function TagesAnsicht({ tag, heute, aufgaben, erinnerung, onErledigen, onWoche, onHeute, onErinnerung, hinweise }: Props) {
  const istHeute = tag === heute;
  return (
    <section class="seite">
      <header class="kopf">
        <div class="kopf-text">
          <h1>{istHeute ? 'Heute' : wochentag(tag)}</h1>
          <p class="unterzeile">
            {istHeute ? `${wochentag(tag)}, ${tagUndMonat(tag)}` : tagUndMonat(tag)}
            {istHeute && (
              <>
                {' · '}
                <button class="textknopf" onClick={onErinnerung} aria-label={`Erinnerung um ${erinnerung} ändern`}>
                  Erinnerung {erinnerung}
                </button>
              </>
            )}
          </p>
        </div>
        <button class="rundknopf" onClick={onWoche} aria-label="Wochenübersicht">
          <IconKalender />
        </button>
      </header>

      {!istHeute && (
        <button class="pille" onClick={onHeute}>
          Zurück zu heute
        </button>
      )}
      {hinweise}

      {aufgaben.length === 0 ? (
        <div class="leer">
          <p class="leer-titel">{istHeute ? 'Nichts offen' : 'Noch nichts geplant'}</p>
          <p class="leer-text">
            {tag < heute ? 'Offenes von diesem Tag ist schon auf heute gewandert.' : 'Tippe auf + oder aufs Mikrofon.'}
          </p>
        </div>
      ) : (
        <ul class="liste">
          {aufgaben.map((aufgabe) => (
            <AufgabeKarte key={aufgabe.id} aufgabe={aufgabe} onErledigen={() => onErledigen(aufgabe)} />
          ))}
        </ul>
      )}
    </section>
  );
}
