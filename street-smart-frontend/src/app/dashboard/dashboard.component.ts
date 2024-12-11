// src/app/dashboard/dashboard.component.ts

import { Component, OnInit, OnDestroy, ViewChild, inject } from '@angular/core';
import { Shop } from '../model/shop.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShopDetailsComponent } from '../shop-details/shop-details.component';
import { NavbarComponent } from '../navbar/navbar.component';
import { MapComponent } from '../map/map.component';
import { GeolocationService } from '../geolocation-service.service';
import { Subscription } from 'rxjs';
import { ShopService } from '../services/shop.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, ShopDetailsComponent, NavbarComponent, MapComponent],
})
export class DashboardComponent implements OnInit, OnDestroy {
  private geolocationService = inject(GeolocationService);
  private shopService = inject(ShopService);

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

  ngOnInit(): void {
    this.loadShops();
    this.locateUser();
  }

  ngOnDestroy(): void {
    if (this.geolocationSubscription) {
      this.geolocationSubscription.unsubscribe();
    }
  }

  loadShops(): void {
    this.shopService.getShops().subscribe({
      next: (shops) => {
        this.shops = shops.map((shop) => ({
          ...shop,
          images: [{ url: `https://picsum.photos/200/300?random=${Math.floor(Math.random() * 1000)}` }]
        }));
        this.filteredShops = [...this.shops];
      },
      error: (error) => {
        console.error('Error fetching shops:', error);
      }
    });
  }

  locateUser(): void {
    this.geolocationSubscription = this.geolocationService.watchLocation().subscribe({
      next: (location) => {
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
    this.selectedShop = shop;
    this.isShopSelected = true;
  }

  closeShopDetails(): void {
    this.isShopSelected = false;
    this.selectedShop = null;
    this.resetNavigationState();
  }

  filterShops(query: string): void {
    this.filteredShops = this.shops.filter((shop) =>
      shop.name.toLowerCase().includes(query.toLowerCase()) ||
      shop.description.toLowerCase().includes(query.toLowerCase())
    );
  }

  filterByCategory(category: string): void {
    this.filteredShops = category
      ? this.shops.filter((shop) => shop.category === category)
      : [...this.shops];
  }

  onNavigateToShop(shop: Shop): void {
    if (this.mapComponent) {
      this.isNavigating = true;
      this.loadingNavigation = true;
      this.mapComponent.navigateToLocation(shop);
    }
  }

  onCancelNavigation(): void {
    if (this.isNavigating && this.mapComponent) {
      this.mapComponent.cancelNavigation();
      this.resetNavigationState();
    }
  }

  cancelNavigation(): void {
    if (this.isNavigating && this.mapComponent) {
      this.mapComponent.cancelNavigation();
      this.resetNavigationState();
    }
  }

  resetNavigationState(): void {
    this.isNavigating = false;
    this.loadingNavigation = false;
  }
}
