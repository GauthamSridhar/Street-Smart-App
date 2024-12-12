import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FavoritesService } from '../services/favorite-service.service';
import { RequestsService } from '../services/requests.service';
import { ReviewService } from '../services/review.service';
import { ProductsService } from '../services/products.service';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  imports: [CommonModule,RouterModule]
})
export class NavbarComponent implements OnInit, OnDestroy {
// navbar.component.ts

// Add this method inside your NavbarComponent class
navigateToDashboard() {
  const token = sessionStorage.getItem('tokenId');
  const role = sessionStorage.getItem('role'); // 'USER', 'SHOPKEEPER', or 'ADMIN'

  if (!token || !role) {
    // If not logged in, navigate to home (root)
    this.router.navigate(['/']);
  } else {
    // Navigate based on role
    switch (role) {
      case 'USER':
        this.router.navigate(['/dashboard']);  // Adjust the route to your user dashboard
        break;
      case 'SHOPKEEPER':
        this.router.navigate(['/shop-dashboard']);  // Adjust the route to your shopkeeper dashboard
        break;
      case 'ADMIN':
        this.router.navigate(['/admin-dashboard']);  // Adjust the route to your admin dashboard
        break;
      default:
        this.router.navigate(['/']);
        break;
    }
  }
}

  isMobileMenuOpen: boolean = false;

  // Variables for counts
  favoritesCount: number = 0;
  reviewsCount: number = 0;
  requestsCount: number = 0;
  productsCount: number = 0;

  // Menu items for display
  menuItems: { name: string; count?: number }[] = [];

  private subscriptions: Subscription[] = [];

  constructor(
    private router: Router,
    private favoritesService: FavoritesService,
    private requestsService: RequestsService,
    private reviewsService: ReviewService,
    private productsService: ProductsService
  ) {}

  ngOnInit() {
    this.updateMenuItemsBasedOnSession();
  }

  ngOnDestroy() {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
  }

  private updateMenuItemsBasedOnSession() {
    const token = sessionStorage.getItem('tokenId');
    const role = sessionStorage.getItem('role'); // 'USER', 'SHOPKEEPER', or 'ADMIN'
    const userId = sessionStorage.getItem('id');

    if (!token || !role || !userId) {
      this.menuItems = [{ name: 'Login' }];
      return;
    }

    switch (role) {
      case 'USER':
        this.menuItems = [
          { name: 'favorites', count: this.favoritesCount },
          { name: 'profile' },
          { name: 'logout' },
        ];
        this.fetchFavoritesCount(userId);
        break;

      case 'SHOPKEEPER':
        this.menuItems = [
          { name: 'reviews', count: this.reviewsCount },
          { name: 'products', count: this.productsCount },
          { name: 'profile' },
          { name: 'logout' },
        ];
        this.fetchReviewsCount(userId);
        this.fetchProductsCount(userId);
        break;

      case 'ADMIN':
        this.menuItems = [
          { name: 'requests', count: this.requestsCount },
          { name: 'profile' },
          { name: 'logout' },
        ];
        this.fetchRequestsCount(userId);
        break;

      default:
        this.menuItems = [{ name: 'Login' }];
        break;
    }
  }

  private fetchFavoritesCount(userId: string): void {
    const sub = this.favoritesService.getFavoritesCount(userId).subscribe({
      next: (count) => {
        console.log(`Favorites count fetched for userId ${userId}:`, count);
        this.favoritesCount = count;
        this.updateMenuCounts('favorites', count);
      },
      error: (err) => console.error('Error fetching favorites count:', err),
    });
    this.subscriptions.push(sub);
  }

  private fetchReviewsCount(userId: string): void {
    const sub = this.reviewsService.getReviewsCount(userId).subscribe({
      next: (count) => {
        console.log(`Reviews count fetched for userId ${userId}:`, count);
        this.reviewsCount = count;
        this.updateMenuCounts('reviews', count);
      },
      error: (err) => console.error('Error fetching reviews count:', err),
    });
    this.subscriptions.push(sub);
  }

  private fetchRequestsCount(userId: string): void {
    const sub = this.requestsService.getRequestsCount(userId).subscribe({
      next: (count) => {
        console.log(`Requests count fetched for userId ${userId}:`, count);
        this.requestsCount = count;
        this.updateMenuCounts('requests', count);
      },
      error: (err) => console.error('Error fetching requests count:', err),
    });
    this.subscriptions.push(sub);
  }

  private fetchProductsCount(userId: string): void {
    const sub = this.productsService.getProductsCount(userId).subscribe({
      next: (count) => {
        console.log(`Products count fetched for userId ${userId}:`, count);
        this.productsCount = count;
        this.updateMenuCounts('products', count);
      },
      error: (err) => console.error('Error fetching products count:', err),
    });
    this.subscriptions.push(sub);
  }

  private updateMenuCounts(name: string, count: number): void {
    const item = this.menuItems.find((item) => item.name === name);
    if (item) {
      item.count = count;
    }
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  logout() {
    sessionStorage.clear();
    this.router.navigate(['/login']);
  }
}
