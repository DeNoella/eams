/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ["class"],
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // National Bank of Rwanda (BNR) brand palette
        bnr: {
          'brown-dark':   '#6B2D0E', // primary nav, buttons, headers
          'brown':        '#8B4513', // hover states, accents
          'olive':        '#7A6B1A', // secondary buttons, highlights
          'cream':        '#F5F0E8', // page & email background
          'cream-deep':   '#ECE4D2',
          'text-dark':    '#2C1810', // headings
          'text-body':    '#3D2B1F', // paragraph text
          'divider':      '#D4C4A8', // subtle separators
        },
        // Semantic aliases (themed via CSS vars on data-theme)
        surface: 'var(--bg-surface)',
        'surface-2': 'var(--bg-surface-2)',
        elevated: 'var(--bg-elevated)',
        primary: 'var(--accent)',
        'primary-hover': 'var(--accent-hover)',
        'primary-fg': 'var(--accent-contrast)',
        secondary: 'var(--secondary)',
        'secondary-fg': 'var(--secondary-contrast)',
        'text-primary': 'var(--text-primary)',
        'text-secondary': 'var(--text-secondary)',
        'text-muted': 'var(--text-muted)',
        border: 'var(--border)',
        'border-strong': 'var(--border-strong)',
      },
      fontFamily: {
        display: ['Inter', 'system-ui', 'sans-serif'],
        body: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['IBM Plex Mono', 'monospace'],
      },
      letterSpacing: {
        nav: '0.05em',
      },
      borderRadius: {
        DEFAULT: '3px',
        sm: '2px',
        md: '4px',
      },
    },
  },
  plugins: [],
}
