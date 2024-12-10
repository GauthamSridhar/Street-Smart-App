// src/app/dashboard/dashboard.component.ts

import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Shop } from '../model/shop.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShopDetailsComponent } from '../shop-details/shop-details.component';
import { NavbarComponent } from '../navbar/navbar.component';
import { MapComponent } from '../map/map.component';
import { GeolocationService } from '../geolocation-service.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, ShopDetailsComponent, NavbarComponent, MapComponent],
})
export class DashboardComponent implements OnInit, OnDestroy {
  shops: Shop[] = [];
  filteredShops: Shop[] = [];
  selectedShop: Shop | null = null;
  isShopSelected = false;
  currentLocation: { lat: number; lng: number } | null = null;
  geolocationError: string | null = null;

  private geolocationSubscription: Subscription | null = null;

  @ViewChild(MapComponent) mapComponent!: MapComponent;

  isNavigating: boolean = false;
  loadingNavigation: boolean = false;

  constructor(private geolocationService: GeolocationService) {
    console.log('DashboardComponent constructed');
  }

  ngOnInit(): void {
    console.log('DashboardComponent ngOnInit');
    this.loadShops();
    this.locateUser();
  }

  ngOnDestroy(): void {
    console.log('DashboardComponent ngOnDestroy');
    if (this.geolocationSubscription) {
      this.geolocationSubscription.unsubscribe();
      console.log('Unsubscribed from geolocation');
    }
  }

  loadShops(): void {
    console.log('Loading mock shops');
    this.shops = [
      {
        id: '2',
        name: 'Book Haven',
        description: 'A paradise for book enthusiasts.',
        address: '456 Chapter Rd, Literary Town',
        latitude: 8.5362836,
        longitude: 76.8830096,
        ratings: [4, 5, 4, 4, 5],
        images: [{ url: 'https://picsum.photos/200/300?random=2' }],
        category: 'Books',
      },
      {
        id: '3',
        name: 'Tech World',
        description: 'Your one-stop shop for gadgets and tech.',
        address: '789 Innovation Blvd, Silicon Valley',
        latitude: 8.5260836,
        longitude: 76.8728096,
        ratings: [5, 5, 4, 5, 4],
        images: [{ url: 'https://picsum.photos/200/300?random=3' }],
        category: 'Electronics',
      },
      {
        id: '4',
        name: 'Green Grocers',
        description: 'Fresh produce and organic goods.',
        address: '321 Farm Lane, Agro Valley',
        latitude: 8.5061836,
        longitude: 76.8727096,
        ratings: [5, 4, 5, 4, 4],
        images: [{ url: 'https://picsum.photos/200/300?random=4' }],
        category: 'Grocery',
      },
      {
        id: '5',
        name: 'Fitness Hub',
        description: 'Get fit and stay healthy.',
        address: '654 Muscle Dr, Fit City',
        latitude: 8.5460836,
        longitude: 76.8826096,
        ratings: [4, 4, 4, 5],
        images: [{ url: 'https://picsum.photos/200/300?random=5' }],
        category: 'Fitness',
      },
      {
        id: '6',
        name: 'Artisan Cafe',
        description: 'Handcrafted coffee and treats.',
        address: '987 Aroma St, Bean Town',
        latitude: 8.5362836,
        longitude: 76.8931096,
        ratings: [5, 5, 4, 5],
        images: [{ url: 'https://picsum.photos/200/300?random=6' }],
        category: 'Food',
      }
    ];
    this.filteredShops = [...this.shops];
    console.log('Shops loaded', this.shops);
  }

  locateUser(): void {
    console.log('Requesting user location');
    this.geolocationSubscription = this.geolocationService.watchLocation().subscribe({
      next: (location) => {
        console.log('Location obtained', location);
        this.currentLocation = location;
        if (this.mapComponent) {
          this.mapComponent.updateCurrentLocation(this.currentLocation);
        }
        this.geolocationError = null;
      },
      error: (error) => {
        console.error('Error getting location:', error);
        this.geolocationError = 'Unable to access your location. Please enable location services.';
      }
    });
  }

  onShopSelected(shop: Shop): void {
    console.log('Shop selected', shop);
    this.selectedShop = shop;
    this.isShopSelected = true;
  }

  closeShopDetails(): void {
    console.log('Closing shop details');
    this.isShopSelected = false;
    this.selectedShop = null;
    this.resetNavigationState();
  }

  filterShops(query: string): void {
    console.log('Filtering shops by query:', query);
    this.filteredShops = this.shops.filter((shop) =>
      shop.name.toLowerCase().includes(query.toLowerCase()) ||
      shop.description.toLowerCase().includes(query.toLowerCase())
    );
  }

  filterByCategory(category: string): void {
    console.log('Filtering shops by category:', category);
    this.filteredShops = category
      ? this.shops.filter((shop) => shop.category === category)
      : [...this.shops];
  }

  onNavigateToShop(shop: Shop): void {
    console.log('Navigating to shop:', shop);
    if (this.mapComponent) {
      this.isNavigating = true;
      this.loadingNavigation = true;
      this.mapComponent.navigateToLocation(shop);
    }
  }

  onCancelNavigation(): void {
    console.log('Cancelling navigation from ShopDetailsComponent');
    if (this.isNavigating && this.mapComponent) {
      this.mapComponent.cancelNavigation();
      this.resetNavigationState();
    }
  }

  cancelNavigation(): void {
    console.log('Cancelling navigation from Dashboard');
    if (this.isNavigating && this.mapComponent) {
      this.mapComponent.cancelNavigation();
      this.resetNavigationState();
    }
  }

  resetNavigationState(): void {
    console.log('Resetting navigation state');
    this.isNavigating = false;
    this.loadingNavigation = false;
  }
}
