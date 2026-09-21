import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';

import { FavoritesService } from './favorite-service.service';

describe('FavoriteServiceService', () => {
  let service: FavoritesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(FavoritesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
