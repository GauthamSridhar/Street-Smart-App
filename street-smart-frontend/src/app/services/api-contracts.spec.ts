import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ProductsService } from './products.service';
import { ReviewService } from './review.service';
import { ShopService } from './shop.service';
describe('Editable API payloads', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('sends product name and intended availability together', () => {
    TestBed.inject(ProductsService)
      .updateProduct('p', { name: 'Milk', available: false })
      .subscribe();
    const req = http.expectOne('/api/products/p');
    expect(req.request.body).toEqual({ name: 'Milk', available: false });
    req.flush({});
  });
  it('does not send user identity or display names in a review body', () => {
    const value = { rating: 4, review: 'Helpful', userId: 'forged', userName: 'Not authoritative' };
    TestBed.inject(ReviewService).addReview('s', value).subscribe();
    const req = http.expectOne((r) => r.url === '/api/ratings/add');
    expect(req.request.body).toEqual({ rating: 4, review: 'Helpful' });
    req.flush({});
  });
  it('uses the owner endpoint, not an unscoped shop list', () => {
    TestBed.inject(ShopService).getShopByShopkeeperId('owner').subscribe();
    const req = http.expectOne('/api/shops/owner/owner');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
  it('strips noneditable shop fields while preserving the full edit contract', () => {
    const value = {
      name: 'Shop',
      description: 'Local shop',
      address: 'Road',
      category: 'Grocery',
      latitude: 10,
      longitude: 76,
      ownerId: 'forged',
      status: 'APPROVED',
    };
    TestBed.inject(ShopService).updateShop('s', value).subscribe();
    const req = http.expectOne('/api/shops/s');
    expect(req.request.body.ownerId).toBeUndefined();
    expect(req.request.body.status).toBeUndefined();
    expect(req.request.body.category).toBe('Grocery');
    req.flush({});
  });
});
