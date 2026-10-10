import { Injectable, inject } from '@angular/core';
import { HttpBackend, HttpClient } from '@angular/common/http';
import { Observable, finalize, map, shareReplay } from 'rxjs';
import { SessionService } from './session.service';
import { LoginResponse } from '../model/loginResponse';
import { environment } from '../environment';

@Injectable({ providedIn: 'root' })
export class RefreshService {
  private http = new HttpClient(inject(HttpBackend));
  private session = inject(SessionService);
  private pending?: Observable<string>;

  renew(): Observable<string> {
    if (!this.pending) {
      const generation = this.session.generation;
      const userId = sessionStorage.getItem('id');
      this.pending = this.http.post<LoginResponse>(environment.apiBaseUrl + '/users/refresh', {}, {
        withCredentials: true, headers: { 'X-Requested-With': 'StreetSmart' },
      }).pipe(
        map(login => {
          if (generation !== this.session.generation) throw new Error('Session changed during renewal');
          if (login.id !== userId) throw new Error('A different account is signed in in this browser');
          this.session.save(login);
          return login.jwt;
        }),
        finalize(() => { this.pending = undefined; }),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }
    return this.pending;
  }
}
