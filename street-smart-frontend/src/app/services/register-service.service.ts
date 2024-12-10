// src/app/services/register.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RegisterPayload } from '../model/registerPayload';
import { RegisterResponse } from '../model/RegisterResponse';
import { environment } from '../environment';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {
  private readonly registerUrl = `${environment.apiBaseUrl}/users/register`; // Ensure this endpoint is correct

  constructor(private httpClient: HttpClient) {
    console.log(`RegisterService initialized with registerUrl: ${this.registerUrl}`);
  }

  /**
   * Register a new user by sending a POST request to the backend.
   * @param payload - The registration data payload.
   * @returns Observable of RegisterResponse.
   */
  registerUser(payload: RegisterPayload): Observable<RegisterResponse> {
    console.log('Sending registration request to backend with payload:', payload);
    return this.httpClient.post<RegisterResponse>(this.registerUrl, payload)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          console.error('Error occurred during registration HTTP request:', error);
          return throwError(() => error);
        })
      );
  }
}
