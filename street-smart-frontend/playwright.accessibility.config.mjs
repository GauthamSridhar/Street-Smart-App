import { defineConfig } from '@playwright/test';
export default defineConfig({
  testDir: './e2e', testMatch: 'accessibility.spec.mjs', workers: 1,
  timeout: 45000, outputDir: '../artifacts/accessibility',
  reporter: [['list'], ['json', { outputFile: '../artifacts/accessibility/results.json' }]],
  use: { baseURL: 'http://127.0.0.1:18089', channel: 'chrome', reducedMotion: 'reduce' },
  webServer: { command: 'node e2e/serve-accessibility.mjs', url: 'http://127.0.0.1:18089', reuseExistingServer: false },
});
