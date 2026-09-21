import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environment';
import { Shop, ShopEdit } from '../model/shop.model';
import { SessionService } from './session.service';
@Injectable({ providedIn: 'root' })
export class ShopRegistrationService {
  constructor(
    private http: HttpClient,
    private session: SessionService,
  ) {}
  registerShop(shop: ShopEdit) {
    return this.http.post<Shop>(environment.apiBaseUrl + '/shops/register', shop, {
      params: { userId: this.session.id },
    });
  }
}
