/** Symbole der App: Plus, Mikrofon, Haken, Kalender (wie im Brief) und die drei für die Darstellung. */

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

/** Darstellung: Hell */
export const IconSonne = () => (
  <svg {...basis}>
    <circle cx="12" cy="12" r="4" />
    <path d="M12 2.5v2M12 19.5v2M2.5 12h2M19.5 12h2M5.3 5.3l1.4 1.4M17.3 17.3l1.4 1.4M5.3 18.7l1.4-1.4M17.3 6.7l1.4-1.4" />
  </svg>
);

/** Darstellung: Dunkel */
export const IconMond = () => (
  <svg {...basis}>
    <path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z" />
  </svg>
);

/** Darstellung: Automatisch (halb hell, halb dunkel) */
export const IconAuto = () => (
  <svg {...basis}>
    <circle cx="12" cy="12" r="8" />
    <path d="M12 4a8 8 0 0 1 0 16z" fill="currentColor" />
  </svg>
);
