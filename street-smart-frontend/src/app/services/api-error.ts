import { HttpErrorResponse } from '@angular/common/http';
export function apiError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'Cannot reach the service. Check your connection and try again.';
    if (error.status === 401)
      return 'Your session has expired or your credentials are incorrect. Please sign in.';
    if (error.status === 403) return 'You do not have permission to perform this action.';
    if (error.status === 429) return 'Too many requests. Please wait before trying again.';
    if (error.status >= 500) return 'The service is temporarily unavailable. Please try again.';
    if (typeof error.error?.detail === 'string') return error.error.detail;
    if (error.status === 404) return 'The requested record was not found.';
    if (error.status === 409)
      return 'This conflicts with an existing record. Refresh and try again.';
    return 'Please check the supplied values and try again.';
  }
  return 'Something went wrong. Please try again.';
}
