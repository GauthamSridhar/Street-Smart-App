import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const token = sessionStorage.getItem('tokenId');
    const userRole = sessionStorage.getItem('role');

    // Check if user is logged in
    if (!token || !userRole) {
      console.warn('No token or role found. User is not authenticated.');
      this.router.navigate(['/login']);
      return false;
    }

    // If this route requires roles, check them
    const requiredRoles = route.data['roles'] as string[] | undefined;
    if (requiredRoles && !requiredRoles.includes(userRole)) {
      console.error(`User role '${userRole}' does not meet required roles: ${requiredRoles.join(', ')}`);
      this.router.navigate(['/error'], { queryParams: { message: 'Access Denied' } });
      return false;
    }

    // User is authenticated and authorized
    return true;
  }
}
