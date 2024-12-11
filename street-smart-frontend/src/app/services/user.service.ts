// src/app/services/user.service.ts

import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError, of } from 'rxjs';
import { environment } from '../environment';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { catchError, tap, shareReplay } from 'rxjs/operators';
import { UserResponse } from '../model/user-response.model';

type GeolocationPermissionState = 'granted' | 'denied' | 'prompt';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly baseUrl = `${environment.apiBaseUrl}/users`;

  private locationSubject = new BehaviorSubject<GeolocationPosition | null>(null);
  location$: Observable<GeolocationPosition | null> = this.locationSubject.asObservable();

  private permissionSubject = new BehaviorSubject<GeolocationPermissionState>('prompt');
  permission$: Observable<GeolocationPermissionState> = this.permissionSubject.asObservable();

  private watchId: number | null = null;

  // Simple cache to store user details
  private userCache = new Map<string, Observable<UserResponse>>();

  constructor(private http: HttpClient) {
    const storedPermission = localStorage.getItem('geolocationPermission') as GeolocationPermissionState | null;
    if (storedPermission) {
      this.permissionSubject.next(storedPermission);
      if (storedPermission === 'granted') {
        this.startTracking();
      }
    } else {
      this.checkLocationPermission();
    }
  }

  ngOnDestroy(): void {
    this.stopTracking();
  }

  /**
   * Fetch user details by user ID.
   * GET /api/users/{userId}
   * @param userId - The ID of the user.
   * @returns An Observable of the UserResponse object.
   */
  getUser(userId: string): Observable<UserResponse> {
    if (this.userCache.has(userId)) {
      return this.userCache.get(userId)!;
    }

    const headers = this.buildHeaders();
    console.log(`Fetching user details for User ID: ${userId} from: ${this.baseUrl}/${userId}`);
    const user$ = this.http.get<UserResponse>(`${this.baseUrl}/${userId}`, { headers })
      .pipe(
        tap(user => console.log('User fetched:', user)),
        shareReplay(1),
        catchError(error => this.handleError(error, 'getUser'))
      );

    this.userCache.set(userId, user$);
    return user$;
  }

  /**
   * Check the current geolocation permission status.
   */
  async checkLocationPermission(): Promise<void> {
    if (!navigator.permissions) {
      this.permissionSubject.next('prompt');
      return;
    }

    try {
      const permissionStatus = await navigator.permissions.query({
        name: 'geolocation' as PermissionName,
      });

      // Handle initial state
      if (permissionStatus.state === 'granted') {
        this.permissionSubject.next('granted');
        localStorage.setItem('geolocationPermission', 'granted');
        this.startTracking();
      } else if (permissionStatus.state === 'denied') {
        this.permissionSubject.next('denied');
        localStorage.setItem('geolocationPermission', 'denied');
      } else {
        this.permissionSubject.next('prompt');
      }

      // Listen for changes in permission state
      permissionStatus.onchange = () => {
        this.permissionSubject.next(permissionStatus.state);
        localStorage.setItem('geolocationPermission', permissionStatus.state);

        // Handle state changes dynamically
        if (permissionStatus.state === 'granted') {
          this.startTracking();
        } else if (permissionStatus.state === 'denied') {
          this.stopTracking();
        }
      };
    } catch (error) {
      console.error('Error checking location permission:', error);
      this.permissionSubject.next('prompt');
    }
  }

  /**
   * Request access to the user's geolocation.
   * @returns A Promise that resolves when access is granted or rejects if denied.
   */
  requestLocationAccess(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        this.permissionSubject.next('denied');
        reject('Geolocation is not supported by your browser.');
        return;
      }

      // Prompt for location access
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.locationSubject.next(position);
          this.permissionSubject.next('granted');
          localStorage.setItem('geolocationPermission', 'granted');
          this.startTracking();
          resolve();
        },
        (error) => {
          console.error('Error obtaining location:', error);
          this.permissionSubject.next('denied');
          localStorage.setItem('geolocationPermission', 'denied');
          reject(error);
        }
      );
    });
  }

  /**
   * Start tracking the user's geolocation.
   */
  startTracking(): void {
    if (!navigator.geolocation) {
      console.error('Geolocation is not supported by your browser.');
      return;
    }

    if (this.watchId !== null) {
      // Already tracking
      return;
    }

    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        this.locationSubject.next(position);
      },
      (error) => {
        console.error('Error watching position:', error);
        this.permissionSubject.next('denied');
        localStorage.setItem('geolocationPermission', 'denied');
      },
      {
        enableHighAccuracy: true,
        maximumAge: 0,
        timeout: 5000,
      }
    );
  }

  /**
   * Stop tracking the user's geolocation.
   */
  stopTracking(): void {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
      this.locationSubject.next(null);
    }
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
    console.error(`HTTP Error in UserService.${methodName}:`, error);
    
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
