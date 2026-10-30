import { render } from 'preact';
import { registerSW } from 'virtual:pwa-register';
import { App } from './ui/App.tsx';
import './style.css';

// Service Worker für den Offline-Start; neue Versionen werden sofort übernommen.
registerSW({ immediate: true });

render(<App />, document.getElementById('app')!);
