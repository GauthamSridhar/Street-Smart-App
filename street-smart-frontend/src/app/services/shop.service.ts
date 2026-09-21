import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environment';
import { Shop, ShopEdit } from '../model/shop.model';
import { Page } from '../model/product-search';
@Injectable({ providedIn: 'root' })
export class ShopService {
  private readonly baseUrl = environment.apiBaseUrl + '/shops';
  constructor(private http: HttpClient) {}
  getShops() {
    return this.http.get<Shop[]>(this.baseUrl);
  }
  getShopById(id: string) {
    return this.http.get<Shop>(this.baseUrl + '/' + id);
  }
  getShopByShopkeeperId(id: string) {
    return this.http.get<Shop>(this.baseUrl + '/owner/' + id);
  }
  search(page = 0, size = 20) {
    return this.http.get<Page<Shop>>(this.baseUrl + '/search', { params: { page, size } });
  }
  updateShop(id: string, shop: ShopEdit) {
    const { name, description, address, latitude, longitude, category } = shop;
    return this.http.put<Shop>(this.baseUrl + '/' + id, {
      name,
      description,
      address,
      latitude,
      longitude,
      category,
      ...(shop.openingHours !== undefined ? { openingHours: shop.openingHours } : {}),
    });
  }
  toggleShopStatus(id: string) {
    return this.http.put<Shop>(this.baseUrl + '/' + id + '/toggle-status', null);
  }
  resubmit(id: string) {
    return this.http.post<Shop>(this.baseUrl + '/' + id + '/resubmit', null);
  }
}
