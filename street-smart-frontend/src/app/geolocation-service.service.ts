// src/app/services/geolocation.service.ts
import { Injectable } from '@angular/core';
import { Observable, Observer } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class GeolocationService {
  getCurrentLocation(): Observable<{ lat: number; lng: number }> {
    return new Observable((observer: Observer<{ lat: number; lng: number }>) => {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            observer.next({
              lat: position.coords.latitude,
              lng: position.coords.longitude,
            });
            observer.complete();
          },
          (error) => {
            observer.error(error);
          },
          { enableHighAccuracy: true }
        );
      } else {
        observer.error(new Error('Geolocation is not supported by this browser.'));
      }
    });
  }

  watchLocation(): Observable<{ lat: number; lng: number }> {
    return new Observable((observer: Observer<{ lat: number; lng: number }>) => {
      if (navigator.geolocation) {
        const watcherId = navigator.geolocation.watchPosition(
          (position) => {
            observer.next({
              lat: position.coords.latitude,
              lng: position.coords.longitude,
            });
          },
          (error) => {
            observer.error(error);
          },
          { enableHighAccuracy: true }
        );

        // Cleanup function
        return () => {
          navigator.geolocation.clearWatch(watcherId);
        };
      } else {
        observer.error(new Error('Geolocation is not supported by this browser.'));
        // Return a no-op cleanup function
        return () => {};
      }
    });
  }
}
