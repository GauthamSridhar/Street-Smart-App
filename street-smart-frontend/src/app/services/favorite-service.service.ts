// src/app/services/favorites.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

// Define the Shop interface
export interface Shop {
  id: string;
  name: string;
  description: string;
  type: string;
}

@Injectable({
  providedIn: 'root'
})
export class FavoritesService {
  // Base URL for the Favorites API
  private readonly apiUrl = 'https://your-backend-api.com/api/favorites'; // Replace with your actual API endpoint

  // Local state to manage favorite shops (for dummy data)
  private favoriteShops: Shop[] = [
    {
      id: 'shop1',
      name: 'Black & White Bistro',
      description: 'A fine dining restaurant offering exquisite black and white themed dishes.',
      type: 'Restaurant'
    },
    {
      id: 'shop2',
      name: 'Shadow Apparel',
      description: 'Trendy clothing store specializing in monochrome fashion.',
      type: 'Clothing Store'
    },
    {
      id: 'shop3',
      name: 'Nightfall Books',
      description: 'Cozy bookstore with a vast collection of literature in dark-themed decor.',
      type: 'Bookstore'
    },
    {
      id: 'shop1',
      name: 'Black & White Bistro',
      description: 'A fine dining restaurant offering exquisite black and white themed dishes.',
      type: 'Restaurant'
    },
    {
      id: 'shop2',
      name: 'Shadow Apparel',
      description: 'Trendy clothing store specializing in monochrome fashion.',
      type: 'Clothing Store'
    },
    {
      id: 'shop3',
      name: 'Nightfall Books',
      description: 'Cozy bookstore with a vast collection of literature in dark-themed decor.',
      type: 'Bookstore'
    }
  ];

  constructor(private http: HttpClient) {}

  /**
   * Fetches the list of favorite shops for the current user.
   * 
   * @returns Observable<Shop[]>
   */
  getFavoriteShops(): Observable<Shop[]> {
    // TODO: Integrate with backend by uncommenting the HTTP GET request below
    // return this.http.get<Shop[]>(`${this.apiUrl}/user-favorites`);

    // Return dummy data for now
    return of(this.favoriteShops);
  }

  /**
   * Checks if a specific shop is in the user's favorites.
   * 
   * @param shopId - The ID of the shop to check.
   * @returns Observable<boolean>
   */
  isFavorite(shopId: string): Observable<boolean> {
    // TODO: Integrate with backend by uncommenting the HTTP GET request below
    // return this.http.get<boolean>(`${this.apiUrl}/isFavorite/${shopId}`);

    // Check in dummy data
    const isFav = this.favoriteShops.some(shop => shop.id === shopId);
    return of(isFav);
  }

  /**
   * Adds a shop to the user's favorites.
   * 
   * @param shopId - The ID of the shop to add.
   * @returns Observable<any>
   */
  addFavorite(shopId: string): Observable<any> {
    // TODO: Integrate with backend by uncommenting the HTTP POST request below
    // return this.http.post(`${this.apiUrl}/add`, { shopId });

    // Add to dummy data
    const newShop: Shop = {
      id: shopId,
      name: `New Shop ${shopId}`, // Replace with actual shop details if available
      description: `Description for Shop ${shopId}.`,
      type: 'New Type' // Replace with actual type if available
    };

    this.favoriteShops.push(newShop);

    return of({ success: true, message: 'Shop added to favorites.', shop: newShop });
  }

  /**
   * Removes a shop from the user's favorites.
   * 
   * @param shopId - The ID of the shop to remove.
   * @returns Observable<any>
   */
  removeFavoriteShop(shopId: string): Observable<any> {
    // TODO: Integrate with backend by uncommenting the HTTP DELETE request below
    // return this.http.delete(`${this.apiUrl}/remove/${shopId}`);

    // Remove from dummy data
    const index = this.favoriteShops.findIndex(shop => shop.id === shopId);
    if (index !== -1) {
      this.favoriteShops.splice(index, 1);
      return of({ success: true, message: 'Shop removed from favorites.' });
    } else {
      return of({ success: false, message: 'Shop not found in favorites.' });
    }
  }
}
