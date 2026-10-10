import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

const shop = { id: 'shop-1', ownerId: 'user-1', name: 'Demo Grocery', category: 'Grocery',
  address: 'Demo Road', description: 'Synthetic accessibility fixture.', latitude: 18.63,
  longitude: 79.95, status: 'APPROVED', products: [{ id: 'product-1', name: 'Rice', available: true,
    price: 80, currency: 'INR', updatedAt: '2026-10-10T00:00:00Z' }], images: [] };
const pageOf = content => ({ content, totalElements: content.length, totalPages: 1, number: 0,
  size: 20, first: true, last: true, empty: !content.length });

async function fixtures(page, role) {
  if (role) await page.addInitScript(role => {
    sessionStorage.setItem('tokenId', 'test.' + btoa(JSON.stringify({ exp: Math.floor(Date.now()/1000)+3600 })) + '.test');
    sessionStorage.setItem('role', role); sessionStorage.setItem('id', 'user-1');
    sessionStorage.setItem('username', 'Accessibility User');
  }, role);
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname;
    let data;
    if (path === '/api/sms/config') data = { enabled: false };
    else if (path.endsWith('/categories')) data = ['Grocery'];
    else if (path === '/api/products/search') data = pageOf([{ ...shop.products[0], productId: 'product-1', productName: 'Rice', shopId: shop.id, shopName: shop.name, category: shop.category, address: shop.address, latitude: shop.latitude, longitude: shop.longitude }]);
    else if (path === '/api/shops/search') data = pageOf([shop]);
    else if (path === '/api/ratings/reports') data = pageOf([{ id: 'report-1', ratingId: 'rating-1', reason: 'Spam', review: 'Reported fixture text', createdAt: '2026-10-10T00:00:00Z', status: 'OPEN' }]);
    else if (path === '/api/users/me') data = { id: 'user-1', fullName: 'Accessibility User', email: 'demo@example.test', phoneNumber: '+919999999999', role, phoneVerified: false };
    else if (path === '/api/users/sessions') data = [{ id: 'session-1', createdAt: '2026-10-10T00:00:00Z', expiresAt: '2026-10-17T00:00:00Z', current: true }];
    else if (path.includes('/mine/')) { await route.fulfill({ status: 404, json: {} }); return; }
    else if (path.includes('/summary/')) data = { average: 0, count: 0 };
    else if (path.includes('/count')) data = 1;
    else if (path.endsWith('/is-favorite')) data = false;
    else if (path.startsWith('/api/shops/')) data = shop;
    else if (path === '/api/approvals/pending') data = [{ id: 'approval-1', shopId: shop.id, approvalStatus: 'PENDING', approved: false, reason: null, deliveryPending: false }];
    else if (path.includes('/favorites/user/')) data = [{ id: 'favorite-1', shopId: shop.id, shopName: shop.name, userId: 'user-1' }];
    else if (path === '/api/products') data = shop.products;
    else if (path.startsWith('/api/images/shops/')) data = [];
    else if (path.startsWith('/api/ratings/shops/')) data = [];
    else throw new Error('Missing accessibility fixture for ' + path);
    await route.fulfill({ json: data });
  });
}

async function audit(page, testInfo, suffix = '') {
  const results = await new AxeBuilder({ page }).withTags(['wcag2a','wcag2aa','wcag21a','wcag21aa','wcag22aa']).analyze();
  await testInfo.attach('axe' + suffix, { body: JSON.stringify(results, null, 2), contentType: 'application/json' });
  expect(results.violations.map(v => ({ id: v.id, impact: v.impact, nodes: v.nodes.map(n => n.target) }))).toEqual([]);
}

const routes = [[null,'/'],[null,'/login'],[null,'/register'],[null,'/about'],
  ['USER','/dashboard'],['USER','/favorites'],['USER','/profile'],
  ['SHOPKEEPER','/shop-dashboard'],['SHOPKEEPER','/shop-registration'],['SHOPKEEPER','/products'],['SHOPKEEPER','/reviews'],
  ['ADMIN','/admin-dashboard'],['ADMIN','/requests'],['ADMIN','/moderation']];
for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }]) {
  for (const [role, path] of routes) test(`axe ${viewport.width} ${role || 'public'} ${path}`, async ({ page }, testInfo) => {
    await page.setViewportSize(viewport);
    await fixtures(page, role);
    const errors = []; page.on('pageerror', error => errors.push(error.message));
    await page.goto(path); await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(path === '/' ? '/$' : path + '$'));
    expect(errors).toEqual([]);
    await audit(page, testInfo);
  });
}

test('keyboard skip link and shop dialog trap, escape and restore focus', async ({ page }, testInfo) => {
  await fixtures(page, 'USER'); await page.goto('/dashboard');
  await page.keyboard.press('Tab');
  await expect(page.getByText('Skip to main content', { exact: true })).toBeFocused();
  await page.keyboard.press('Enter'); await expect(page.locator('main')).toBeFocused();
  const open = page.getByRole('button', { name: /view shop/i });
  await open.focus(); await page.keyboard.press('Enter');
  const dialog = page.getByRole('dialog'); await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('button', { name: 'Close Shop Details' })).toBeFocused();
  await page.keyboard.press('Shift+Tab');
  expect(await dialog.evaluate(el => el.contains(document.activeElement))).toBeTruthy();
  await audit(page, testInfo, '-dialog');
  await page.keyboard.press('Escape'); await expect(dialog).toBeHidden();
  await expect(open).toBeFocused();
});

test('expired access token renews before protected route rendering', async ({ page, context }) => {
  await fixtures(page, 'USER');
  await page.addInitScript(() => {
    sessionStorage.setItem('tokenId', 'test.' + btoa(JSON.stringify({ exp: 1 })) + '.test');
  });
  await context.addCookies([{ name: 'ss_refresh', value: 'local-test-secret',
    domain: '127.0.0.1', path: '/api/users', httpOnly: true, sameSite: 'Strict' }]);
  let renewals = 0;
  await page.route('**/api/users/refresh', async route => {
    renewals++;
    expect(route.request().headers()['x-requested-with']).toBe('StreetSmart');
    expect(route.request().headers().cookie).toContain('ss_refresh=local-test-secret');
    await route.fulfill({ json: { jwt: 'test.' + Buffer.from(JSON.stringify({ exp: Math.floor(Date.now()/1000)+3600 })).toString('base64') + '.test',
      id: 'user-1', role: 'USER', username: 'Accessibility User' } });
  });
  await page.goto('/dashboard');
  await expect(page.getByRole('heading', { name: 'Find what you need, nearby' })).toBeVisible();
  expect(renewals).toBe(1);
  expect(await page.evaluate(() => document.cookie)).not.toContain('ss_refresh');
});
