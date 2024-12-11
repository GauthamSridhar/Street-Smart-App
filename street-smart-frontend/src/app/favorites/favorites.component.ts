// src/app/favorites/favorites.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FavoritesService, Shop } from '../services/favorite-service.service';
import { Subscription } from 'rxjs';
import { NavbarComponent } from "../navbar/navbar.component";

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './favorites.component.html',
  styleUrls: ['./favorites.component.css']
})
export class FavoritesComponent implements OnInit, OnDestroy {
  favoriteShops: Shop[] = [];
  private subscription!: Subscription;

  constructor(private router: Router, private favoritesService: FavoritesService) {}

  ngOnInit(): void {
    this.fetchFavoriteShops();
  }

  /**
   * Fetches favorite shops from the FavoritesService.
   */
  fetchFavoriteShops(): void {
    const userId = sessionStorage.getItem('id'); // Replace with actual user ID
    if (userId) {
      this.subscription = this.favoritesService.getFavoriteShops(userId).subscribe(
      (shops: Shop[]) => {
        this.favoriteShops = shops;
      },
      (error) => {
        console.error('Error fetching favorite shops:', error);
      }
      );
    } else {
      console.error('User ID is null');
    }

  }

  /**
   * Navigates to the selected shop's detail page.
   * @param shopId The ID of the shop to navigate to.
   */
  navigateToShop(shopId: string): void {
    this.router.navigate(['/shops', shopId]);
  }

  /**
   * Removes a shop from favorites.
   * @param shopId The ID of the shop to remove.
   */
  removeFromFavorites(shopId: string): void {
    const userId = sessionStorage.getItem('id'); // Replace with actual user ID
    if (userId) {
      this.favoritesService.removeFavoriteShop(shopId, userId).subscribe(
        (response) => {
          console.log(response);
          // Refresh the favorite shops list
          this.fetchFavoriteShops();
        },
        (error) => {
          console.error('Error removing favorite shop:', error);
        }
      );
  }
}
  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}