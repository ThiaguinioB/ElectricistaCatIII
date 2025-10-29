import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#0d9488',
        accent: '#f97316'
      }
    }
  },
  plugins: []
} satisfies Config;
