import { Component, OnInit, OnDestroy } from '@angular/core';

import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { FavoritesService, Shop } from '../services/favorite-service.service';
import { SessionService } from '../services/session.service';
import { NavbarComponent } from '../navbar/navbar.component';
import { apiError } from '../services/api-error';
@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [NavbarComponent, RouterModule],
  templateUrl: './favorites.component.html',
  styleUrls: ['./favorites.component.css'],
})
export class FavoritesComponent implements OnInit, OnDestroy {
  favoriteShops: Shop[] = [];
  formError = '';
  registrationSuccess = false;
  isLoading = false;
  page = 0;
  private subscriptions = new Subscription();
  constructor(
    private favorites: FavoritesService,
    private session: SessionService,
  ) {}
  ngOnInit(): void {
    this.fetchFavoriteShops();
  }
  fetchFavoriteShops(): void {
    this.isLoading = true;
    this.formError = '';
    this.subscriptions.add(
      this.favorites.getFavoriteShops(this.session.id, this.page).subscribe({
        next: (rows) => {
          this.favoriteShops = rows;
          this.isLoading = false;
          if (!rows.length && this.page > 0) {
            this.page--;
            this.fetchFavoriteShops();
          }
        },
        error: (error) => {
          this.formError = apiError(error);
          this.isLoading = false;
        },
      }),
    );
  }
  move(delta: number): void {
    if (!this.isLoading) {
      this.page += delta;
      this.fetchFavoriteShops();
    }
  }
  removeFromFavorites(shopId: string): void {
    if (this.isLoading) return;
    this.isLoading = true;
    this.subscriptions.add(
      this.favorites.removeFavoriteShop(shopId, this.session.id).subscribe({
        next: () => {
          this.registrationSuccess = true;
          this.fetchFavoriteShops();
        },
        error: (error) => {
          this.formError = apiError(error);
          this.isLoading = false;
        },
      }),
    );
  }
  trackByShopId(_index: number, shop: Shop): string {
    return shop.shopId;
  }
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
