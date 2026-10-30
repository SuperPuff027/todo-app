/** Die einzigen Symbole der App (wie im Brief): Plus, Mikrofon, Haken, Kalender. */

const basis = {
  width: 24,
  height: 24,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  'stroke-width': 2.2,
  'stroke-linecap': 'round',
  'stroke-linejoin': 'round',
  'aria-hidden': true,
} as const;

export const IconPlus = () => (
  <svg {...basis}>
    <path d="M12 5v14M5 12h14" />
  </svg>
);

export const IconMikrofon = () => (
  <svg {...basis}>
    <rect x="9" y="3" width="6" height="11" rx="3" />
    <path d="M5.5 11a6.5 6.5 0 0 0 13 0M12 17.5V21" />
  </svg>
);

export const IconHaken = () => (
  <svg {...basis} stroke-width={2.8}>
    <path d="M5.5 12.5l4 4 9-9" />
  </svg>
);

export const IconKalender = () => (
  <svg {...basis}>
    <rect x="3.5" y="5" width="17" height="15.5" rx="4" />
    <path d="M8 3v4M16 3v4M3.5 10h17" />
  </svg>
);
