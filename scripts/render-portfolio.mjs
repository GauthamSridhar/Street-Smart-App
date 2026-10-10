// Render source-derived SVG diagrams without starting the application stack.
import { createRequire } from 'node:module';
import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';

const requireFrontend = createRequire(new URL('../street-smart-frontend/package.json', import.meta.url));
const { chromium } = requireFrontend('@playwright/test');
const directory = new URL('../docs/assets/portfolio/', import.meta.url);
const diagrams = ['system-architecture', 'approval-reliability', 'search-benchmark'];
const browser = await chromium.launch({ channel: 'chrome', headless: true });
try {
  for (const name of diagrams) {
    const source = await readFile(new URL(`${name}.svg`, directory), 'utf8');
    const dimensions = source.match(/<svg[^>]*width="(\d+)"[^>]*height="(\d+)"/);
    if (!dimensions) throw new Error(`Missing dimensions: ${name}`);
    const page = await browser.newPage({
      viewport: { width: Number(dimensions[1]), height: Number(dimensions[2]) },
      deviceScaleFactor: 1,
    });
    // Block remote requests; these assets contain only inline vectors and text.
    await page.route('**/*', route => route.abort());
    await page.setContent(`<html><body style="margin:0">${source}</body></html>`);
    await page.evaluate(() => document.fonts.ready);
    await page.screenshot({ path: fileURLToPath(new URL(`${name}.png`, directory)) });
    await page.close();
    console.log(`Rendered ${name}.png`);
  }
} finally {
  await browser.close();
}
