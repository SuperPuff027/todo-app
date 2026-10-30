/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import preact from '@preact/preset-vite';
import { VitePWA } from 'vite-plugin-pwa';

// Nur diese Node-Variable wird gebraucht; spart die Abhängigkeit @types/node.
declare const process: { env: Record<string, string | undefined> };

const HINTERGRUND = '#f6f5f2';

export default defineConfig({
  // GitHub Pages liefert unter /<repo-name>/ aus; der Workflow setzt BASE_PATH.
  base: process.env.BASE_PATH ?? '/',
  plugins: [
    preact(),
    VitePWA({
      // Eigener Service Worker (src/sw.ts), weil er auch Erinnerung und Übertrag im Hintergrund prüft.
      strategies: 'injectManifest',
      srcDir: 'src',
      filename: 'sw.ts',
      // Neue Version wird sofort aktiv (Seite lädt neu). Unkritisch, jede Eingabe ist sofort gespeichert.
      registerType: 'autoUpdate',
      injectRegister: false,
      injectManifest: {
        globPatterns: ['**/*.{js,css,html,png,svg,woff2}'],
      },
      manifest: {
        name: 'ToDo',
        short_name: 'ToDo',
        description: 'Heutige Aufgaben, per Text oder Sprache, auch offline',
        lang: 'de',
        start_url: './',
        scope: './',
        display: 'standalone',
        orientation: 'portrait',
        background_color: HINTERGRUND,
        theme_color: HINTERGRUND,
        icons: [
          { src: 'icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'icon-512.png', sizes: '512x512', type: 'image/png' },
          { src: 'icon-maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
        // Langes Drücken auf das App-Icon (Ersatz für die Widget-Knöpfe der Android-App).
        shortcuts: [
          { name: 'Neue Aufgabe', short_name: 'Neu', url: './?neu=text', icons: [{ src: 'icon-192.png', sizes: '192x192' }] },
          { name: 'Aufgabe sprechen', short_name: 'Sprechen', url: './?neu=sprache', icons: [{ src: 'icon-192.png', sizes: '192x192' }] },
        ],
      },
    }),
  ],
  test: {
    include: ['src/**/*.test.ts'],
    environment: 'node',
  },
});
