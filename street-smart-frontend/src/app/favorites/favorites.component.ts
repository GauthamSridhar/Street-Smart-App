// src/app/favorites/favorites.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FavoritesService, Shop } from '../services/favorite-service.service';
import { Subscription } from 'rxjs';
import { NavbarComponent } from "../navbar/navbar.component";

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule, NavbarComponent, RouterModule],
  templateUrl: './favorites.component.html',
  styleUrls: ['./favorites.component.css']
})
export class FavoritesComponent implements OnInit, OnDestroy {
  favoriteShops: Shop[] = [];
  formError: string = '';
  registrationSuccess: boolean = false;
  isLoading: boolean = false;
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
          console.log('Favorite shops:', this.favoriteShops);
        },
        (error) => {
          console.error('Error fetching favorite shops:', error);
          this.formError = 'Failed to load favorite shops. Please try again later.';
        }
      );
    } else {
      console.error('User ID is null');
      this.formError = 'User not authenticated. Please log in.';
    }
  }

  /**
   * Confirms removal of a shop from favorites.
   * @param shopId The ID of the shop to remove.
   */
  confirmRemove(shopId: string): void {
    const confirmation = window.confirm('Are you sure you want to remove this shop from your favorites?');
    if (confirmation) {
      this.removeFromFavorites(shopId);
    }
  }

  /**
   * Removes a shop from favorites.
   * @param shopId The ID of the shop to remove.
   */
  removeFromFavorites(shopId: string): void {
    const userId = sessionStorage.getItem('id'); // Replace with actual user ID

    if (!shopId) {
      console.error('Invalid shop ID.');
      this.formError = 'Invalid shop ID.';
      return;
    }

    if (!userId) {
      console.error('User ID is null.');
      this.formError = 'User not authenticated. Please log in.';
      return;
    }

    if (!this.isValidUUID(userId) || !this.isValidUUID(shopId)) {
      console.error('Invalid UUID format for User ID or Shop ID.');
      this.formError = 'Invalid identifier format.';
      return;
    }

    console.log('Removing shop from favorites:', shopId);
    console.log('User ID:', userId);
    
    this.isLoading = true; // Start loading indicator

    this.favoritesService.removeFavoriteShop(shopId, userId).subscribe(
      () => {
        console.log('Successfully removed favorite shop:', shopId);
        this.isLoading = false; // End loading indicator
        this.registrationSuccess = true; // Show success message
        this.fetchFavoriteShops(); // Refresh the favorite shops list

        // Auto-hide the success message after 3 seconds
        setTimeout(() => {
          this.clearMessages();
        }, 2000);
      },
      (error) => {
        console.error('Error removing favorite shop:', error);
        if (error.status === 404) {
          this.formError = 'Shop not found.';
        } else if (error.status === 401) {
          this.formError = 'Unauthorized. Please log in again.';
        } else {
          this.formError = 'Failed to remove the favorite shop. Please try again later.';
        }
        this.isLoading = false; // End loading indicator
      }
    );
  }

  /**
   * Clears success and error messages.
   */
  clearMessages(): void {
    this.formError = '';
    this.registrationSuccess = false;
  }

  /**
   * Validates whether a string is a valid UUID.
   * @param uuid The string to validate.
   * @returns True if valid, false otherwise.
   */
  isValidUUID(uuid: string): boolean {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(uuid);
  }

  /**
   * trackBy function to optimize ngFor rendering.
   * @param index The index of the item.
   * @param shop The shop item.
   * @returns The unique identifier for the shop.
   */
  trackByShopId(index: number, shop: Shop): string {
    return shop.shopId;
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
