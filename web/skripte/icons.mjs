// Erzeugt die App-Icons in public/ (weißer Haken im Kreis auf Salbeigrün), ohne Grafik-Bibliothek.
// Aufruf: npm run icons
import { writeFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';

const AKZENT = [0x2f, 0x6b, 0x5e];
const WEISS = [0xff, 0xff, 0xff];
const ZIEL = new URL('../public/', import.meta.url);

const CRC_TABELLE = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});

function crc32(daten) {
  let c = 0xffffffff;
  for (const byte of daten) c = CRC_TABELLE[(c ^ byte) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function block(typ, daten) {
  const laenge = Buffer.alloc(4);
  laenge.writeUInt32BE(daten.length);
  const inhalt = Buffer.concat([Buffer.from(typ, 'ascii'), daten]);
  const pruefsumme = Buffer.alloc(4);
  pruefsumme.writeUInt32BE(crc32(inhalt));
  return Buffer.concat([laenge, inhalt, pruefsumme]);
}

function png(groesse, rgba) {
  const kopf = Buffer.alloc(13);
  kopf.writeUInt32BE(groesse, 0);
  kopf.writeUInt32BE(groesse, 4);
  kopf[8] = 8; // Bit pro Kanal
  kopf[9] = 6; // RGBA
  const zeilen = Buffer.alloc((groesse * 4 + 1) * groesse);
  for (let y = 0; y < groesse; y++) {
    zeilen[y * (groesse * 4 + 1)] = 0;
    rgba.copy(zeilen, y * (groesse * 4 + 1) + 1, y * groesse * 4, (y + 1) * groesse * 4);
  }
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    block('IHDR', kopf),
    block('IDAT', deflateSync(zeilen, { level: 9 })),
    block('IEND', Buffer.alloc(0)),
  ]);
}

function abstandZuStrecke(px, py, ax, ay, bx, by) {
  const dx = bx - ax;
  const dy = by - ay;
  const t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)));
  return Math.hypot(px - ax - t * dx, py - ay - t * dy);
}

/** Liegt (x, y) im Symbol (Ring + Haken)? Koordinaten 0..1, Symbol um die Mitte, skaliert mit `massstab`. */
function imSymbol(x, y, massstab) {
  const u = (x - 0.5) / massstab + 0.5;
  const v = (y - 0.5) / massstab + 0.5;
  const ring = Math.abs(Math.hypot(u - 0.5, v - 0.5) - 0.36) < 0.035;
  const haken =
    Math.min(abstandZuStrecke(u, v, 0.335, 0.515, 0.45, 0.63), abstandZuStrecke(u, v, 0.45, 0.63, 0.67, 0.4)) < 0.042;
  return ring || haken;
}

/**
 * @param hintergrund 'kreis' | 'voll' | 'keiner'
 * @param massstab Größe des Symbols relativ zum Icon
 * @param vordergrund Farbe des Symbols
 */
function zeichne(groesse, hintergrund, massstab, vordergrund) {
  const rgba = Buffer.alloc(groesse * groesse * 4);
  const proben = 4;
  for (let y = 0; y < groesse; y++) {
    for (let x = 0; x < groesse; x++) {
      let flaeche = 0;
      let symbol = 0;
      for (let sy = 0; sy < proben; sy++) {
        for (let sx = 0; sx < proben; sx++) {
          const px = (x + (sx + 0.5) / proben) / groesse;
          const py = (y + (sy + 0.5) / proben) / groesse;
          const imHintergrund =
            hintergrund === 'voll' || (hintergrund === 'kreis' && Math.hypot(px - 0.5, py - 0.5) <= 0.5);
          if (imHintergrund) flaeche++;
          if (imSymbol(px, py, massstab)) symbol++;
        }
      }
      const n = proben * proben;
      const a = flaeche / n;
      const s = symbol / n;
      const i = (y * groesse + x) * 4;
      if (hintergrund === 'keiner') {
        rgba.set([...vordergrund, Math.round(s * 255)], i);
      } else {
        // Symbol nur innerhalb des Hintergrunds
        const anteil = Math.min(s, a);
        const farbe = AKZENT.map((k, j) => Math.round(k * (1 - anteil / (a || 1)) + vordergrund[j] * (anteil / (a || 1))));
        rgba.set([...farbe, Math.round(a * 255)], i);
      }
    }
  }
  return png(groesse, rgba);
}

const dateien = {
  'icon-192.png': zeichne(192, 'kreis', 0.78, WEISS),
  'icon-512.png': zeichne(512, 'kreis', 0.78, WEISS),
  // Maskierbar: Android schneidet die Form selbst zu, das Symbol muss in den inneren 80 % liegen.
  'icon-maskable-512.png': zeichne(512, 'voll', 0.62, WEISS),
  // iOS rundet selbst ab und mag keine Transparenz.
  'apple-touch-icon.png': zeichne(180, 'voll', 0.7, WEISS),
  // Kleines Symbol in der Statusleiste von Android (nur die Form zählt).
  'badge-96.png': zeichne(96, 'keiner', 1, WEISS),
};

for (const [name, inhalt] of Object.entries(dateien)) {
  writeFileSync(new URL(name, ZIEL), inhalt);
  console.log(`${name}: ${inhalt.length} Bytes`);
}
