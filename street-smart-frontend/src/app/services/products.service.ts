// src/app/services/products.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../environment'; // Corrected import path
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { AddProductRequest } from '../model/add-product-request.model';
import { ProductResponseDTO } from '../model/product-response-dto.model';

@Injectable({
  providedIn: 'root'
})
export class ProductsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/products`;

  constructor(private http: HttpClient) {
    console.log('ProductsService initialized with baseUrl:', this.baseUrl);
  }

  /**
   * Fetch products for a specific shop.
   * GET /api/products?shopId={shopId}
   * @param shopId - The ID of the shop.
   */
  getProductsByShop(shopId: string): Observable<ProductResponseDTO[]> {
    const headers = this.buildHeaders();
    const params = new HttpParams().set('shopId', shopId);
    console.log(`Fetching products for Shop ID: ${shopId} from: ${this.baseUrl}`);
    return this.http.get<ProductResponseDTO[]>(`${this.baseUrl}`, { headers, params })
      .pipe(
        tap(products => console.log('Products retrieved:', products)),
        catchError(error => this.handleError(error, 'getProductsByShop'))
      );
  }

  /**
   * Add a new product to a shop.
   * POST /api/products?shopId={shopId}
   * @param shopId - The ID of the shop.
   * @param product - The product to add.
   */
  addProduct(shopId: string, product: AddProductRequest): Observable<ProductResponseDTO> {
    const headers = this.buildHeaders();
    const params = new HttpParams().set('shopId', shopId);
    console.log(`Adding product to Shop ID: ${shopId} with data:`, product);
    return this.http.post<ProductResponseDTO>(`${this.baseUrl}`, product, { headers, params })
      .pipe(
        tap(addedProduct => console.log('Product added:', addedProduct)),
        catchError(error => this.handleError(error, 'addProduct'))
      );
  }

  /**
   * Toggle the availability status of a product.
   * PUT /api/products/{productId}
   * @param productId - The ID of the product to update.
   * @param available - The new availability status.
   */
  toggleAvailability(productId: string, available: boolean): Observable<ProductResponseDTO> {
    const headers = this.buildHeaders();
    const body = { available };
    console.log(`Toggling availability for Product ID: ${productId} to ${available}`);
    return this.http.put<ProductResponseDTO>(`${this.baseUrl}/${productId}`, body, { headers })
      .pipe(
        tap(updatedProduct => console.log('Product availability updated:', updatedProduct)),
        catchError(error => this.handleError(error, 'toggleAvailability'))
      );
  }

  /**
   * Update an existing product.
   * PUT /api/products/{productId}
   * @param productId - The ID of the product to update.
   * @param updatedProduct - The updated product data.
   */
  updateProduct(productId: string, updatedProduct: Partial<AddProductRequest>): Observable<ProductResponseDTO> {
    const headers = this.buildHeaders();
    console.log(`Updating Product ID: ${productId} with data:`, updatedProduct);
    return this.http.put<ProductResponseDTO>(`${this.baseUrl}/${productId}`, updatedProduct, { headers })
      .pipe(
        tap(product => console.log('Product updated:', product)),
        catchError(error => this.handleError(error, 'updateProduct'))
      );
  }

  /**
   * Delete a product from the shop.
   * DELETE /api/products/{productId}
   * @param productId - The ID of the product to delete.
   */
  deleteProduct(productId: string): Observable<void> {
    const headers = this.buildHeaders();
    console.log(`Deleting Product ID: ${productId}`);
    return this.http.delete<void>(`${this.baseUrl}/${productId}`, { headers })
      .pipe(
        tap(() => console.log('Product deleted:', productId)),
        catchError(error => this.handleError(error, 'deleteProduct'))
      );
  }
  getProductsCount(userId: string): Observable<number> {
    const headers = this.buildHeaders();
    return this.http.get<number>(`${this.baseUrl}/count/${userId}`,{headers});
  }
  

  /**
   * Helper method to build HTTP headers with Bearer token.
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
   */
  private handleError(error: HttpErrorResponse, methodName: string) {
    console.error(`HTTP Error in ProductsService.${methodName}:`, error);
    
    let errorMessage = 'An error occurred while processing your request. Please try again later.';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `A network error occurred: ${error.error.message}`;
    } else if (error.status === 400) {
      // Backend validation error
      if (typeof error.error === 'object') {
        const validationErrors = Object.entries(error.error).map(
          ([field, message]) => `${field}: ${message}`
        ).join('\n');
        errorMessage = `Validation errors:\n${validationErrors}`;
      } else if (typeof error.error === 'string') {
        errorMessage = `Error: ${error.error}`;
      }
    } else if (error.status === 401) {
      errorMessage = 'Unauthorized. Please log in again.';
    } else if (error.status === 404) {
      errorMessage = 'Resource not found.';
    }

    return throwError(() => new Error(errorMessage));
  }
}
