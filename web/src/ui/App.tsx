import { useEffect, useRef, useState } from 'preact/hooks';
import { erledigen, hinzufuegen, offeneAmTag, offeneZwischen, rueckgaengig, uebertragen } from '../daten/aufgaben.ts';
import type { Aufgabe } from '../daten/db.ts';
import { leseErinnerung, setzeErinnerung, STANDARD_ERINNERUNG } from '../daten/einstellungen.ts';
import { ERINNERUNG_TAG, pruefeErinnerung } from '../daten/erinnerung.ts';
import { epochTag, wochenStart as wochenStartVon } from '../logik/kalender.ts';
import { DARSTELLUNG_NAME, leseDarstellung, naechsteDarstellung, wendeDarstellungAn, type Darstellung } from './darstellung.ts';
import { Eingabe } from './Eingabe.tsx';
import { ErinnerungDialog } from './ErinnerungDialog.tsx';
import { IconMikrofon, IconPlus } from './icons.tsx';
import { TagesAnsicht } from './TagesAnsicht.tsx';
import { useLive } from './useLive.ts';
import { WochenAnsicht } from './WochenAnsicht.tsx';

type Ansicht = 'tag' | 'woche';
type EingabeArt = 'text' | 'sprache';
type Erlaubnis = NotificationPermission | 'nicht-verfuegbar';

interface Hinweis {
  id: number;
  text: string;
  aktion?: { label: string; ausfuehren: () => void };
}

/** Von Chrome, wenn die Seite als App installierbar ist. Fehlt in den TypeScript-Typen. */
interface InstallAufforderung extends Event {
  prompt(): Promise<void>;
}

const HINWEIS_DAUER_MS = 4000;
/** Finger zusammen unter 80 % → Woche; auseinander über 125 % → Tag. */
const ZOOM_RAUS = 0.8;
const ZOOM_REIN = 1.25;

const erlaubnisJetzt = (): Erlaubnis => (typeof Notification === 'undefined' ? 'nicht-verfuegbar' : Notification.permission);

/** Startet die App per Verknüpfung (langes Drücken aufs Icon) mit `?neu=text` oder `?neu=sprache`. */
function eingabeAusAdresse(): EingabeArt | null {
  const neu = new URLSearchParams(location.search).get('neu');
  if (neu !== 'text' && neu !== 'sprache') return null;
  history.replaceState(null, '', location.pathname);
  return neu;
}

/** Bittet Android, die App ab und zu im Hintergrund zu wecken (nur installierte App in Chrome). */
async function hintergrundAnmelden(): Promise<void> {
  try {
    const registrierung = await navigator.serviceWorker.ready;
    const periodisch = (registrierung as unknown as { periodicSync?: { register(tag: string, o: { minInterval: number }): Promise<void> } })
      .periodicSync;
    if (!periodisch) return;
    const status = await navigator.permissions.query({ name: 'periodic-background-sync' as PermissionName });
    if (status.state === 'granted') await periodisch.register(ERINNERUNG_TAG, { minInterval: 60 * 60 * 1000 });
  } catch {
    // Nicht unterstützt: Dann wird nur beim Öffnen der App erinnert.
  }
}

function erinnerungPruefen(): void {
  if (!('serviceWorker' in navigator)) return;
  void navigator.serviceWorker.getRegistration().then((r) => (r ? pruefeErinnerung(r) : undefined));
}

export function App() {
  const [heute, setHeute] = useState(() => epochTag(new Date()));
  const [tag, setTag] = useState(heute);
  const [ansicht, setAnsicht] = useState<Ansicht>('tag');
  const [zoom, setZoom] = useState<'' | 'raus' | 'rein'>('');
  const [wochenStart, setWochenStart] = useState(() => wochenStartVon(heute));
  const [eingabe, setEingabe] = useState<EingabeArt | null>(eingabeAusAdresse);
  const [dialog, setDialog] = useState(false);
  const [erinnerung, setErinnerungsZeit] = useState(STANDARD_ERINNERUNG);
  const [erlaubnis, setErlaubnis] = useState<Erlaubnis>(erlaubnisJetzt);
  const [hinweis, setHinweis] = useState<Hinweis | null>(null);
  const [installieren, setInstallieren] = useState<InstallAufforderung | null>(null);
  const [darstellung, setDarstellung] = useState<Darstellung>(leseDarstellung);

  const aufgaben = useLive(() => offeneAmTag(tag), [tag], [] as Aufgabe[]);
  const woche = useLive(() => offeneZwischen(wochenStart, wochenStart + 6), [wochenStart], [] as Aufgabe[]);

  // Aktuelle Werte für Zeitgeber und Gesten, die nicht bei jedem Rendern neu angelegt werden.
  const stand = useRef({ heute, tag, ansicht });
  stand.current = { heute, tag, ansicht };

  const zeige = (text: string, aktion?: Hinweis['aktion']) => setHinweis({ id: Date.now(), text, aktion });

  useEffect(() => {
    if (!hinweis) return;
    const zeitgeber = setTimeout(() => setHinweis(null), HINWEIS_DAUER_MS);
    return () => clearTimeout(zeitgeber);
  }, [hinweis]);

  // Start, Datumswechsel (auch um Mitternacht bei offener App) und Erinnerung.
  useEffect(() => {
    void navigator.storage?.persist?.();
    void leseErinnerung().then(setErinnerungsZeit);
    if (erlaubnisJetzt() === 'granted') void hintergrundAnmelden();

    const pruefen = () => {
      const jetzt = epochTag(new Date());
      const vorher = stand.current;
      if (jetzt !== vorher.heute) {
        setHeute(jetzt);
        if (vorher.tag <= vorher.heute) setTag(jetzt);
      }
      void uebertragen(jetzt).then(erinnerungPruefen);
    };
    pruefen();
    const zeitgeber = setInterval(pruefen, 30_000);
    const sichtbar = () => {
      if (document.visibilityState === 'visible') pruefen();
    };
    document.addEventListener('visibilitychange', sichtbar);

    const installierbar = (e: Event) => {
      e.preventDefault();
      setInstallieren(e as InstallAufforderung);
    };
    const installiert = () => setInstallieren(null);
    addEventListener('beforeinstallprompt', installierbar);
    addEventListener('appinstalled', installiert);
    return () => {
      clearInterval(zeitgeber);
      document.removeEventListener('visibilitychange', sichtbar);
      removeEventListener('beforeinstallprompt', installierbar);
      removeEventListener('appinstalled', installiert);
    };
  }, []);

  const zurWoche = () => {
    if (stand.current.ansicht === 'woche') return;
    setWochenStart(wochenStartVon(stand.current.tag));
    setZoom('raus');
    setAnsicht('woche');
  };
  const zumTag = (ziel = stand.current.tag) => {
    setTag(ziel);
    setZoom('rein');
    setAnsicht('tag');
  };
  const zuHeute = () => {
    setWochenStart(wochenStartVon(stand.current.heute));
    zumTag(stand.current.heute);
  };

  // Zurück-Taste von Android: erst Eingabe schließen, dann aus der Woche bzw. einem anderen Tag zu heute.
  // Sobald die App nicht auf „heute“ steht, liegt genau ein eigener Eintrag im Verlauf, den die Taste verbraucht.
  const eintragImVerlauf = useRef(false);
  const [verbraucht, setVerbraucht] = useState(0);
  const tief = eingabe !== null || dialog || ansicht === 'woche' || tag !== heute;
  useEffect(() => {
    if (tief && !eintragImVerlauf.current) {
      history.pushState({ todo: true }, '');
      eintragImVerlauf.current = true;
    }
  }, [tief, verbraucht]);
  const offen = useRef({ eingabe, dialog });
  offen.current = { eingabe, dialog };
  useEffect(() => {
    const zurueck = () => {
      eintragImVerlauf.current = false;
      setVerbraucht((n) => n + 1);
      if (offen.current.eingabe || offen.current.dialog) {
        setEingabe(null);
        setDialog(false);
      } else if (stand.current.ansicht === 'woche') zumTag();
      else if (stand.current.tag !== stand.current.heute) zuHeute();
    };
    addEventListener('popstate', zurueck);
    return () => removeEventListener('popstate', zurueck);
  }, []);

  // Pinch-Geste mit zwei Fingern; am Rechner auch Strg + Mausrad bzw. Touchpad-Pinch.
  const pinch = useRef<{ abstand: number; ausgeloest: boolean } | null>(null);
  const abstand = (t: TouchList) => Math.hypot(t[0]!.clientX - t[1]!.clientX, t[0]!.clientY - t[1]!.clientY);
  const zoomen = (faktor: number) => {
    if (faktor < ZOOM_RAUS) zurWoche();
    else if (faktor > ZOOM_REIN && stand.current.ansicht === 'woche') zumTag();
    else return false;
    return true;
  };

  const erledigt = (aufgabe: Aufgabe) => {
    const id = aufgabe.id!;
    void erledigen(id);
    zeige(`Erledigt: ${aufgabe.text}`, { label: 'Rückgängig', ausfuehren: () => void rueckgaengig(id) });
  };

  const erlauben = async () => {
    if (typeof Notification === 'undefined') return;
    const ergebnis = await Notification.requestPermission();
    setErlaubnis(ergebnis);
    if (ergebnis === 'granted') {
      await hintergrundAnmelden();
      erinnerungPruefen();
    }
  };

  const hinweise = (
    <>
      {installieren && (
        <button
          class="pille"
          onClick={async () => {
            await installieren.prompt();
            setInstallieren(null);
          }}
        >
          Als App installieren
        </button>
      )}
      {erlaubnis === 'default' && tag === heute && (
        <button class="pille" onClick={() => void erlauben()}>
          Erinnerung um {erinnerung} einschalten
        </button>
      )}
    </>
  );

  return (
    <div
      class="wurzel"
      onTouchStart={(e) => {
        if (e.touches.length === 2) pinch.current = { abstand: abstand(e.touches), ausgeloest: false };
      }}
      onTouchMove={(e) => {
        const p = pinch.current;
        if (!p || p.ausgeloest || e.touches.length < 2) return;
        p.ausgeloest = zoomen(abstand(e.touches) / p.abstand);
      }}
      onTouchEnd={(e) => {
        if (e.touches.length < 2) pinch.current = null;
      }}
      onWheel={(e) => {
        if (!e.ctrlKey) return;
        e.preventDefault();
        zoomen(e.deltaY > 0 ? 0 : 2);
      }}
    >
      <main key={ansicht} class={`ansicht ${zoom}`}>
        {ansicht === 'tag' ? (
          <TagesAnsicht
            tag={tag}
            heute={heute}
            aufgaben={aufgaben}
            erinnerung={erinnerung}
            onErledigen={erledigt}
            onWoche={zurWoche}
            onHeute={zuHeute}
            onErinnerung={() => setDialog(true)}
            darstellung={darstellung}
            onDarstellung={() => {
              const neu = naechsteDarstellung(darstellung);
              wendeDarstellungAn(neu);
              setDarstellung(neu);
              zeige(`Darstellung: ${DARSTELLUNG_NAME[neu]}`);
            }}
            hinweise={hinweise}
          />
        ) : (
          <WochenAnsicht
            wochenStart={wochenStart}
            heute={heute}
            aufgaben={woche}
            onTag={zumTag}
            onVerschieben={(wochen) => setWochenStart(wochenStart + 7 * wochen)}
            onHeute={zuHeute}
          />
        )}
      </main>

      {ansicht === 'tag' && tag >= heute && !eingabe && (
        <div class="schwebend">
          <button class="fab klein" onClick={() => setEingabe('sprache')} aria-label="Per Sprache eintragen">
            <IconMikrofon />
          </button>
          <button class="fab" onClick={() => setEingabe('text')} aria-label="Aufgabe eintragen">
            <IconPlus />
          </button>
        </div>
      )}

      {hinweis && (
        <div class="hinweis" role="status" key={hinweis.id}>
          <span>{hinweis.text}</span>
          {hinweis.aktion && (
            <button
              class="textknopf"
              onClick={() => {
                hinweis.aktion!.ausfuehren();
                setHinweis(null);
              }}
            >
              {hinweis.aktion.label}
            </button>
          )}
        </div>
      )}

      {eingabe && (
        <Eingabe
          zielTag={tag}
          heute={heute}
          mitSprache={eingabe === 'sprache'}
          onSpeichern={(text) => void hinzufuegen(text, tag)}
          onSchliessen={() => setEingabe(null)}
          onHinweis={(text) => zeige(text)}
        />
      )}

      {dialog && (
        <ErinnerungDialog
          uhrzeit={erinnerung}
          erlaubnis={erlaubnis}
          onErlauben={() => void erlauben()}
          onSchliessen={() => setDialog(false)}
          onSpeichern={(uhrzeit) => {
            void setzeErinnerung(uhrzeit).then(erinnerungPruefen);
            setErinnerungsZeit(uhrzeit);
            setDialog(false);
            zeige(`Erinnerung täglich um ${uhrzeit}, wenn noch etwas offen ist`);
          }}
        />
      )}
    </div>
  );
}
