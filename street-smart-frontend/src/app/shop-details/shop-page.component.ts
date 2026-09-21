import { Component, OnInit, OnDestroy } from '@angular/core';

import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, switchMap, catchError, of } from 'rxjs';
import { ShopService } from '../services/shop.service';
import { SessionService } from '../services/session.service';
import { Shop } from '../model/shop.model';
import { ShopDetailsComponent } from './shop-details.component';
import { NavbarComponent } from '../navbar/navbar.component';
import { apiError } from '../services/api-error';
@Component({
  selector: 'app-shop-page',
  standalone: true,
  imports: [ShopDetailsComponent, NavbarComponent],
  template: `<app-navbar></app-navbar>
    <main class="ss-page ss-shell">
      <button (click)="back()" class="ss-btn-ghost mb-4">Back to dashboard</button>
      @if (loading) {
        <p role="status" class="ss-alert-info">Loading shop...</p>
      }
      @if (error) {
        <p role="alert" class="ss-alert-error">{{ error }}</p>
      }
      @if (shop) {
        <app-shop-details
          [shop]="shop"
          [isOpen]="true"
          (close)="back()"
          (navigateToShop)="directions($event)"
        ></app-shop-details>
      }
    </main>`,
})
export class ShopPageComponent implements OnInit, OnDestroy {
  shop: Shop | null = null;
  error = '';
  loading = true;
  private subscription?: Subscription;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private shops: ShopService,
    private session: SessionService,
  ) {}
  ngOnInit(): void {
    this.subscription = this.route.paramMap
      .pipe(
        switchMap((params) => {
          this.loading = true;
          this.error = '';
          this.shop = null;
          return this.shops.getShopById(params.get('id') || '').pipe(
            catchError((error) => {
              this.error = apiError(error);
              return of(null);
            }),
          );
        }),
      )
      .subscribe((shop) => {
        this.shop = shop;
        this.loading = false;
      });
  }
  back(): void {
    void this.router.navigateByUrl(this.session.home);
  }
  directions(shop: Shop): void {
    window.open(
      'https://www.google.com/maps/dir/?api=1&destination=' +
        encodeURIComponent(shop.latitude + ',' + shop.longitude),
      '_blank',
      'noopener,noreferrer',
    );
  }
  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
