// src/app/services/review.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../environment';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Review } from '../model/review.model';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private readonly baseUrl = `${environment.apiBaseUrl}/ratings`;

  constructor(private http: HttpClient) { }

  /**
   * Fetch reviews for a specific shop.
   * GET /api/reviews?shopId={shopId}
   * @param shopId - The ID of the shop.
   * @returns An Observable of an array of Review objects.
   */
  getReviewsByShop(shopId: string): Observable<Review[]> {
    const headers = this.buildHeaders();
    console.log(`Fetching reviews for Shop ID: ${shopId} from: ${this.baseUrl}`);
    return this.http.get<Review[]>(`${this.baseUrl}/shops/${shopId}`, { headers })
      .pipe(
        tap(reviews => console.log('Reviews retrieved:', reviews)),
        catchError(error => this.handleError(error, 'getReviewsByShop'))
      );
  }


  addReview(
    shopId: string,
    ratingData: { rating: number; review: string; userId: string; userName: string }
  ): Observable<Review> {
    const userId = sessionStorage.getItem('id'); // Retrieve the user ID from session storage
    if (!userId) {
      console.error('User ID is missing from session storage.');
      return throwError(() => new Error('User ID is required'));
    }
  
    const headers = this.buildHeaders(); // Assumes buildHeaders is implemented
    const url = `${this.baseUrl}/add?userId=${userId}&shopId=${shopId}`;
  
    console.log(`Adding new review for Shop ID: ${shopId}`);
    console.log(`Review Data:`, ratingData);
  
    return this.http.post<Review>(url, ratingData, { headers }).pipe(
      tap((newReview) => console.log('Review added successfully:', newReview)),
      catchError((error) => this.handleError(error, 'addReview'))
    );
  }
  
  /**
   * Update an existing review.
   * PUT /api/reviews/{reviewId}
   * @param reviewId - The ID of the review to update.
   * @param updatedReview - The updated review data.
   * @returns An Observable of the updated Review object.
   */
  updateReview(
    userId: string,
    ratingId: string,
    updatedReview: { rating: number; review: string }
  ): Observable<Review> {
    if (!userId) {
      console.error('User ID is missing.');
      return throwError(() => new Error('User ID is required'));
    }
  
    const headers = this.buildHeaders(); // Assumes buildHeaders is implemented
    const url = `${this.baseUrl}/${ratingId}?userId=${userId}`;
  
    console.log(`Updating rating ID: ${ratingId} with data:`, updatedReview);
  
    return this.http.put<Review>(url, updatedReview, { headers }).pipe(
      tap((review) => console.log('Review updated successfully:', review)),
      catchError((error) => this.handleError(error, 'updateReview'))
    );
  }
  

  /**
   * Delete a review.
   * DELETE /api/reviews/{reviewId}
   * @param reviewId - The ID of the review to delete.
   * @returns An Observable of type void upon successful deletion.
   */
  deleteReview(shopId: string, ratingId: string): Observable<void> {
    const userId = sessionStorage.getItem('id'); // Get the user ID from session storage
    if (!userId) {
      console.error('User ID is missing from session storage.');
      return throwError(() => new Error('User ID is required'));
    }
  
    const headers = this.buildHeaders(); // Assumes buildHeaders is implemented
    const url = `${this.baseUrl}/${ratingId}?userId=${userId}&shopId=${shopId}`;
  
    console.log(`Deleting shop ID: ${shopId}`);
    console.log(`Deleting rating ID: ${ratingId}`);
    console.log(`User ID: ${userId}`);
  
    return this.http.delete<void>(url, { headers }).pipe(
      tap(() => console.log(`Review deleted: shopId=${shopId}, ratingId=${ratingId}`)),
      catchError((error) => this.handleError(error, 'deleteReview'))
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
  getReviewsCount(userId: string): Observable<number> {
    const headers = this.buildHeaders(); // Assumes buildHeaders is implemented
    return this.http.get<number>(`${this.baseUrl}/count/${userId}`, { headers });
  }
  

  /**
   * Handle HTTP errors.
   * @param error - The HTTP error response.
   * @param methodName - The method where the error occurred.
   * @returns An Observable throwing a user-friendly error message.
   */
  private handleError(error: HttpErrorResponse, methodName: string) {
    console.error(`HTTP Error in ReviewService.${methodName}:`, error);
    
    let errorMessage = 'An error occurred while processing your request. Please try again later.';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `A network error occurred: ${error.error.message}`;
    } else {
      // Backend returned an unsuccessful response code
      switch (error.status) {
        case 400:
          // Bad Request
          if (typeof error.error === 'object') {
            const validationErrors = Object.entries(error.error).map(
              ([field, message]) => `${field}: ${message}`
            ).join('\n');
            errorMessage = `Validation errors:\n${validationErrors}`;
          } else if (typeof error.error === 'string') {
            errorMessage = `Error: ${error.error}`;
          }
          break;
        case 401:
          // Unauthorized
          errorMessage = 'Unauthorized. Please log in again.';
          break;
        case 403:
          // Forbidden
          errorMessage = 'You do not have permission to perform this action.';
          break;
        case 404:
          // Not Found
          errorMessage = 'The requested resource was not found.';
          break;
        case 500:
          // Internal Server Error
          errorMessage = 'An internal server error occurred. Please try again later.';
          break;
        default:
          // Other errors
          errorMessage = `Unexpected error: ${error.statusText} (${error.status})`;
      }
    }

    return throwError(() => new Error(errorMessage));
  }
}
