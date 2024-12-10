// src/app/services/user.service.ts
import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

type GeolocationPermissionState = 'granted' | 'denied' | 'prompt';

@Injectable({
  providedIn: 'root',
})
export class UserService implements OnDestroy {
  private locationSubject = new BehaviorSubject<GeolocationPosition | null>(null);
  location$: Observable<GeolocationPosition | null> = this.locationSubject.asObservable();

  private permissionSubject = new BehaviorSubject<GeolocationPermissionState>('prompt');
  permission$: Observable<GeolocationPermissionState> = this.permissionSubject.asObservable();

  private watchId: number | null = null;

  constructor() {
    const storedPermission = localStorage.getItem('geolocationPermission') as GeolocationPermissionState | null;
    if (storedPermission) {
      this.permissionSubject.next(storedPermission);
      if (storedPermission === 'granted') {
        this.startTracking();
      }
    } else {
      this.checkLocationPermission();
    }
  }

  ngOnDestroy(): void {
    this.stopTracking();
  }
  async checkLocationPermission(): Promise<void> {
    if (!navigator.permissions) {
      this.permissionSubject.next('prompt');
      return;
    }
  
    try {
      const permissionStatus = await navigator.permissions.query({
        name: 'geolocation' as PermissionName,
      });
  
      // Handle initial state
      if (permissionStatus.state === 'granted') {
        this.permissionSubject.next('granted');
        localStorage.setItem('geolocationPermission', 'granted');
        this.startTracking();
      } else if (permissionStatus.state === 'denied') {
        this.permissionSubject.next('denied');
        localStorage.setItem('geolocationPermission', 'denied');
      } else {
        this.permissionSubject.next('prompt');
      }
  
      // Listen for changes in permission state
      permissionStatus.onchange = () => {
        this.permissionSubject.next(permissionStatus.state);
        localStorage.setItem('geolocationPermission', permissionStatus.state);
  
        // Handle state changes dynamically
        if (permissionStatus.state === 'granted') {
          this.startTracking();
        } else if (permissionStatus.state === 'denied') {
          this.stopTracking();
        }
      };
    } catch (error) {
      console.error('Error checking location permission:', error);
      this.permissionSubject.next('prompt');
    }
  }
  
  requestLocationAccess(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        this.permissionSubject.next('denied');
        reject('Geolocation is not supported by your browser.');
        return;
      }
  
      // Prompt for location access
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.locationSubject.next(position);
          this.permissionSubject.next('granted');
          localStorage.setItem('geolocationPermission', 'granted');
          this.startTracking();
          resolve();
        },
        (error) => {
          console.error('Error obtaining location:', error);
          this.permissionSubject.next('denied');
          localStorage.setItem('geolocationPermission', 'denied');
          reject(error);
        }
      );
    });
  }
  
  startTracking(): void {
    if (!navigator.geolocation) {
      console.error('Geolocation is not supported by your browser.');
      return;
    }

    if (this.watchId !== null) {
      // Already tracking
      return;
    }

    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        this.locationSubject.next(position);
      },
      (error) => {
        console.error('Error watching position:', error);
        this.permissionSubject.next('denied');
        localStorage.setItem('geolocationPermission', 'denied');
      },
      {
        enableHighAccuracy: true,
        maximumAge: 0,
        timeout: 5000,
      }
    );
  }

  stopTracking(): void {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }
  }
}
