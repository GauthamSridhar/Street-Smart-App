// src/app/services/requests.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../environment';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface ShopApprovalResponseDTO {
  id: string;
  shopId: string;
  approvalStatus: string; // PENDING, APPROVED, REJECTED, etc.
  approved: boolean;
  reason: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class RequestsService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {
    console.log('RequestsService initialized with baseUrl:', this.baseUrl);
  }

  /**
   * Fetch all pending shop approval requests.
   * GET /api/approvals/pending
   */
  getPendingRequests(): Observable<ShopApprovalResponseDTO[]> {
    const headers = this.buildHeaders();
    console.log('Fetching pending approvals from:', `${this.baseUrl}/approvals/pending`);
    console.log('Using headers:', headers);
    return this.http.get<ShopApprovalResponseDTO[]>(`${this.baseUrl}/approvals/pending`, { headers })
      .pipe(
        catchError((error: HttpErrorResponse) => this.handleError(error, 'getPendingRequests'))
      );
  }

  /**
   * Approve a shop.
   * POST /api/approvals/{shopId}/approve
   * @param shopId - The ID of the shop to approve.
   */
  approveRequest(shopId: string): Observable<ShopApprovalResponseDTO> {
    const headers = this.buildHeaders();
    const url = `${this.baseUrl}/approvals/${shopId}/approve`;
    console.log(`Approving shop with ID: ${shopId} at: ${url}`);
    console.log('Using headers:', headers);
    return this.http.post<ShopApprovalResponseDTO>(url, null, { headers })
      .pipe(
        catchError((error: HttpErrorResponse) => this.handleError(error, 'approveRequest'))
      );
  }

  /**
   * Reject a shop with a reason.
   * POST /api/approvals/{shopId}/reject?reason=...
   * @param shopId - The ID of the shop to reject.
   * @param reason - The reason for rejection.
   */
  rejectRequest(shopId: string, reason: string): Observable<ShopApprovalResponseDTO> {
    const token = sessionStorage.getItem('tokenId');  
    if (!token) {
      console.error('No token found in sessionStorage');
      return throwError(() => new Error('No authentication token found'));
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    const params = new HttpParams().set('reason', reason);
    const url = `${this.baseUrl}/approvals/${shopId}/reject`;

    console.log(`Rejecting shop with ID: ${shopId} for reason: "${reason}" at: ${url}`);
    console.log('Using headers:', headers);
    console.log('Using params:', params.toString());

    return this.http.post<ShopApprovalResponseDTO>(url, null, { headers, params })
      .pipe(
        catchError((error: HttpErrorResponse) => this.handleError(error, 'rejectRequest'))
      );
  }
  
  getRequestsCount(userId: string): Observable<number> {
    const headers = this.buildHeaders();
    return this.http.get<number>(`${this.baseUrl}/approvals/pending/count`,{headers});
  }
  /**
   * Helper method to build HTTP headers with Bearer token.
   */
  private buildHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('tokenId');
    console.log('Token retrieved from sessionStorage:', token ? 'Present' : 'Absent');
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
    console.error(`HTTP Error in RequestsService.${methodName}:`, error);
    console.error('Error details:', {
      url: error.url,
      status: error.status,
      message: error.message,
      errorObject: error.error
    });
    return throwError(() => error);
  }
  
}
