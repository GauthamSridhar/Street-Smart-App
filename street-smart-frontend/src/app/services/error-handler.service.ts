import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {
  constructor(private router: Router) {}

  /**
   * Handle HTTP errors by navigating to a dedicated error page or displaying messages.
   * @param error - The HTTP error response.
   */
  handleError(error: HttpErrorResponse): void {
    console.error('An error occurred:', error);

    if (error.status === 0) {
      // A client-side or network error occurred.
      alert('An unexpected error occurred. Please try again later.');
    } else if (error.status === 401) {
      // Unauthorized
      alert('Invalid credentials. Please try again.');
    } else if (error.status === 404) {
      // Not Found
      alert('The requested resource was not found.');
    } else {
      // Backend returned an unsuccessful response code.
      alert(`Backend returned code ${error.status}. Please try again.`);
    }

    // Optionally navigate to a global error page
    // this.router.navigate(['/error']);
  }
}
