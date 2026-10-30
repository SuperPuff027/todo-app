/**
 * Spracheingabe über die Web Speech API (Chrome/Android, Safari/iOS).
 * Braucht auf den meisten Geräten Internet. Offline bleibt das Mikrofon der Bildschirmtastatur.
 */

interface Ergebnis {
  readonly isFinal: boolean;
  readonly [alternative: number]: { readonly transcript: string };
}

interface Erkennung {
  lang: string;
  interimResults: boolean;
  continuous: boolean;
  maxAlternatives: number;
  onresult: ((ereignis: { readonly results: ArrayLike<Ergebnis> }) => void) | null;
  onerror: ((ereignis: { readonly error: string }) => void) | null;
  onend: (() => void) | null;
  start(): void;
  stop(): void;
  abort(): void;
}

type ErkennungsKlasse = new () => Erkennung;

function klasse(): ErkennungsKlasse | undefined {
  const fenster = window as unknown as { SpeechRecognition?: ErkennungsKlasse; webkitSpeechRecognition?: ErkennungsKlasse };
  return fenster.SpeechRecognition ?? fenster.webkitSpeechRecognition;
}

export const spracheVerfuegbar = (): boolean => klasse() !== undefined;

export interface Aufnahme {
  /** Aufnahme beenden; das bis dahin Erkannte bleibt. */
  stoppen(): void;
  /** Aufnahme verwerfen, ohne weiteres Ergebnis. */
  abbrechen(): void;
}

/**
 * Startet eine Aufnahme. [onText] bekommt laufend den bisher erkannten Text (auch Zwischenergebnisse),
 * [onEnde] einmal am Schluss den Fehlercode oder `null`.
 */
export function starteAufnahme(onText: (text: string) => void, onEnde: (fehler: string | null) => void): Aufnahme | null {
  const Klasse = klasse();
  if (!Klasse) return null;
  const erkennung = new Klasse();
  erkennung.lang = navigator.language || 'de-DE';
  erkennung.interimResults = true;
  erkennung.continuous = false;
  erkennung.maxAlternatives = 1;

  let fehler: string | null = null;
  erkennung.onresult = ({ results }) => {
    let text = '';
    for (let i = 0; i < results.length; i++) text += results[i]?.[0]?.transcript ?? '';
    onText(text.trim());
  };
  erkennung.onerror = (ereignis) => {
    fehler = ereignis.error;
  };
  erkennung.onend = () => onEnde(fehler);
  try {
    erkennung.start();
  } catch {
    return null;
  }
  return { stoppen: () => erkennung.stop(), abbrechen: () => erkennung.abort() };
}

/** Hinweis für den Nutzer zu einem Fehlercode, oder `null`, wenn es nichts zu sagen gibt. */
export function spracheFehlertext(fehler: string | null): string | null {
  switch (fehler) {
    case null:
    case 'aborted':
      return null;
    case 'no-speech':
    case 'no-match':
      return 'Nichts verstanden';
    case 'not-allowed':
    case 'service-not-allowed':
      return 'Mikrofon ist nicht erlaubt';
    case 'network':
      return 'Spracherkennung braucht Internet. Offline geht das Mikrofon der Tastatur.';
    default:
      return 'Spracherkennung fehlgeschlagen';
  }
}
