import { SessionService } from './session.service';
describe('Session expiry', () => {
  const session = new SessionService();
  afterEach(() => session.clear());
  it('removes an expired session and its old shop context', () => {
    sessionStorage.setItem('tokenId', 'x.' + btoa(JSON.stringify({ exp: 1 })) + '.x');
    sessionStorage.setItem('shopId', 'old-shop');
    expect(session.token).toBeNull();
    expect(sessionStorage.getItem('shopId')).toBeNull();
  });
  it('rejects malformed stored tokens', () => {
    sessionStorage.setItem('tokenId', 'not-a-jwt');
    expect(session.token).toBeNull();
  });
  it('clears old owned-shop context when another user signs in', () => {
    sessionStorage.setItem('shopId', 'old-shop');
    const jwt = 'x.' + btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })) + '.x';
    session.save({ jwt, id: 'new', username: 'New', role: 'SHOPKEEPER' });
    expect(sessionStorage.getItem('shopId')).toBeNull();
    expect(session.home).toBe('/shop-dashboard');
  });
});
