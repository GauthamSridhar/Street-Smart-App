import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map, of, throwError } from 'rxjs';
import { environment } from '../environment';
import { Review } from '../model/review.model';
import { SessionService } from './session.service';
interface RatingResponse {
  id: string;
  userId: string;
  shopId: string;
  rating: number;
  review: string;
  updatedAt: string;
}
const toReview = (r: RatingResponse): Review => ({ ...r, date: r.updatedAt });
@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly url = environment.apiBaseUrl + '/ratings';
  constructor(
    private http: HttpClient,
    private session: SessionService,
  ) {}
  getReviewsByShop(shopId: string, page = 0, size = 20) {
    return this.http
      .get<RatingResponse[]>(this.url + '/shops/' + shopId, { params: { page, size } })
      .pipe(map((rows) => rows.map(toReview)));
  }
  summary(shopId: string) {
    return this.http.get<{ count: number; average: number }>(this.url + '/summary/' + shopId);
  }
  mine(shopId: string) {
    return this.http.get<RatingResponse>(this.url + '/mine/' + shopId).pipe(
      map(toReview),
      catchError((error) =>
        error instanceof HttpErrorResponse && error.status === 404
          ? of(null)
          : throwError(() => error),
      ),
    );
  }
  addReview(shopId: string, value: { rating: number; review: string }) {
    return this.http
      .post<RatingResponse>(
        this.url + '/add',
        { rating: Number(value.rating), review: value.review },
        { params: { userId: this.session.id, shopId } },
      )
      .pipe(map(toReview));
  }
  updateReview(userId: string, id: string, value: { rating: number; review: string }) {
    return this.http
      .put<RatingResponse>(
        this.url + '/' + id,
        { rating: Number(value.rating), review: value.review },
        { params: { userId } },
      )
      .pipe(map(toReview));
  }
  deleteReview(shopId: string, id: string) {
    return this.http.delete<void>(this.url + '/' + id, {
      params: { userId: this.session.id, shopId },
    });
  }
  getReviewsCount(shopId: string) {
    return this.http.get<number>(this.url + '/count/' + shopId);
  }
  reportReview(id: string, reason: string) {
    return this.http.post<{ id: string; status: string }>(this.url + '/' + id + '/reports', { reason });
  }
  reports() {
    return this.http.get<Array<{ id: string; ratingId: string; reason: string; review: string; createdAt: string; status: string }>>(this.url + '/reports');
  }
  resolveReport(id: string, action: 'DISMISS' | 'REMOVE') {
    return this.http.post<void>(this.url + '/reports/' + id + '/resolve', { action });
  }
}
