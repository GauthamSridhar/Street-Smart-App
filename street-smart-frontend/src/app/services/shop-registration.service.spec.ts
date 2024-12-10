import { TestBed } from '@angular/core/testing';

import { ShopRegistrationService } from './shop-registration.service';

describe('ShopRegistrationService', () => {
  let service: ShopRegistrationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ShopRegistrationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
