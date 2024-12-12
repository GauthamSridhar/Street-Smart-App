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
import { environment } from '../environment';

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

  categories = [ 'Clothing', 'Electronics','Grocery','Books','Pharmacy','Restaurant'];

  readonly API_KEY = environment.googleMapsApiKey;

  currentNavigation: google.maps.DirectionsResult | null = null;
  loadingMap: boolean = true;
  loadingNavigation: boolean = false;

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
    if (this.directionsRenderer) {
      this.directionsRenderer.setMap(null);
    }
  }

  async loadGoogleMaps(): Promise<void> {
    const loader = new Loader({
      apiKey: this.API_KEY,
      version: 'weekly',
    });

    try {
      await loader.load();
      this.initializeMap();
    } catch (error) {
      console.error('Error loading Google Maps:', error);
      this.loadingMap = false;
    }
  }

  initializeMap(): void {
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
        this.shopMarkerClicked.emit(shop);
      });

      this.markers.push(marker);
    });
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

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.searchShops.emit(query);
  }

  onFilter(event: Event): void {
    const category = (event.target as HTMLSelectElement).value;
    this.filterCategory.emit(category);
  }

  navigateToLocation(shop: Shop): void {
    if (!this.directionsService || !this.directionsRenderer || !this.currentLocation) return;

    const request: google.maps.DirectionsRequest = {
      origin: this.currentLocation,
      destination: { lat: shop.latitude, lng: shop.longitude },
      travelMode: google.maps.TravelMode.DRIVING,
    };

    this.loadingNavigation = true;

    this.directionsService.route(request, (result, status) => {
      if (status === 'OK' && result) {
        this.directionsRenderer?.setDirections(result);
        this.currentNavigation = result;
      } else {
        console.error('Directions request failed:', status);
      }
      this.loadingNavigation = false;
    });
  }

  cancelNavigation(): void {
    if (this.directionsRenderer) {
      this.directionsRenderer.setDirections({
        routes: [],
        request: {} as google.maps.DirectionsRequest,
      });
      this.currentNavigation = null;
    }
  }

  updateCurrentLocation(location: { lat: number; lng: number }): void {
    this.currentLocation = location;
    if (this.currentLocationMarker) {
      this.currentLocationMarker.setPosition(this.currentLocation);
    } else {
      this.addCurrentLocationMarker();
    }
  }
}
