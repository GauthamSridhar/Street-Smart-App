import { expect, test } from '@playwright/test';
import { existsSync, readFileSync } from 'node:fs';

const chrome = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const now = () => `${Date.now()}${Math.floor(Math.random() * 10000)}`;
const e2eOtpCode = process.env.E2E_OTP_CODE;

function environment() {
  const path = new URL('../../.env', import.meta.url);
  if (!existsSync(path)) return {};
  return Object.fromEntries(
    readFileSync(path, 'utf8')
      .split(/\r?\n/)
      .map((line) => line.match(/^([A-Z_]+)=(.*)$/))
      .filter(Boolean)
      .map((match) => [match[1], match[2]]),
  );
}

async function newSession(request, role) {
  const id = now();
  const body = {
    email: `visual-${role.toLowerCase()}-${id}@example.test`,
    password: `Visual-only-${id}`,
    fullName: 'Visual Acceptance',
    phoneNumber: `+9198${id.slice(-8)}`,
    role,
  };
  const smsConfig = await request.get('/api/sms/config');
  expect(smsConfig.ok()).toBeTruthy();
  if ((await smsConfig.json()).enabled) {
    if (!e2eOtpCode)
      throw new Error('SMS is enabled. Start the stack with compose.e2e.yml and set E2E_OTP_CODE.');
    const sent = await request.post('/api/sms/send', { data: { phoneNumber: body.phoneNumber } });
    expect(sent.ok()).toBeTruthy();
    const verified = await request.post('/api/sms/verify', {
      data: { phoneNumber: body.phoneNumber, otpCode: e2eOtpCode },
    });
    expect(verified.ok()).toBeTruthy();
    body.phoneVerificationToken = (await verified.json()).verificationToken;
  }
  const registered = await request.post('/api/users/register', { data: body });
  expect(registered.ok()).toBeTruthy();
  const login = await request.post('/api/users/login', {
    data: { identifier: body.email, password: body.password },
  });
  expect(login.ok()).toBeTruthy();
  return await login.json();
}

async function authenticatedPage(browser, baseURL, session, viewport = { width: 1440, height: 1000 }) {
  const context = await browser.newContext({ viewport });
  await context.addInitScript((value) => {
    sessionStorage.setItem('tokenId', value.jwt);
    sessionStorage.setItem('id', value.id);
    sessionStorage.setItem('role', value.role);
    sessionStorage.setItem('username', value.username);
  }, session);
  const page = await context.newPage();
  await page.goto(baseURL, { waitUntil: 'networkidle' });
  return { context, page };
}

test.use({ launchOptions: { executablePath: chrome, args: ['--disable-gpu'] } });

async function visit(page, path, heading, image) {
  await page.goto(path, { waitUntil: 'networkidle' });
  await page.evaluate(() => window.scrollTo(0, 0));
  await expect(page.getByRole('heading', { name: heading })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBeTruthy();
  await page.screenshot({ path: `../artifacts/visual-acceptance/${image}`, fullPage: true });
}

test('a customer can register and sign in through the visible forms', async ({ page, baseURL }) => {
  test.skip(!e2eOtpCode, 'Use the isolated E2E stack and E2E_OTP_CODE for registration.');
  const id = now();
  const email = `visual-ui-${id}@example.test`;
  const password = `Visual-only-${id}`;
  await page.goto('/register', { waitUntil: 'networkidle' });
  await page.getByLabel('Phone Number').fill(`98${id.slice(-8)}`);
  await page.getByLabel('Full Name').fill('Visual Acceptance');
  await page.getByRole('textbox', { name: 'Email' }).fill(email);
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByLabel('Confirm Password').fill(password);
  await page.locator('form button[type="submit"]').click();
  const smsConfig = await page.request.get('/api/sms/config');
  if ((await smsConfig.json()).enabled) {
    if (!e2eOtpCode)
      throw new Error('SMS is enabled. Start the stack with compose.e2e.yml and set E2E_OTP_CODE.');
    await page.getByRole('dialog', { name: 'Verify your phone' }).getByLabel('Verification code').fill(e2eOtpCode);
    await page.getByRole('dialog', { name: 'Verify your phone' }).getByRole('button', { name: 'Submit' }).click();
  }
  await page.waitForURL(/\/login$/);
  await page.getByRole('textbox', { name: 'Email' }).fill(email);
  await page.getByLabel('Password').fill(password);
  await page.locator('form button[type="submit"]').click();
  await page.waitForURL(/\/dashboard$/);
  await expect(page.getByRole('heading', { name: /find products/i })).toBeVisible();
  await expect(page.getByText(/phone verification is disabled/i)).toHaveCount(0);
  await page.goto('/profile');
  await expect(page.getByRole('heading', { name: 'Active sessions' })).toBeVisible();
  await expect(page.getByText('This device', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'Sign out all devices', exact: true }).click();
  await page.waitForURL(/\/login$/);
});

test('public screens are readable at desktop and phone widths', async ({ page, baseURL }) => {
  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto('/', { waitUntil: 'networkidle' });
  await expect(page.getByRole('heading', { name: /your next find/i })).toBeVisible();
  await expect(page.getByRole('img', { name: /neighbourhood/i })).toBeVisible();
  await page.screenshot({ path: '../artifacts/visual-acceptance/landing-desktop.png', fullPage: true });

  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.getByRole('link', { name: /start exploring/i })).toBeVisible();
  await expect(page.locator('.neighbourhood')).toBeVisible();
  await page.screenshot({ path: '../artifacts/visual-acceptance/landing-mobile.png', fullPage: true });

  await page.goto('/login', { waitUntil: 'networkidle' });
  await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
  await expect(page.getByLabel('Password')).toBeVisible();
  await page.goto('/register', { waitUntil: 'networkidle' });
  await expect(page.getByRole('heading', { name: 'Register' })).toBeVisible();
  await expect(page.getByLabel('Full Name')).toBeVisible();
});

test('role pages render without horizontal overflow at desktop and phone widths', async ({ browser, request, baseURL }) => {
  const customer = await newSession(request, 'USER');
  const customerDesktop = await authenticatedPage(browser, baseURL, customer);
  for (const [path, heading, image] of [
    ['/dashboard', /find products/i, 'customer-dashboard-desktop.png'],
    ['/favorites', /your favourite shops/i, 'customer-favorites-desktop.png'],
    ['/profile', /your profile/i, 'customer-profile-desktop.png'],
  ]) await visit(customerDesktop.page, path, heading, image);
  await customerDesktop.context.close();

  const customerMobile = await authenticatedPage(browser, baseURL, customer, { width: 390, height: 844 });
  for (const [path, heading, image] of [
    ['/dashboard', /find products/i, 'customer-dashboard-mobile.png'],
    ['/favorites', /your favourite shops/i, 'customer-favorites-mobile.png'],
    ['/profile', /your profile/i, 'customer-profile-mobile.png'],
  ]) await visit(customerMobile.page, path, heading, image);
  await customerMobile.context.close();

  const shopkeeper = await newSession(request, 'SHOPKEEPER');
  const shopkeeperDesktop = await authenticatedPage(browser, baseURL, shopkeeper);
  for (const [path, heading, image] of [
    ['/shop-registration', /shopkeeper registration/i, 'shopkeeper-registration-desktop.png'],
    ['/shop-dashboard', /shop dashboard/i, 'shopkeeper-dashboard-desktop.png'],
    ['/products', /manage products/i, 'shopkeeper-products-desktop.png'],
    ['/reviews', /shop reviews/i, 'shopkeeper-reviews-desktop.png'],
  ]) await visit(shopkeeperDesktop.page, path, heading, image);
  await shopkeeperDesktop.context.close();

  const shopkeeperMobile = await authenticatedPage(browser, baseURL, shopkeeper, { width: 390, height: 844 });
  for (const [path, heading, image] of [
    ['/shop-registration', /shopkeeper registration/i, 'shopkeeper-registration-mobile.png'],
    ['/shop-dashboard', /shop dashboard/i, 'shopkeeper-dashboard-mobile.png'],
    ['/products', /manage products/i, 'shopkeeper-products-mobile.png'],
    ['/reviews', /shop reviews/i, 'shopkeeper-reviews-mobile.png'],
  ]) await visit(shopkeeperMobile.page, path, heading, image);
  await shopkeeperMobile.context.close();

  const settings = environment();
  test.skip(!settings.ADMIN_EMAIL || !settings.ADMIN_PASSWORD, 'Local administrator credentials are unavailable.');
  const adminLogin = await request.post('/api/users/login', {
    data: { identifier: settings.ADMIN_EMAIL, password: settings.ADMIN_PASSWORD },
  });
  expect(adminLogin.ok()).toBeTruthy();
  const admin = await adminLogin.json();
  for (const [viewport, suffix] of [[{ width: 1440, height: 1000 }, 'desktop'], [{ width: 390, height: 844 }, 'mobile']]) {
    const adminView = await authenticatedPage(browser, baseURL, admin, viewport);
    await visit(adminView.page, '/admin-dashboard', /admin dashboard/i, `admin-dashboard-${suffix}.png`);
    await visit(adminView.page, '/requests', /pending shop approvals/i, `admin-requests-${suffix}.png`);
    await visit(adminView.page, '/moderation', /review moderation/i, `admin-moderation-${suffix}.png`);
    await adminView.context.close();
  }
});

test('the configured Google map loads on the customer dashboard', async ({ browser, request, baseURL }) => {
  test.skip(process.env.E2E_OFFLINE_MAPS === 'true', 'Live Google Maps is validated separately; isolated acceptance disables provider traffic.');
  const settings = environment();
  test.skip(!settings.ADMIN_EMAIL || !settings.ADMIN_PASSWORD, 'Local administrator credentials are unavailable.');
  const adminLogin = await request.post('/api/users/login', {
    data: { identifier: settings.ADMIN_EMAIL, password: settings.ADMIN_PASSWORD },
  });
  expect(adminLogin.ok()).toBeTruthy();
  // The map itself is client-side. Reuse the local admin token while rendering
  // the customer route, avoiding an OTP-dependent throwaway registration.
  const customerSession = { ...(await adminLogin.json()), role: 'USER' };
  const { context, page } = await authenticatedPage(browser, baseURL, customerSession);
  const providerErrors = [];
  page.on('console', (message) => {
    if (message.type() === 'error' && /Google Maps|RefererNotAllowed|ApiNotActivated|BillingNotEnabled/i.test(message.text())) {
      providerErrors.push(message.text());
    }
  });

  await page.goto('/dashboard', { waitUntil: 'networkidle' });
  await page.getByRole('button', { name: 'Show map' }).click();
  await expect(page.locator('#map .gm-style')).toBeVisible({ timeout: 30_000 });
  await expect(page.getByText('Map could not load. Product search is still available.')).toHaveCount(0);
  await expect(page.getByText('Map is not configured. Use the product results above.')).toHaveCount(0);
  expect(providerErrors).toEqual([]);
  await context.close();
});
