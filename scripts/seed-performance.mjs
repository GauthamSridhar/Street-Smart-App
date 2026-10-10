// Reusable fixture for the isolated compose.e2e.yml stack only. Node 22+.
const base = 'http://localhost:18088';
const adminCredentials = { identifier: 'acceptance-admin@example.test', password: 'Acceptance-only-2026!' };
const tokens = [];
const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
async function call(path, token, data, method = data ? 'POST' : 'GET', allowed = []) {
  for (let attempt = 0; attempt < 12; attempt++) {
    const response = await fetch(base + path, {
      method, signal: AbortSignal.timeout(15000),
      headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
      ...(data ? { body: JSON.stringify(data) } : {}),
    });
    if (response.status === 429) { await response.arrayBuffer(); await pause(10000); continue; }
    if (allowed.includes(response.status)) return null;
    if (!response.ok) throw new Error('HTTP ' + response.status + ' on ' + path.split('?')[0]);
    return response.status === 204 ? null : response.json();
  }
  throw new Error('Rate limit persisted on ' + path.split('?')[0]);
}
async function wait(check, description) {
  for (let i = 0; i < 90; i++) {
    try { const result = await check(); if (result) return result; } catch {}
    await pause(2000);
  }
  throw new Error('Timed out waiting for ' + description);
}
const categories = [
  ['grocery', ['Milk', 'Rice', 'Wheat Flour', 'Tea', 'Coffee', 'Sugar', 'Lentils', 'Cooking Oil', 'Biscuits', 'Salt']],
  ['electronics', ['USB Cable', 'Phone Charger', 'Headphones', 'Power Bank', 'Keyboard', 'Mouse', 'LED Bulb', 'Speaker', 'Memory Card', 'Adapter']],
  ['clothing', ['Cotton Shirt', 'T Shirt', 'Jeans', 'Kurta', 'Socks', 'Scarf', 'Jacket', 'Trousers', 'Cap', 'Leggings']],
  ['books', ['Java Handbook', 'Algorithms Book', 'Notebook', 'English Grammar', 'Mathematics Guide', 'Science Book', 'Story Book', 'Atlas', 'Exam Guide', 'Drawing Book']],
  ['pharmacy', ['First Aid Kit', 'Bandages', 'Hand Sanitizer', 'Thermometer', 'Cotton Pads', 'Face Mask', 'Soap', 'Toothpaste', 'Sunscreen', 'Moisturizer']],
  ['restaurant', ['Veg Biryani', 'Dosa', 'Idli', 'Paneer Curry', 'Veg Meals', 'Chapati', 'Lemon Rice', 'Fruit Juice', 'Tea', 'Coffee']],
];
try {
  // Authentication must succeed before any fixture account or OTP is created.
  const admin = await wait(() => call('/api/users/login', '', adminCredentials), 'isolated test administrator');
  tokens.push(admin.jwt);
  let addedProducts = 0;
  for (let index = 0; index < 12; index++) {
    const [category, names] = categories[index % categories.length];
    const identifier = 'catalogue-fixture-' + index + '@example.test';
    const password = 'Catalogue-test-only-2026!';
    let owner = await call('/api/users/login', '', { identifier, password }, 'POST', [401]);
    if (!owner) {
      const phoneNumber = '+91910010' + String(index).padStart(4, '0');
      await call('/api/sms/send', '', { phoneNumber });
      const verification = await call('/api/sms/verify', '', { phoneNumber, otpCode: '000000' });
      await call('/api/users/register', '', {
        email: identifier, password, phoneNumber, fullName: 'Catalogue Fixture ' + index, role: 'SHOPKEEPER',
        phoneVerificationToken: verification.verificationToken,
      });
      owner = await call('/api/users/login', '', { identifier, password });
    }
    tokens.push(owner.jwt);
    let shop = await call('/api/shops/owner/' + owner.id, owner.jwt, null, 'GET', [404]);
    if (!shop) {
      shop = await call('/api/shops/register?userId=' + owner.id, owner.jwt, {
        name: 'Benchmark ' + category + ' Shop ' + (index + 1), category,
        description: 'Synthetic local catalogue fixture', address: 'Fixture Street ' + (index + 1),
        latitude: 18.625156 + (index - 5) * 0.003, longitude: 79.943906 + (index % 4 - 2) * 0.004,
      });
    }
    if (shop.status === 'PENDING') {
      await wait(async () => (await call('/api/approvals/pending', admin.jwt)).some(row => row.shopId === shop.id), 'shop approval request');
      await call('/api/approvals/' + shop.id + '/approve', admin.jwt, null, 'POST');
      await wait(async () => (await call('/api/shops/' + shop.id, owner.jwt)).status === 'APPROVED', 'shop approval delivery');
    }
    const existing = new Set((await call('/api/products?shopId=' + shop.id, owner.jwt)).map(row => row.name));
    let cursor = 0;
    await Promise.all(Array.from({ length: 4 }, async () => {
      while (cursor < 100) {
        const number = cursor++;
        const name = names[number % 10] + ' - Variant ' + (Math.floor(number / 10) + 1);
        if (existing.has(name)) continue;
        await call('/api/products?shopId=' + shop.id, owner.jwt, {
          name, available: number % 5 !== 0, price: 25 + (number * 17 + index * 11) % 1975, currency: 'INR',
        });
        addedProducts++;
      }
    }));
    const count = await call('/api/products/count/' + shop.id, owner.jwt);
    if (count !== 100) throw new Error('Unexpected fixture shop product count: ' + count);
    console.log('Verified fixture shop ' + (index + 1) + '/12: ' + category + ', 100 products');
  }
  console.log(JSON.stringify({ fixture: 'catalogue-v1', shops: 12, products: 1200, categories: 6, availableProducts: 960, addedProducts }, null, 2));
} finally {
  await Promise.allSettled(tokens.map(token => call('/api/users/logout', token, null, 'POST')));
}
