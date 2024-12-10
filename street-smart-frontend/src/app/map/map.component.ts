// src/app/map/map.component.ts
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
import { CommonModule } from '@angular/common';
import { environment } from '../environment'; // Adjust the path as needed

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css'],
  standalone: true,
  imports: [CommonModule],
})
export class MapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() shops: Shop[] = [];
  @Input() currentLocation: { lat: number; lng: number } | null = null;
  
  @Output() shopMarkerClicked = new EventEmitter<Shop>();
  @Output() locateUser = new EventEmitter<void>();
  @Output() searchShops = new EventEmitter<string>();
  @Output() filterCategory = new EventEmitter<string>();

  private map: google.maps.Map | null = null;
  private markers: google.maps.Marker[] = [];
  private currentLocationMarker: google.maps.Marker | null = null;
  private directionsService: google.maps.DirectionsService | null = null;
  private directionsRenderer: google.maps.DirectionsRenderer | null = null;

  categories = ['Food', 'Clothing', 'Electronics'];

  readonly API_KEY = environment.googleMapsApiKey; // Ensure this is set in your environment

  currentNavigation: google.maps.DirectionsResult | null = null;
  loadingMap: boolean = true;
  loadingNavigation: boolean = false;

  ngAfterViewInit(): void {
    console.log('MapComponent: ngAfterViewInit triggered');
    this.loadGoogleMaps();
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('MapComponent: ngOnChanges triggered', changes);

    if (changes['shops'] && this.map) {
      console.log('Shops input changed, updating markers');
      this.updateMarkers();
    }

    if (changes['currentLocation'] && this.currentLocation && this.map) {
      console.log('Current location input changed, adding location marker');
      this.addCurrentLocationMarker();
    }
  }

  ngOnDestroy(): void {
    console.log('MapComponent: ngOnDestroy triggered');
    if (this.directionsRenderer) {
      this.directionsRenderer.setMap(null);
      console.log('DirectionsRenderer map cleared');
    }
  }

  async loadGoogleMaps(): Promise<void> {
    console.log('MapComponent: Loading Google Maps API');
    const loader = new Loader({
      apiKey: this.API_KEY,
      version: 'weekly',
    });

    try {
      await loader.load();
      console.log('Google Maps API loaded successfully');
      this.initializeMap();
    } catch (error) {
      console.error('Error loading Google Maps:', error);
      this.loadingMap = false;
    }
  }

  initializeMap(): void {
    console.log('Initializing map');
    const mapElement = document.getElementById('map') as HTMLElement;
    this.map = new google.maps.Map(mapElement, {
      center: { lat: 8.5361836, lng: 76.8829096 },
      zoom: 12,
    });

    this.directionsService = new google.maps.DirectionsService();
    this.directionsRenderer = new google.maps.DirectionsRenderer();
    this.directionsRenderer.setMap(this.map);

    this.addMarkers();
    this.loadingMap = false;
    console.log('Map initialized');
  }

  addMarkers(): void {
    if (!this.map) return;

    console.log('Adding markers for shops');
    this.markers.forEach((marker) => marker.setMap(null));
    this.markers = [];

    this.shops.forEach((shop) => {
      const marker = new google.maps.Marker({
        position: { lat: shop.latitude, lng: shop.longitude },
        map: this.map,
        title: shop.name,
      });

      marker.addListener('click', () => {
        console.log(`Marker clicked for shop: ${shop.name}`);
        this.shopMarkerClicked.emit(shop);
      });

      this.markers.push(marker);
    });

    console.log('Markers added for all shops');
  }

  updateMarkers(): void {
    console.log('Updating markers');
    this.addMarkers();
  }

  addCurrentLocationMarker(): void {
    if (!this.map || !this.currentLocation) return;

    console.log('Adding current location marker');
    if (this.currentLocationMarker) {
      this.currentLocationMarker.setPosition(this.currentLocation);
      console.log('Updated position of current location marker');
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
      console.log('Created new marker for current location');
    }

    this.map.setCenter(this.currentLocation);
    this.map.setZoom(14);
  }

  emitLocateUser(): void {
    console.log('Emitting locateUser event');
    this.locateUser.emit();
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    console.log('Search input changed:', query);
    this.searchShops.emit(query);
  }

  onFilter(event: Event): void {
    const category = (event.target as HTMLSelectElement).value;
    console.log('Filter selected:', category);
    this.filterCategory.emit(category);
  }

  navigateToLocation(shop: Shop): void {
    if (!this.directionsService || !this.directionsRenderer) {
      console.error('Directions Service or Renderer not initialized');
      return;
    }

    if (!this.currentLocation) {
      console.error('Current location is not available');
      return;
    }

    console.log('Starting navigation to:', shop.name);
    const request: google.maps.DirectionsRequest = {
      origin: this.currentLocation,
      destination: { lat: shop.latitude, lng: shop.longitude },
      travelMode: google.maps.TravelMode.DRIVING,
    };

    this.loadingNavigation = true;

    this.directionsService.route(request, (result, status) => {
      if (status === google.maps.DirectionsStatus.OK && result) {
        console.log('Navigation successful');
        if (this.directionsRenderer) {
          this.directionsRenderer.setDirections(result);
        }
        this.currentNavigation = result;
      } else {
        console.error('Directions request failed:', status);
      }
      this.loadingNavigation = false;
    });
  }

  cancelNavigation(): void {
    console.log('Cancelling navigation');
    if (this.directionsRenderer) {
      this.directionsRenderer.setDirections({ routes: [], request: {} as google.maps.DirectionsRequest });
      this.currentNavigation = null;
    }
  }

  updateCurrentLocation(location: { lat: number; lng: number }): void {
    console.log('Updating current location:', location);
    this.currentLocation = location;
    if (this.currentLocationMarker) {
      this.currentLocationMarker.setPosition(this.currentLocation);
    } else {
      this.addCurrentLocationMarker();
    }
  }
}
