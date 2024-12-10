// src/app/services/shop.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { ShopResponse } from '../model/shop-response.model';

@Injectable({
  providedIn: 'root'
})
export class ShopService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getAllShops(): Observable<ShopResponse[]> {
    return this.http.get<ShopResponse[]>(`${this.baseUrl}/shops`).pipe(
      tap((shops) => console.log('Fetched shops:', shops)),
      catchError(this.handleError)
    );
  }

  toggleShopStatus(shopId: string): Observable<ShopResponse> {
    return this.http.put<ShopResponse>(`${this.baseUrl}/shops/${shopId}/toggle-status`, null).pipe(
      tap((updatedShop) => console.log(`Toggled status for shop: ${updatedShop.name}`, updatedShop)),
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    console.error('ShopService encountered an error:', error);
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `A network error occurred: ${error.error.message}`;
    } else {
      errorMessage = `Backend returned code ${error.status}, body was: ${JSON.stringify(error.error)}`;
    }
    return throwError(() => new Error(errorMessage));
  }
}
