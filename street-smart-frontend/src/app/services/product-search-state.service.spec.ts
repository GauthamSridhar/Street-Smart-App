import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ProductSearchState, initialQuery } from './product-search-state.service';
describe('Product search state', () => {
  let search: ProductSearchState;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ProductSearchState],
    });
    search = TestBed.inject(ProductSearchState);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('combines product, category, availability and pagination in one server request', fakeAsync(() => {
    search.setQuery({
      ...initialQuery,
      q: ' milk ',
      category: 'Grocery',
      availableOnly: false,
      page: 2,
    });
    const sub = search.state$.subscribe();
    tick(251);
    const req = http.expectOne((r) => r.url === '/api/products/search');
    expect(req.request.params.get('q')).toBe('milk');
    expect(req.request.params.get('category')).toBe('Grocery');
    expect(req.request.params.get('availableOnly')).toBe('false');
    expect(req.request.params.get('page')).toBe('2');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 2, size: 20 });
    sub.unsubscribe();
  }));
  it('cancels stale requests immediately when a new query arrives', fakeAsync(() => {
    const sub = search.state$.subscribe();
    tick(251);
    const old = http.expectOne((r) => r.url === '/api/products/search');
    search.setQuery({ ...initialQuery, q: 'rice' });
    expect(old.cancelled).toBeTrue();
    tick(251);
    const latest = http.expectOne((r) => r.params.get('q') === 'rice');
    latest.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 });
    sub.unsubscribe();
  }));
  it('recovers from errors without killing the search stream', fakeAsync(() => {
    let message = '';
    const sub = search.state$.subscribe((state) => (message = state.error));
    tick(251);
    http
      .expectOne((r) => r.url === '/api/products/search')
      .flush({}, { status: 503, statusText: 'Unavailable' });
    expect(message).toContain('temporarily unavailable');
    search.retry();
    tick(251);
    http
      .expectOne((r) => r.url === '/api/products/search')
      .flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 });
    expect(message).toBe('');
    sub.unsubscribe();
  }));
});
