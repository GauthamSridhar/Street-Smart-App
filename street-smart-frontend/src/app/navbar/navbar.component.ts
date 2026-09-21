import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription, forkJoin, switchMap } from 'rxjs';
import { FavoritesService } from '../services/favorite-service.service';
import { RequestsService } from '../services/requests.service';
import { ReviewService } from '../services/review.service';
import { ProductsService } from '../services/products.service';
import { ShopService } from '../services/shop.service';
import { SessionService } from '../services/session.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environment';
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent implements OnInit, OnDestroy {
  menuItems: { name: string; count?: number }[] = [];
  private subscriptions = new Subscription();
  constructor(
    private router: Router,
    private session: SessionService,
    private shops: ShopService,
    private favorites: FavoritesService,
    private requests: RequestsService,
    private reviews: ReviewService,
    private products: ProductsService,
    private http: HttpClient,
  ) {}
  ngOnInit(): void {
    const role = this.session.role;
    if (!role) {
      this.menuItems = [{ name: 'Login' }];
      return;
    }
    this.menuItems = [
      ...(role === 'USER'
        ? [{ name: 'favorites' }]
        : role === 'ADMIN'
          ? [{ name: 'requests' }, { name: 'moderation' }]
          : [{ name: 'products' }, { name: 'reviews' }]),
      { name: 'profile' },
      { name: 'logout' },
    ];
    if (role === 'USER')
      this.subscriptions.add(
        this.favorites
          .getFavoritesCount(this.session.id)
          .subscribe({ next: (count) => this.count('favorites', count), error: () => {} }),
      );
    if (role === 'ADMIN')
      this.subscriptions.add(
        this.requests
          .getRequestsCount()
          .subscribe({ next: (count) => this.count('requests', count), error: () => {} }),
      );
    if (role === 'SHOPKEEPER')
      this.subscriptions.add(
        this.shops
          .getShopByShopkeeperId(this.session.id)
          .pipe(
            switchMap((shop) =>
              forkJoin({
                products: this.products.getProductsCount(shop.id),
                reviews: this.reviews.getReviewsCount(shop.id),
              }),
            ),
          )
          .subscribe({
            next: (counts) => {
              this.count('products', counts.products);
              this.count('reviews', counts.reviews);
            },
            error: () => {},
          }),
      );
  }
  private count(name: string, count: number): void {
    const item = this.menuItems.find((i) => i.name === name);
    if (item) item.count = count;
  }
  navigateToDashboard(): void {
    void this.router.navigateByUrl(this.session.token ? this.session.home : '/');
  }
  logout(): void {
    this.subscriptions.add(this.http.post(environment.apiBaseUrl + '/users/logout', null).subscribe({
      next: () => { this.session.clear(); void this.router.navigate(['/login']); },
      error: () => { window.alert('Could not confirm server logout. Please retry when the service is available.'); },
    }));
  }
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
