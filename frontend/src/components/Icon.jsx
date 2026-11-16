// Íconos de línea, dibujados a mano (sin librería externa).
const PATHS = {
  sprout: 'M12 20v-8m0 0c0-3 2-5 6-5 0 3-2 5-6 5Zm0 2c0-2.5-1.7-4-5-4 0 2.5 1.7 4 5 4Z',
  pin: 'M12 21s-7-5.6-7-11a7 7 0 1 1 14 0c0 5.4-7 11-7 11Zm0-8.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z',
  search: 'm21 21-4.3-4.3M11 18a7 7 0 1 1 0-14 7 7 0 0 1 0 14Z',
  plus: 'M12 5v14M5 12h14',
  eye: 'M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Zm10 3a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z',
  pencil: 'm4 20 4-1 11-11a2.1 2.1 0 0 0-3-3L5 16l-1 4Z',
  trash: 'M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3',
  user: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-8 9c0-4 3.6-6 8-6s8 2 8 6',
  users: 'M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 10c0-3.5 3-5.5 7-5.5s7 2 7 5.5M17 4a4 4 0 0 1 0 7m3 10c0-2.5-1.5-4-3.5-4.8',
  logout: 'M9 21H5V3h4M16 17l5-5-5-5M21 12H9',
  home: 'm3 11 9-8 9 8M5 10v10h5v-6h4v6h5V10',
  file: 'M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8l-5-5Zm0 0v5h5M9 13h6M9 17h6',
  chart: 'M4 20V4M4 20h16M8 16v-5M12 16V8M16 16v-3',
  bell: 'M6 8a6 6 0 1 1 12 0c0 7 3 8 3 8H3s3-1 3-8ZM10 20a2 2 0 0 0 4 0',
  card: 'M3 6h18v12H3V6Zm0 4h18M7 15h3',
  calendar: 'M4 6h16v14H4V6Zm0 4h16M8 3v4M16 3v4',
  compass: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Zm3-12-2 5-5 2 2-5 5-2Z',
  flag: 'M5 21V4m0 0h11l-2 4 2 4H5',
  check: 'M5 12.5 10 17l9-10',
  x: 'M6 6l12 12M18 6 6 18',
  download: 'M12 4v11m0 0-4-4m4 4 4-4M4 20h16',
  arrow: 'M5 12h14m-5-5 5 5-5 5',
  arrowLeft: 'M19 12H5m5-5-5 5 5 5',
  coffee: 'M5 9h11v6a4 4 0 0 1-4 4H9a4 4 0 0 1-4-4V9Zm11 1h2a2 2 0 0 1 0 4h-2M8 3v3m3-3v3',
  alert: 'M12 9v4m0 4h.01M10.3 3.9 2.4 18a2 2 0 0 0 1.7 3h15.8a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z',
  lock: 'M6 11h12v9H6v-9Zm3 0V8a3 3 0 0 1 6 0v3',
  mail: 'M3 5h18v14H3V5Zm0 1 9 7 9-7',
  tag: 'M3 12V4h8l10 10-8 8L3 12Zm5-4h.01',
  tour: 'M3 20 9 6l4 8 3-5 5 11H3Z',
  clock: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Zm0-14v5l3 2',
  send: 'm22 2-11 11M22 2l-7 20-4-9-9-4 20-7Z',
  chevron: 'm6 9 6 6 6-6',
  refresh: 'M20 12a8 8 0 1 1-2.3-5.7M20 4v4h-4',
  list: 'M8 6h12M8 12h12M8 18h12M4 6h.01M4 12h.01M4 18h.01',
  menu: 'M4 6h16M4 12h16M4 18h16',
}

export default function Icon({ name, size = 18, className = '' }) {
  return (
    <svg
      className={`icon ${className}`}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={PATHS[name] || PATHS.file} />
    </svg>
  )
}
