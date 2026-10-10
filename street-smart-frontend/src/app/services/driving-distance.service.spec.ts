import { DrivingDistanceService } from './driving-distance.service';
import { environment } from '../environment';

describe('Driving distance', () => {
  let service: DrivingDistanceService;
  const origin = { lat: 18.62, lng: 79.94 };
  const destination = { lat: 18.63, lng: 79.95 };
  let original: string;
  beforeEach(() => { service = new DrivingDistanceService(); original = environment.googleMapsApiKey; environment.googleMapsApiKey = 'unit-test'; });
  afterEach(() => { environment.googleMapsApiKey = original; });
  it('requests a driving route and converts metres and milliseconds', async () => {
    const computeRoutes = jasmine.createSpy().and.resolveTo({ routes: [{ distanceMeters: 4200, durationMillis: 540000 }] });
    spyOn(service, 'loadClient').and.resolveTo({ Route: { computeRoutes } });
    expect(await service.estimate(origin, destination)).toEqual({ kilometres: 4.2, minutes: 9 });
    expect(computeRoutes.calls.mostRecent().args[0]).toEqual(jasmine.objectContaining({ travelMode: 'DRIVING', origin, destination }));
  });
  it('does not invent a distance when no route exists', async () => {
    spyOn(service, 'loadClient').and.resolveTo({ Route: { computeRoutes: () => Promise.resolve({ routes: [] }) } });
    await expectAsync(service.estimate(origin, destination)).toBeRejectedWithError(/No driving route/);
  });
  it('does not contact Google without a configured key', async () => {
    environment.googleMapsApiKey = '';
    const load = spyOn(service, 'loadClient');
    await expectAsync(service.estimate(origin, destination)).toBeRejectedWithError(/unavailable/);
    expect(load).not.toHaveBeenCalled();
  });
  it('rejects invalid coordinates before contacting the provider', async () => {
    const load = spyOn(service, 'loadClient');
    await expectAsync(service.estimate({ lat: NaN, lng: 0 }, destination)).toBeRejectedWithError(/valid location/);
    expect(load).not.toHaveBeenCalled();
  });
});
