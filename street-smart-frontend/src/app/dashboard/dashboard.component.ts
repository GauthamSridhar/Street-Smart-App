import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, Subscription, switchMap, catchError, of } from 'rxjs';
import { Shop } from '../model/shop.model';
import { ProductMatch } from '../model/product-search';
import { ShopDetailsComponent } from '../shop-details/shop-details.component';
import { NavbarComponent } from '../navbar/navbar.component';
import { MapComponent } from '../map/map.component';
import { GeolocationService } from '../geolocation-service.service';
import { ShopService } from '../services/shop.service';
import { ProductSearchState, SearchState } from '../services/product-search-state.service';
import { apiError } from '../services/api-error';
import { ProductsService } from '../services/products.service';
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ShopDetailsComponent, NavbarComponent, MapComponent],
  providers: [ProductSearchState],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardComponent implements OnInit, OnDestroy {
  @ViewChild(MapComponent) mapComponent?: MapComponent;
  state: SearchState = { loading: true, error: '', page: null };
  searchQuery = '';
  category = '';
  availableOnly = true;
  fuzzy = false;
  minPrice: number | null = null;
  maxPrice: number | null = null;
  radiusKm: number | null = null;
  categories = ['clothing', 'electronics', 'grocery', 'books', 'pharmacy', 'restaurant'];
  selectedShop: Shop | null = null;
  filteredShops: Shop[] = [];
  selectionError = '';
  selectionLoading = false;
  showMap = false;
  currentLocation: { lat: number; lng: number } | null = null;
  geolocationError = '';
  private subscriptions = new Subscription();
  private locationSubscription?: Subscription;
  private selection = new Subject<string | null>();
  constructor(
    public search: ProductSearchState,
    private shops: ShopService,
    private location: GeolocationService,
    private route: ActivatedRoute,
    private router: Router,
    private products: ProductsService,
  ) {}
  ngOnInit(): void {
    this.subscriptions.add(this.products.categories().subscribe({
      next: categories => this.categories = [...new Set([...categories, this.category.toLowerCase()].filter(Boolean))].sort(),
      error: () => {},
    }));
    this.subscriptions.add(
      this.search.state$.subscribe((state) => {
        this.state = state;
        const unique = new Map<string, Shop>();
        for (const result of state.page?.content || []) {
          const existing = unique.get(result.shopId);
          if (existing) {
            existing.mapProductNames = [...new Set([...(existing.mapProductNames || []), result.name])];
          } else {
            unique.set(result.shopId, {
              id: result.shopId,
              name: result.shopName,
              category: result.category,
              address: result.address,
              latitude: result.latitude,
              longitude: result.longitude,
              status: result.shopStatus,
              description: '',
              ownerId: '',
              products: [],
              images: [],
              mapProductNames: [result.name],
            });
          }
        }
        this.filteredShops = [...unique.values()];
      }),
    );
    this.subscriptions.add(
      this.route.queryParamMap.subscribe((params) => {
        this.searchQuery = (params.get('q') || '').slice(0, 100);
        this.category = (params.get('category') || '').slice(0, 60).trim().toLowerCase();
        this.availableOnly = params.get('availableOnly') !== 'false';
        this.fuzzy = params.get('fuzzy') === 'true';
        const numeric = (key: string): number | undefined => {
          const raw = params.get(key);
          return raw !== null && raw !== '' && Number.isFinite(Number(raw)) ? Number(raw) : undefined;
        };
        this.minPrice = numeric('minPrice') ?? null;
        this.maxPrice = numeric('maxPrice') ?? null;
        this.radiusKm = numeric('radiusKm') ?? null;
        const rawPage = Number(params.get('page') || 0);
        const page = Number.isInteger(rawPage) && rawPage >= 0 && rawPage <= 10000 ? rawPage : 0;
        this.search.setQuery({
          q: this.searchQuery,
          category: this.category,
          availableOnly: this.availableOnly,
          page,
          size: 20,
          ...(this.fuzzy ? { fuzzy: true } : {}),
          ...(this.minPrice != null ? { minPrice: this.minPrice } : {}),
          ...(this.maxPrice != null ? { maxPrice: this.maxPrice } : {}),
          ...(this.radiusKm != null ? { latitude: numeric('latitude'), longitude: numeric('longitude'), radiusKm: this.radiusKm } : {}),
        });
        this.closeShopDetails();
      }),
    );
    this.subscriptions.add(
      this.selection
        .pipe(
          switchMap((id) => {
            this.selectedShop = null;
            this.selectionError = '';
            this.selectionLoading = !!id;
            return id
              ? this.shops.getShopById(id).pipe(
                  catchError((error) => {
                    this.selectionError = apiError(error);
                    return of(null);
                  }),
                )
              : of(null);
          }),
        )
        .subscribe((shop) => {
          this.selectedShop = shop;
          this.selectionLoading = false;
        }),
    );
  }
  filtersChanged(): void {
    this.navigatePage(0, true);
  }
  navigatePage(page: number, replaceUrl = false): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl,
      queryParams: {
        q: this.searchQuery || null,
        category: this.category || null,
        availableOnly: this.availableOnly ? null : 'false',
        page: page || null,
        fuzzy: this.fuzzy ? 'true' : null,
        minPrice: this.minPrice,
        maxPrice: this.maxPrice,
        currency: null,
        latitude: this.radiusKm != null ? this.currentLocation?.lat ?? this.search.query.latitude : null,
        longitude: this.radiusKm != null ? this.currentLocation?.lng ?? this.search.query.longitude : null,
        radiusKm: this.radiusKm,
      },
    });
  }
  selectProduct(product: ProductMatch): void {
    this.selection.next(product.shopId);
  }
  onShopSelected(shop: Shop): void {
    this.selection.next(shop.id);
  }
  closeShopDetails(): void {
    this.selection.next(null);
    this.selectedShop = null;
  }
  locateUser(): void {
    this.locationSubscription?.unsubscribe();
    this.locationSubscription = this.location.watchLocation().subscribe({
      next: (location) => {
        this.currentLocation = location;
        this.geolocationError = '';
      },
      error: () => {
        this.geolocationError = 'Location is unavailable. Product search still works without it.';
      },
    });
  }
  onNavigateToShop(shop: Shop): void {
    const destination = encodeURIComponent(`${shop.latitude},${shop.longitude}`);
    window.open(
      `https://www.google.com/maps/dir/?api=1&destination=${destination}`,
      '_blank',
      'noopener,noreferrer',
    );
  }
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.locationSubscription?.unsubscribe();
  }
}
