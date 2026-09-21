import { Component, OnInit, OnDestroy } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { ShopService } from '../services/shop.service';
import { SessionService } from '../services/session.service';
import { Shop, ShopEdit } from '../model/shop.model';
import { NavbarComponent } from '../navbar/navbar.component';
import { apiError } from '../services/api-error';
import { HttpErrorResponse } from '@angular/common/http';
import { ImageManagerComponent } from './image-manager.component';
@Component({
  selector: 'app-shop-dashboard',
  imports: [NavbarComponent, FormsModule, RouterModule, ImageManagerComponent],
  templateUrl: './shop-dashboard.component.html',
  styleUrls: ['./shop-dashboard.component.css'],
})
export class ShopDashboardComponent implements OnInit, OnDestroy {
  shopDetails: Shop | null = null;
  edit: ShopEdit | null = null;
  errorMessage = '';
  isLoading = true;
  busy = false;
  missing = false;
  private subscriptions = new Subscription();
  constructor(
    private shops: ShopService,
    private session: SessionService,
  ) {}
  ngOnInit(): void {
    this.load();
  }
  load(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.missing = false;
    this.subscriptions.add(
      this.shops.getShopByShopkeeperId(this.session.id).subscribe({
        next: (shop) => {
          this.shopDetails = shop;
          this.edit = {
            name: shop.name,
            description: shop.description,
            address: shop.address,
            category: shop.category,
            openingHours: shop.openingHours,
            latitude: shop.latitude,
            longitude: shop.longitude,
          };
          this.isLoading = false;
        },
        error: (error) => {
          this.isLoading = false;
          this.missing = error instanceof HttpErrorResponse && error.status === 404;
          if (!this.missing) this.errorMessage = apiError(error);
        },
      }),
    );
  }
  save(): void {
    if (!this.shopDetails || !this.edit || this.busy) return;
    this.busy = true;
    this.errorMessage = '';
    this.subscriptions.add(
      this.shops.updateShop(this.shopDetails.id, this.edit).subscribe({
        next: (shop) => {
          this.shopDetails = shop;
          this.busy = false;
        },
        error: (error) => {
          this.errorMessage = apiError(error);
          this.busy = false;
        },
      }),
    );
  }
  toggle(): void {
    if (!this.shopDetails || this.busy) return;
    this.busy = true;
    this.subscriptions.add(
      this.shops.toggleShopStatus(this.shopDetails.id).subscribe({
        next: (shop) => {
          this.shopDetails = shop;
          this.busy = false;
        },
        error: (error) => {
          this.errorMessage = apiError(error);
          this.busy = false;
        },
      }),
    );
  }
  resubmit(): void {
    if (!this.shopDetails || this.busy) return;
    this.busy = true;
    this.errorMessage = '';
    this.subscriptions.add(this.shops.resubmit(this.shopDetails.id).subscribe({
      next: shop => { this.shopDetails = shop; this.busy = false; },
      error: error => { this.errorMessage = apiError(error); this.busy = false; },
    }));
  }
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
