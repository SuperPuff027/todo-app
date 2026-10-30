import { useState } from 'preact/hooks';

interface Props {
  uhrzeit: string;
  erlaubnis: NotificationPermission | 'nicht-verfuegbar';
  onSpeichern: (uhrzeit: string) => void;
  onErlauben: () => void;
  onSchliessen: () => void;
}

/** Uhrzeit der täglichen Erinnerung und Erlaubnis für Benachrichtigungen. */
export function ErinnerungDialog({ uhrzeit, erlaubnis, onSpeichern, onErlauben, onSchliessen }: Props) {
  const [wert, setWert] = useState(uhrzeit);
  return (
    <div class="blatt-hintergrund" onClick={onSchliessen}>
      <form
        class="blatt"
        onClick={(e) => e.stopPropagation()}
        onSubmit={(e) => {
          e.preventDefault();
          if (wert) onSpeichern(wert);
        }}
      >
        <h2>Erinnerung</h2>
        <p class="leise">Täglich zu dieser Uhrzeit, wenn heute noch etwas offen ist.</p>
        <input
          class="zeit"
          type="time"
          value={wert}
          required
          onInput={(e) => setWert(e.currentTarget.value)}
          aria-label="Uhrzeit der Erinnerung"
        />
        {erlaubnis === 'default' && (
          <button type="button" class="pille breit" onClick={onErlauben}>
            Benachrichtigungen erlauben
          </button>
        )}
        {erlaubnis === 'denied' && (
          <p class="leise">Benachrichtigungen sind im Browser blockiert. Erlauben unter Website-Einstellungen → Benachrichtigungen.</p>
        )}
        {erlaubnis === 'nicht-verfuegbar' && (
          <p class="leise">Benachrichtigungen gehen erst, wenn ToDo als App auf dem Startbildschirm installiert ist.</p>
        )}
        <p class="leise klein">
          Eine Web-App kann nicht auf die Minute geweckt werden: Ist ToDo geschlossen, kommt die Erinnerung, sobald Android
          die App im Hintergrund kurz aufweckt (meist alle paar Stunden) oder du sie öffnest.
        </p>
        <div class="knopfzeile">
          <button type="button" class="textknopf" onClick={onSchliessen}>
            Abbrechen
          </button>
          <button type="submit" class="pille voll">
            Speichern
          </button>
        </div>
      </form>
    </div>
  );
}
