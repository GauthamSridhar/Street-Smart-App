import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { SessionService } from './services/session.service';
import { RefreshService } from './services/refresh.service';

describe('AuthGuard', () => {
  const session = new SessionService();
  const refresh = jasmine.createSpyObj<RefreshService>('RefreshService', ['renew']);
  const loginTree = {} as UrlTree;
  const errorTree = {} as UrlTree;
  const router = jasmine.createSpyObj<Router>('Router', ['createUrlTree']);
  const state = {} as RouterStateSnapshot;

  beforeEach(() => {
    session.clear();
    router.createUrlTree.calls.reset();
    router.createUrlTree.and.callFake((commands: unknown[]) =>
      commands[0] === '/login' ? loginTree : errorTree,
    );
  });

  afterEach(() => session.clear());

  function route(roles?: string[]): ActivatedRouteSnapshot {
    return { data: roles ? { roles } : {} } as unknown as ActivatedRouteSnapshot;
  }

  function signIn(role: 'USER' | 'SHOPKEEPER' | 'ADMIN'): void {
    const jwt =
      'x.' +
      btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })) +
      '.x';
    session.save({ jwt, id: 'user-id', username: 'User', role });
  }

  it('redirects anonymous users to login', () => {
    expect(new AuthGuard(router, session, refresh).canActivate(route(['USER']), state)).toBe(loginTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  });

  it('allows an authenticated user with an accepted role', () => {
    signIn('SHOPKEEPER');
    expect(new AuthGuard(router, session, refresh).canActivate(route(['SHOPKEEPER']), state)).toBeTrue();
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  it('redirects an authenticated user with the wrong role', () => {
    signIn('USER');
    expect(new AuthGuard(router, session, refresh).canActivate(route(['ADMIN']), state)).toBe(errorTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/error'], {
      queryParams: { message: 'Access denied.' },
    });
  });
});
