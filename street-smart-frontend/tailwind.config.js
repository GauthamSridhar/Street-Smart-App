/** @type {import('tailwindcss').Config} */
module.exports = {
  presets: [require('@spartan-ng/ui-core/hlm-tailwind-preset')],
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
      },
      colors: {
        ink: {
          950: '#0f1419',
          900: '#171e27',
          800: '#243040',
        },
        paper: '#f6f3ee',
        amber: {
          50: '#fff8eb',
          100: '#ffefc6',
          400: '#f5b942',
          500: '#e8a317',
          600: '#c9840d',
        },
      },
      boxShadow: {
        card: '0 1px 2px rgba(15, 20, 25, 0.06), 0 12px 32px -16px rgba(15, 20, 25, 0.18)',
        lift: '0 18px 40px -20px rgba(15, 20, 25, 0.35)',
      },
    },
  },
  plugins: [],
};
