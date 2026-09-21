import {
  Component,
  Input,
  Output,
  EventEmitter,
  AfterViewInit,
  OnChanges,
  SimpleChanges,
  OnDestroy,
} from '@angular/core';
import { Loader } from '@googlemaps/js-api-loader';
import { Shop } from '../model/shop.model';

import { environment } from '../environment';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css'],
  standalone: true,
  imports: [],
})
export class MapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() shops: Shop[] = [];
  @Input() currentLocation: { lat: number; lng: number } | null = null;

  @Output() shopMarkerClicked = new EventEmitter<Shop>();
  @Output() locateUser = new EventEmitter<void>();

  private map: google.maps.Map | null = null;
  private markers: google.maps.Marker[] = [];
  private currentLocationMarker: google.maps.Marker | null = null;
  private infoWindow: google.maps.InfoWindow | null = null;

  categories = ['Clothing', 'Electronics', 'Grocery', 'Books', 'Pharmacy', 'Restaurant'];

  readonly API_KEY = environment.googleMapsApiKey;

  loadingMap: boolean = !!this.API_KEY;
  mapError = this.API_KEY ? '' : 'Map is not configured. Use the product results above.';
  private destroyed = false;

  ngAfterViewInit(): void {
    this.loadGoogleMaps();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['shops'] && this.map) {
      this.updateMarkers();
    }

    if (changes['currentLocation'] && this.currentLocation && this.map) {
      this.addCurrentLocationMarker();
    }
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.markers.forEach((marker) => marker.setMap(null));
    this.currentLocationMarker?.setMap(null);
  }

  async loadGoogleMaps(): Promise<void> {
    if (!this.API_KEY) {
      return;
    }
    const loader = new Loader({
      apiKey: this.API_KEY,
      version: 'weekly',
    });

    try {
      await loader.load();
      if (!this.destroyed) this.initializeMap();
    } catch (error) {
      this.mapError = 'Map could not load. Product search is still available.';
      this.loadingMap = false;
    }
  }

  initializeMap(): void {
    const mapElement = document.getElementById('map') as HTMLElement;
    this.map = new google.maps.Map(mapElement, {
      center: { lat: 8.5361836, lng: 76.8829096 },
      zoom: 12,
    });

    this.infoWindow = new google.maps.InfoWindow();

    this.addMarkers();
    this.addCurrentLocationMarker();
    this.loadingMap = false;
  }

  addMarkers(): void {
    if (!this.map) return;

    this.markers.forEach((marker) => marker.setMap(null));
    this.markers = [];

    this.shops.forEach((shop) => {
      const marker = new google.maps.Marker({
        position: { lat: shop.latitude, lng: shop.longitude },
        map: this.map,
        title: shop.name,
      });

      marker.addListener('click', () => {
        this.openShopInfo(shop, marker);
        this.shopMarkerClicked.emit(shop);
      });

      this.markers.push(marker);
    });
    this.fitToMarkers();
  }

  updateMarkers(): void {
    this.addMarkers();
  }

  addCurrentLocationMarker(): void {
    if (!this.map || !this.currentLocation) return;

    if (this.currentLocationMarker) {
      this.currentLocationMarker.setPosition(this.currentLocation);
    } else {
      this.currentLocationMarker = new google.maps.Marker({
        position: this.currentLocation,
        map: this.map,
        icon: {
          url: 'https://maps.google.com/mapfiles/kml/shapes/man.png',
          scaledSize: new google.maps.Size(30, 30),
        },
        title: 'Your Location',
      });
    }

    this.map.setCenter(this.currentLocation);
    this.map.setZoom(14);
  }

  updateCurrentLocation(location: { lat: number; lng: number }): void {
    this.currentLocation = location;
    if (this.currentLocationMarker) {
      this.currentLocationMarker.setPosition(this.currentLocation);
    } else {
      this.addCurrentLocationMarker();
    }
  }

  private openShopInfo(shop: Shop, marker: google.maps.Marker): void {
    if (!this.infoWindow) return;
    const content = document.createElement('div');
    const name = document.createElement('strong');
    name.textContent = shop.name;
    const address = document.createElement('p');
    address.textContent = shop.address;
    content.append(name, address);
    if (shop.mapProductNames?.length) {
      const products = document.createElement('p');
      products.textContent = `Matching: ${shop.mapProductNames.slice(0, 3).join(', ')}${shop.mapProductNames.length > 3 ? '…' : ''}`;
      content.append(products);
    }
    this.infoWindow.setContent(content);
    this.infoWindow.open({ map: this.map, anchor: marker });
  }

  private fitToMarkers(): void {
    if (!this.map || !this.shops.length) return;
    if (this.shops.length === 1) {
      this.map.setCenter({ lat: this.shops[0].latitude, lng: this.shops[0].longitude });
      this.map.setZoom(14);
      return;
    }
    const bounds = new google.maps.LatLngBounds();
    this.shops.forEach((shop) => bounds.extend({ lat: shop.latitude, lng: shop.longitude }));
    this.map.fitBounds(bounds, 48);
  }
}
