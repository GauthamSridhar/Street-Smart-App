// src/app/services/shop.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';
import { Shop, ShopResponse } from '../model/shop-response.model';

@Injectable({
  providedIn: 'root'
})
export class ShopService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Fetches all shops and maps them to ShopResponse objects.
   */
  getAllShops(): Observable<ShopResponse[]> {
    const headers = this.buildHeaders();



    return this.http.get<Shop[]>(`${this.baseUrl}/shops`,{headers}).pipe(
      tap((shops) => console.log('Fetched shops:', shops)),
      map((shops) => shops.map(this.transformToShopResponse)),
      catchError(this.handleError)
    );
  }

  /**
   * Toggles the status of a shop.
   */
  toggleShopStatus(shopId: string): Observable<ShopResponse> {
    const headers = this.buildHeaders();

    return this.http.put<ShopResponse>(`${this.baseUrl}/shops/${shopId}/toggle-status`, {headers}).pipe(
      tap((updatedShop) => console.log(`Toggled status for shop: ${updatedShop.name}`, updatedShop)),
      catchError(this.handleError)
    );
  }

  /**
   * Transforms a Shop object to a ShopResponse object by providing default values for missing properties.
   */
  private transformToShopResponse(shop: Shop): ShopResponse {
    return {
      id: shop.id,
      name: shop.name,
      description: shop.description,
      address: shop.address,
      latitude: shop.latitude,
      longitude: shop.longitude,
      status: shop.status || 'Unknown', // Provide a default value if 'status' is missing
      ownerId: shop.ownerId || 'N/A',    // Provide a default value if 'ownerId' is missing
      category: shop.category || 'Uncategorized',
      products: shop.products || [],
      images: shop.images || [],
      ratings: shop.ratings || []
    };
  }

  private buildHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('tokenId');
    if (!token) {
      console.warn('No token found in sessionStorage');
    }
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  /**
   * Handles HTTP errors.
   */
  private handleError(error: HttpErrorResponse) {
    console.error('ShopService encountered an error:', error);
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `A network error occurred: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 404:
          errorMessage = 'The requested shop was not found.';
          break;
        case 403:
          errorMessage = 'You do not have permission to perform this action.';
          break;
        case 500:
          errorMessage = 'An internal server error occurred.';
          break;
        default:
          errorMessage = `Unexpected error: ${error.message}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}
