import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../environment';

interface ShopRegistrationRequest {
  name: string;
  description: string;
  address: string;
  latitude: number;
  longitude: number;
  category: string;
}

interface ShopResponse {
  id: string;
  name: string;
  description: string;
  address: string;
  latitude: number;
  longitude: number;
  status: string;
  category: string;
}

@Injectable({
  providedIn: 'root'
})
export class ShopRegistrationService {
  private apiUrl = environment.apiBaseUrl + '/shops/register';

  constructor(private http: HttpClient) {}

  registerShop(shopData: ShopRegistrationRequest): Observable<ShopResponse> {
    const token = sessionStorage.getItem('tokenId');
    console.log('Sending shop registration request with payload:', shopData);
    console.log('Token:', token);
    
    const ownderId= sessionStorage.getItem('id');
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization:'Bearer '+token
    });

    return this.http.post<ShopResponse>(this.apiUrl+'?userId='+ownderId, shopData, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      errorMessage = `Server Error: ${error.status} - ${error.message}`;
    }
    console.error('HTTP Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
