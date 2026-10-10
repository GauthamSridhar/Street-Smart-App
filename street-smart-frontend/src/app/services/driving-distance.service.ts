import { Injectable } from '@angular/core';
import { Loader } from '@googlemaps/js-api-loader';
import { environment } from '../environment';

export interface Coordinates { lat: number; lng: number }
export interface DrivingEstimate { kilometres: number; minutes: number }
interface RoutesClient {
  Route: { computeRoutes(request: {
    origin: Coordinates; destination: Coordinates; travelMode: 'DRIVING';
    routingPreference: 'TRAFFIC_UNAWARE'; fields: string[];
  }): Promise<{ routes?: Array<{ distanceMeters?: number; durationMillis?: number }> }> };
}

@Injectable({ providedIn: 'root' })
export class DrivingDistanceService {
  get configured(): boolean { return !!environment.googleMapsApiKey; }

  async loadClient(): Promise<RoutesClient> {
    await new Loader({ apiKey: environment.googleMapsApiKey, version: 'weekly' }).load();
    return await google.maps.importLibrary('routes') as unknown as RoutesClient;
  }

  async estimate(origin: Coordinates, destination: Coordinates): Promise<DrivingEstimate> {
    for (const point of [origin, destination]) {
      if (!Number.isFinite(point.lat) || !Number.isFinite(point.lng) ||
          Math.abs(point.lat) > 90 || Math.abs(point.lng) > 180)
        throw new Error('A valid location is required.');
    }
    if (!this.configured) throw new Error('Driving distance is unavailable. Use Open directions.');
    let timer: ReturnType<typeof setTimeout> | undefined;
    try {
      return await Promise.race([
        this.calculate(origin, destination),
        new Promise<never>((_, reject) => {
          timer = setTimeout(() => reject(new Error('Route lookup timed out. Use Open directions.')), 15000);
        }),
      ]);
    } finally { clearTimeout(timer); }
  }

  private async calculate(origin: Coordinates, destination: Coordinates): Promise<DrivingEstimate> {
    const client = await this.loadClient();
    const result = await client.Route.computeRoutes({ origin, destination, travelMode: 'DRIVING',
      routingPreference: 'TRAFFIC_UNAWARE', fields: ['distanceMeters', 'durationMillis'] });
    const route = result.routes?.[0];
    if (!route || !Number.isFinite(route.distanceMeters) || !Number.isFinite(route.durationMillis) ||
        route.distanceMeters! < 0 || route.durationMillis! < 0)
      throw new Error('No driving route is available. Use Open directions.');
    return { kilometres: route.distanceMeters! / 1000, minutes: Math.ceil(route.durationMillis! / 60000) };
  }
}
