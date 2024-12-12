import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { environment } from '../environment';
import { Shop } from '../model/shop.model';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ShopService {
  private readonly baseUrl = `${environment.apiBaseUrl}/shops`;

  constructor(private http: HttpClient) {
    console.log('ShopService initialized with baseUrl:', this.baseUrl);
  }

  /**
   * Fetch all shops.
   * GET /api/shops
   * @returns An Observable of an array of Shop objects.
   */
  getShops(): Observable<Shop[]> {
    const headers = this.buildHeaders();
    console.log(`Fetching all shops from: ${this.baseUrl}`);
    return this.http.get<Shop[]>(`${this.baseUrl}`, { headers })
      .pipe(
        tap(shops => console.log('Shops retrieved:', shops)),
        catchError(error => this.handleError(error, 'getShops'))
      );
  }

  /**
   * Fetch a single shop by its ID.
   * GET /api/shops/{shopId}
   * @param shopId - The ID of the shop to fetch.
   * @returns An Observable of the Shop object.
   */
  getShopById(shopId: string): Observable<Shop> {
    const headers = this.buildHeaders();
    console.log(`Fetching Shop ID: ${shopId} from: ${this.baseUrl}`);
    return this.http.get<Shop>(`${this.baseUrl}/${shopId}`, { headers })
      .pipe(
        tap(shop => console.log('Shop retrieved:', shop)),
        catchError(error => this.handleError(error, 'getShopById'))
      );
  }

  /**
   * Fetch the shop owned by a specific shopkeeper.
   * GET /api/shops/shopkeeper/{shopkeeperId}
   * @param shopkeeperId - The ID of the shopkeeper.
   * @returns An Observable of the Shop object.
   */
  getShopByShopkeeperId(shopkeeperId: string): Observable<Shop> {
    const headers = this.buildHeaders();
    console.log(`Fetching shop for shopkeeper ID: ${shopkeeperId}`);
    return this.http.get<Shop>(`${this.baseUrl}/shopkeeper/${shopkeeperId}`, { headers })
      .pipe(
        tap(shop => console.log('Shop retrieved for shopkeeper:', shop)),
        catchError(error => this.handleError(error, 'getShopByShopkeeperId'))
      );
  }

  /**
   * Add a new shop.
   * POST /api/shops
   * @param shop - The shop data to add.
   * @returns An Observable of the added Shop object.
   */
  addShop(shop: Partial<Shop>): Observable<Shop> {
    const headers = this.buildHeaders();
    console.log(`Adding new shop with data:`, shop);
    return this.http.post<Shop>(`${this.baseUrl}`, shop, { headers })
      .pipe(
        tap(addedShop => console.log('Shop added:', addedShop)),
        catchError(error => this.handleError(error, 'addShop'))
      );
  }

  /**
   * Update an existing shop.
   * PUT /api/shops/{shopId}
   * @param shopId - The ID of the shop to update.
   * @param updatedShop - The updated shop data.
   * @returns An Observable of the updated Shop object.
   */
  updateShop(shopId: string, updatedShop: Partial<Shop>): Observable<Shop> {
    const headers = this.buildHeaders();
    console.log(`Updating Shop ID: ${shopId} with data:`, updatedShop);
    return this.http.put<Shop>(`${this.baseUrl}/${shopId}`, updatedShop, { headers })
      .pipe(
        tap(shop => console.log('Shop updated:', shop)),
        catchError(error => this.handleError(error, 'updateShop'))
      );
  }

  /**
   * Toggle the active/inactive status of a shop.
   * PUT /api/shops/{shopId}/toggle-status
   * @param shopId - The ID of the shop to toggle the status.
   * @returns An Observable of the updated Shop object.
   */
  toggleShopStatus(shopId: string): Observable<Shop> {
    const headers = this.buildHeaders();
    console.log(`Toggling status for Shop ID: ${shopId}`);
    return this.http.put<Shop>(`${this.baseUrl}/${shopId}/toggle-status`, null, { headers })
      .pipe(
        tap(shop => console.log('Shop status toggled:', shop)),
        catchError(error => this.handleError(error, 'toggleShopStatus'))
      );
  }

  /**
   * Delete a shop.
   * DELETE /api/shops/{shopId}
   * @param shopId - The ID of the shop to delete.
   * @returns An Observable of type void upon successful deletion.
   */
  deleteShop(shopId: string): Observable<void> {
    const headers = this.buildHeaders();
    console.log(`Deleting Shop ID: ${shopId} from: ${this.baseUrl}`);
    return this.http.delete<void>(`${this.baseUrl}/${shopId}`, { headers })
      .pipe(
        tap(() => console.log('Shop deleted:', shopId)),
        catchError(error => this.handleError(error, 'deleteShop'))
      );
  }

  /**
   * Helper method to build HTTP headers with Bearer token.
   * @returns HttpHeaders object with necessary headers.
   */
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
   * Handle HTTP errors.
   * @param error - The HTTP error response.
   * @param methodName - The method where the error occurred.
   * @returns An Observable throwing a user-friendly error message.
   */
  private handleError(error: HttpErrorResponse, methodName: string) {
    console.error(`HTTP Error in ShopService.${methodName}:`, error);

    let errorMessage = 'An error occurred while processing your request. Please try again later.';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `A network error occurred: ${error.error.message}`;
    } else {
      // Backend returned an unsuccessful response code
      switch (error.status) {
        case 400:
          errorMessage = 'Bad request. Please check the input data.';
          break;
        case 401:
          errorMessage = 'Unauthorized. Please log in again.';
          break;
        case 403:
          errorMessage = 'Forbidden. You do not have access to this resource.';
          break;
        case 404:
          errorMessage = 'Resource not found.';
          break;
        case 500:
          errorMessage = 'Internal server error. Please try again later.';
          break;
        default:
          errorMessage = `Unexpected error: ${error.statusText} (${error.status})`;
      }
    }

    return throwError(() => new Error(errorMessage));
  }
}
