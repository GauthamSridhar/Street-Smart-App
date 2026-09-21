import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  HTTP_INTERCEPTORS,
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthInterceptor } from './auth.interceptor';
import { SessionService } from '../services/session.service';
describe('API authentication boundary', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let session: SessionService;
  const token = () =>
    'test.' + btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })) + '.signature';
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionService);
    session.save({ jwt: token(), username: 'Test', id: 'user-id', role: 'USER' });
  });
  afterEach(() => {
    controller.verify();
    session.clear();
  });
  it('attaches the current bearer token to this API', () => {
    http.get('/api/shops').subscribe();
    const req = controller.expectOne('/api/shops');
    expect(req.request.headers.get('Authorization')).toBe('Bearer ' + session.token);
    req.flush([]);
  });
  it('does not send a token to external origins or lookalike paths', () => {
    for (const url of ['https://untrusted.example/api/shops', '/api-evil/shops']) {
      http.get(url).subscribe();
      const req = controller.expectOne(url);
      expect(req.request.headers.has('Authorization')).toBeFalse();
      req.flush([]);
    }
  });
  it('keeps public login requests independent of the previous token', () => {
    http.post('/api/users/login', {}).subscribe();
    const req = controller.expectOne('/api/users/login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
  it('clears expired sessions and redirects on a protected 401', () => {
    const navigate = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    http.get('/api/shops').subscribe({ error: () => {} });
    controller.expectOne('/api/shops').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(session.token).toBeNull();
    expect(navigate).toHaveBeenCalled();
  });
  it('does not log out a new session because an older request failed', () => {
    http.get('/api/shops').subscribe({ error: () => {} });
    const old = controller.expectOne('/api/shops');
    const replacement = token() + 'new';
    session.save({ jwt: replacement, username: 'New', id: 'new-user', role: 'USER' });
    old.flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(session.token).toBe(replacement);
  });
});
