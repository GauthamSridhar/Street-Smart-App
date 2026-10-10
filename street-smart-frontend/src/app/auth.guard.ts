import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
  UrlTree,
} from '@angular/router';
import { SessionService } from './services/session.service';
import { RefreshService } from './services/refresh.service';
import { Observable, map, catchError, of } from 'rxjs';
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(
    private router: Router,
    private session: SessionService,
    private refresh: RefreshService,
  ) {}
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree | Observable<boolean | UrlTree> {
    if (!this.session.token && this.session.storedToken) {
      const generation = this.session.generation;
      return this.refresh.renew().pipe(
        map(() => this.authorize(route)),
        catchError(() => {
          if (generation === this.session.generation) this.session.clear();
          return of(this.router.createUrlTree(['/login']));
        }),
      );
    }
    return this.authorize(route);
  }
  private authorize(route: ActivatedRouteSnapshot): boolean | UrlTree {
    if (!this.session.token || !this.session.role) return this.router.createUrlTree(['/login']);
    const roles = route.data['roles'] as string[] | undefined;
    return !roles || roles.includes(this.session.role)
      ? true
      : this.router.createUrlTree(['/error'], { queryParams: { message: 'Access denied.' } });
  }
}
