import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';

import { ShopRegistrationService } from './shop-registration.service';

describe('ShopRegistrationService', () => {
  let service: ShopRegistrationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(ShopRegistrationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
