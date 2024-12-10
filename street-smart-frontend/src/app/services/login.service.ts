// src/app/services/login.service.ts

import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, throwError } from 'rxjs';
import { ErrorHandlerService } from './error-handler.service';
import { LoginForm } from '../model/loginForm';
import { environment } from '../environment';
import { LoginResponse } from '../model/loginResponse';
import { ShopDetails } from '../model/shopDetails';
import { ShopRegistrationResponse } from '../model/ShopRegistrationResponse';

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(
    private httpClient: HttpClient,
    private router: Router,
    private errorHandler: ErrorHandlerService
  ) {
    console.log('LoginService initialized with baseUrl:', this.baseUrl);
  }

  /**
   * Validate user credentials by sending a POST request to the backend.
   * @param credentials - User credentials containing identifier and password.
   */
  validateUser(credentials: LoginForm): Observable<LoginResponse> {
    console.log('Sending login request with credentials:', credentials);
    return this.httpClient.post<LoginResponse>(`${this.baseUrl}/users/login`, credentials)
      .pipe(
        catchError((err: HttpErrorResponse) => {
          console.error('Error occurred during login HTTP request:', err);
          this.errorHandler.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Fetch shop details associated with a shopkeeper's ownerId.
   * Sends a GET request with Bearer token in Authorization header.
   * @param ownerId - ID of the shopkeeper.
   */
  getShopDetails(ownerId: string): Observable<ShopDetails> {
    const token = sessionStorage.getItem('tokenId');
    console.log(`Fetching shop details for owner ID: ${ownerId}`);
    console.log('Retrieved token from sessionStorage:', token);

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : ''
    });
    console.log('Request headers for shop details:', headers);

    return this.httpClient.get<ShopDetails>(`${this.baseUrl}/shops/owner/${ownerId}`, { headers })
      .pipe(
        catchError((err: HttpErrorResponse) => {
          console.error('Error occurred while fetching shop details:', err);
          this.errorHandler.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Register a new shop for a shopkeeper.
   * Sends a POST request with Bearer token in Authorization header.
   * @param ownerId - ID of the shopkeeper.
   * @param shopData - Data for the new shop.
   */
  registerShop(ownerId: string, shopData: Partial<ShopDetails>): Observable<ShopRegistrationResponse> {
    console.log(`Registering shop for owner ID: ${ownerId} with data:`, shopData);
    const token = sessionStorage.getItem('tokenId');
    console.log('Retrieved token from sessionStorage:', token);

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : ''
    });
    console.log('Request headers for shop registration:', headers);

    const payload = { ownerId, ...shopData };
    console.log('Payload being sent for shop registration:', payload);

    return this.httpClient.post<ShopRegistrationResponse>(`${this.baseUrl}/shops/register`, payload, { headers })
      .pipe(
        catchError((err: HttpErrorResponse) => {
          console.error('Error occurred during shop registration:', err);
          this.errorHandler.handleError(err);
          return throwError(() => err);
        })
      );
  }

  /**
   * Logout the current user by clearing session storage and redirecting to login.
   */
  logout(): void {
    console.log('Logging out user. Clearing session storage.');
    sessionStorage.clear();
    this.router.navigate(['/login']).then(() => {
      console.log('User logged out and redirected to login.');
    }).catch(err => {
      console.error('Error during logout redirection:', err);
    });
  }
}
