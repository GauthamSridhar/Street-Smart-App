import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environment';
import { Shop } from '../model/shop.model';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ShopService {
  private http = inject(HttpClient);

  getShops(): Observable<Shop[]> {
    return this.http.get<Shop[]>(`${environment.apiBaseUrl}/shops`);
  }
}
