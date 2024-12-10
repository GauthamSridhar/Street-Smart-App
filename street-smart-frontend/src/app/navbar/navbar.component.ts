import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  imports: [CommonModule, RouterModule]
})
export class NavbarComponent implements OnInit, OnDestroy {
  isMobileMenuOpen: boolean = false;
  menuItems: { name: string; count?: number }[] = []; 
  favouritesCount: number = 5;
  requestsCount: number = 12;
  reviewsCount: number = 8;
  productsCount: number = 15;

  constructor(private router: Router) {}

  ngOnInit() {
    this.updateMenuItemsBasedOnSession();
  }

  ngOnDestroy() {
    // No subscriptions to unsubscribe from now.
  }

  private updateMenuItemsBasedOnSession() {
    const token = sessionStorage.getItem('tokenId');
    const role = sessionStorage.getItem('role'); // 'USER', 'SHOPKEEPER', or 'ADMIN'

    console.log('Checking session data for navbar:', { token, role });

    if (!token || !role) {
      // No user logged in, show only login
      this.menuItems = [
        { name: 'Login' }
      ];
    } else {
      // User is logged in, show items based on role
      switch (role) {
        case 'USER':
          this.menuItems = [
            { name: 'favorites', count: this.favouritesCount },
            { name: 'profile' },
            { name: 'logout' }
          ];
          break;
        case 'SHOPKEEPER':
          this.menuItems = [
            { name: 'reviews', count: this.reviewsCount },
            { name: 'profile' },
            { name: 'products', count: this.productsCount },
            { name: 'logout' }
          ];
          break;
        case 'ADMIN':
          this.menuItems = [
            { name: 'requests', count: this.requestsCount },
            { name: 'profile' },
            { name: 'logout' }
          ];
          break;
        default:
          this.menuItems = [
            { name: 'Login' }
          ];
          break;
      }
    }

    console.log('Updated menu items based on session:', this.menuItems);
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  logout() {
    console.log('Logout clicked');
    // Clear session storage and navigate to login
    sessionStorage.clear();
    this.router.navigate(['/login']);
  }
}
