import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 120_000,
  outputDir: '../artifacts/visual-acceptance',
  reporter: [['list']],
  use: {
    baseURL: process.env.VISUAL_BASE_URL || 'http://localhost:8088',
    browserName: 'chromium',
    headless: true,
    screenshot: 'only-on-failure',
  },
});
