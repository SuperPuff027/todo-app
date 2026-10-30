import { render } from 'preact';
import { registerSW } from 'virtual:pwa-register';
import { App } from './ui/App.tsx';
import { leseDarstellung, wendeDarstellungAn } from './ui/darstellung.ts';
import './style.css';

// Service Worker für den Offline-Start; neue Versionen werden sofort übernommen.
registerSW({ immediate: true });

// Gespeicherte Darstellung (hell/dunkel) vor dem ersten Zeichnen setzen.
wendeDarstellungAn(leseDarstellung());

render(<App />, document.getElementById('app')!);
