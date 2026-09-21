import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environment';
export interface Shop {
  id: string;
  shopId: string;
  shopName: string;
  userId: string;
}
@Injectable({ providedIn: 'root' })
export class FavoritesService {
  private readonly url = environment.apiBaseUrl + '/favorites';
  constructor(private http: HttpClient) {}
  getFavoriteShops(userId: string, page = 0, size = 20) {
    return this.http.get<Shop[]>(this.url + '/user/' + userId, { params: { page, size } });
  }
  isFavorite(shopId: string, userId: string) {
    return this.http.get<boolean>(this.url + '/' + shopId + '/is-favorite', { params: { userId } });
  }
  addFavorite(shopId: string, userId: string) {
    return this.http.post<Shop>(this.url + '/' + shopId, null, { params: { userId } });
  }
  removeFavoriteShop(shopId: string, userId: string) {
    return this.http.delete<void>(this.url + '/' + shopId, { params: { userId } });
  }
  getFavoritesCount(userId: string) {
    return this.http.get<number>(this.url + '/count/' + userId);
  }
}
