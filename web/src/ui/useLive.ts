import { liveQuery } from 'dexie';
import { useEffect, useState } from 'preact/hooks';

/** Ergebnis einer Datenbankabfrage, das sich bei jeder Änderung an den Daten selbst aktualisiert. */
export function useLive<T>(abfrage: () => Promise<T>, abhaengig: unknown[], anfang: T): T {
  const [wert, setWert] = useState<T>(anfang);
  useEffect(() => {
    const abo = liveQuery(abfrage).subscribe({
      next: (neu) => setWert(() => neu),
      error: (fehler: unknown) => console.error(fehler),
    });
    return () => abo.unsubscribe();
  }, abhaengig);
  return wert;
}
