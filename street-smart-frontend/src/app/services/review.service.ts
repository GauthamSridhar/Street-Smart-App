import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environment';
import { Review } from '../model/review.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private http = inject(HttpClient);

  getReviews(shopId: string): Observable<Review[]> {
    return this.http.get<Review[]>(`${environment.apiBaseUrl}/shops/${shopId}/reviews`);
  }

  addReview(shopId: string, review: Partial<Review>): Observable<Review> {
    return this.http.post<Review>(`${environment.apiBaseUrl}/shops/${shopId}/reviews`, review);
  }

  updateReview(shopId: string, reviewId: string, review: Partial<Review>): Observable<Review> {
    return this.http.put<Review>(`${environment.apiBaseUrl}/shops/${shopId}/reviews/${reviewId}`, review);
  }

  deleteReview(shopId: string, reviewId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/shops/${shopId}/reviews/${reviewId}`);
  }
}
