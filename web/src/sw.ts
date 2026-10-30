/// <reference lib="webworker" />
import { clientsClaim } from 'workbox-core';
import { cleanupOutdatedCaches, createHandlerBoundToURL, precacheAndRoute, type PrecacheEntry } from 'workbox-precaching';
import { NavigationRoute, registerRoute } from 'workbox-routing';
import { ERINNERUNG_TAG, pruefeErinnerung } from './daten/erinnerung.ts';

// __WB_MANIFEST ersetzt vite-plugin-pwa beim Bauen durch die Liste aller Dateien der App.
declare const self: ServiceWorkerGlobalScope & { __WB_MANIFEST: (PrecacheEntry | string)[] };

/** Periodic Background Sync (nur Chrome/Android, nur installierte App) fehlt in den TypeScript-Typen. */
interface PeriodicSyncEvent extends ExtendableEvent {
  readonly tag: string;
}

// App-Hülle vollständig vorab speichern, damit sie ohne Netz startet.
precacheAndRoute(self.__WB_MANIFEST);
cleanupOutdatedCaches();
registerRoute(new NavigationRoute(createHandlerBoundToURL('index.html')));

// Neue Version sofort aktivieren (registerType: autoUpdate).
void self.skipWaiting();
clientsClaim();

// Android weckt den Service Worker ab und zu im Hintergrund (Abstand bestimmt das System, meist Stunden).
// Dann Übertrag nachholen und, falls fällig, erinnern.
self.addEventListener('periodicsync', (ereignis) => {
  const sync = ereignis as PeriodicSyncEvent;
  if (sync.tag === ERINNERUNG_TAG) sync.waitUntil(pruefeErinnerung(self.registration));
});

// Tipp auf die Benachrichtigung öffnet die Tagesansicht.
self.addEventListener('notificationclick', (ereignis) => {
  ereignis.notification.close();
  ereignis.waitUntil(
    (async () => {
      const fenster = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
      const offen = fenster[0];
      if (offen) {
        await offen.navigate(self.registration.scope).catch(() => undefined);
        await offen.focus();
      } else {
        await self.clients.openWindow(self.registration.scope);
      }
    })(),
  );
});
