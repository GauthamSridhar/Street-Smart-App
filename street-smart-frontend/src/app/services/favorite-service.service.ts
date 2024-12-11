// src/app/services/favorites.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environment';

export interface Shop {
  shopId: string;
  id: string | null;
  shopName: string;
  userId: string;
}

@Injectable({
  providedIn: 'root'
})
export class FavoritesService {
  private baseUrl = environment.apiBaseUrl; // e.g. https://your-backend-domain.com/api

  constructor(private http: HttpClient) {}

  /**
   * Helper to create an HttpHeaders object with authorization token.
   */
  private getAuthHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('tokenId');
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    console.log('Token:', token);
    
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  /**
   * Fetches the list of favorite shops for the given user.
   * 
   * Backend endpoint: GET /api/favorites/user/{userId}
   * 
   * @param userId - The ID of the user
   * @returns Observable<Shop[]>
   */
  getFavoriteShops(userId: string): Observable<Shop[]> {
    return this.http.get<Shop[]>(`${this.baseUrl}/favorites/user/${userId}`, {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Checks if a specific shop is in the user's favorites.
   *
   * Backend endpoint: GET /api/favorites/{shopId}/is-favorite?userId={userId}
   * 
   * @param shopId - The ID of the shop to check.
   * @param userId - The ID of the user
   * @returns Observable<boolean>
   */
  isFavorite(shopId: string, userId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/favorites/${shopId}/is-favorite?userId=${userId}`,{
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Adds a shop to the user's favorites.
   * 
   * Backend endpoint: POST /api/favorites/{shopId}?userId={userId}
   * 
   * @param shopId - The ID of the shop to add.
   * @param userId - The ID of the user
   * @returns Observable<any>
   */
  addFavorite(shopId: string, userId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/favorites/${shopId}?userId=${userId}`, null, {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Removes a shop from the user's favorites.
   * 
   * Backend endpoint: DELETE /api/favorites/{shopId}?userId={userId}
   * @PathVariable ratingId
   * @param shopId - The ID of the shop to remove.
   * @param userId - The ID of the user
   * @returns Observable<any>
   */
  removeFavoriteShop(shopId: string, userId: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/favorites/${shopId}?userId=${userId}`, {
      headers: this.getAuthHeaders()
    });
  }

  getFavoritesCount(userId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/favorites/count/${userId}`, {
      headers: this.getAuthHeaders()
    });
  }
  
}
