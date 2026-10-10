import { Injectable, inject } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, throwError, switchMap } from 'rxjs';
import { environment } from '../environment';
import { SessionService } from '../services/session.service';
import { RefreshService } from '../services/refresh.service';
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private session = inject(SessionService);
  private router = inject(Router);
  private refresh = inject(RefreshService);
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const base = new URL(environment.apiBaseUrl, document.baseURI);
    const target = new URL(request.url, document.baseURI);
    const isApi =
      target.origin === base.origin &&
      (target.pathname === base.pathname ||
        target.pathname.startsWith(base.pathname.replace(/\/$/, '') + '/'));
    if (!isApi) return next.handle(request);
    const publicRequest = [
      '/users/login',
      '/users/refresh',
      '/users/register',
      '/sms/send',
      '/sms/verify',
      '/sms/config',
    ].some((path) => target.pathname === base.pathname.replace(/\/$/, '') + path);
    const token = publicRequest ? null : this.session.storedToken;
    const headers = token
      ? request.headers.set('Authorization', 'Bearer ' + token)
      : request.headers.delete('Authorization');
    const send = (bearer: string | null) => next.handle(request.clone({
      headers: bearer ? request.headers.set('Authorization', 'Bearer ' + bearer) : headers,
      withCredentials: true,
    }));
    const fail = (error: unknown) => {
      if (this.session.storedToken === token || !this.session.storedToken) {
        this.session.clear();
        void this.router.navigate(['/login'], { queryParams: { expired: 'true' } });
      }
      return throwError(() => error);
    };
    if (!publicRequest && token && !this.session.token)
      return this.refresh.renew().pipe(catchError(fail), switchMap(send));
    return send(token).pipe(
      catchError((error) => {
        // A late response from a previous session must not log out a newly signed-in user.
        if (
          !publicRequest &&
          error instanceof HttpErrorResponse &&
          error.status === 401 &&
          token && this.session.storedToken === token
        ) {
          return this.refresh.renew().pipe(catchError(fail), switchMap(send));
        }
        return throwError(() => error);
      }),
    );
  }
}
