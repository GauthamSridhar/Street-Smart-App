import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
  UrlTree,
} from '@angular/router';
import { SessionService } from './services/session.service';
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(
    private router: Router,
    private session: SessionService,
  ) {}
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    if (!this.session.token || !this.session.role) return this.router.createUrlTree(['/login']);
    const roles = route.data['roles'] as string[] | undefined;
    return !roles || roles.includes(this.session.role)
      ? true
      : this.router.createUrlTree(['/error'], { queryParams: { message: 'Access denied.' } });
  }
}
