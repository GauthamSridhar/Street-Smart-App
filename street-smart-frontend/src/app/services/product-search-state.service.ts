import { Injectable } from '@angular/core';
import {
  BehaviorSubject,
  timer,
  of,
  switchMap,
  map,
  startWith,
  catchError,
  shareReplay,
} from 'rxjs';
import { Page, ProductMatch, ProductQuery } from '../model/product-search';
import { ProductsService } from './products.service';
import { apiError } from './api-error';
export interface SearchState {
  loading: boolean;
  error: string;
  page: Page<ProductMatch> | null;
}
export const initialQuery: ProductQuery = {
  q: '',
  category: '',
  availableOnly: true,
  page: 0,
  size: 20,
};
@Injectable()
export class ProductSearchState {
  private querySubject = new BehaviorSubject<ProductQuery>({ ...initialQuery });
  // Cancel the old HTTP request immediately, including during the debounce interval.
  readonly state$ = this.querySubject.pipe(
    switchMap((query) =>
      timer(250).pipe(
        switchMap(() => this.products.search(query)),
        map((page) => ({ loading: false, error: '', page }) as SearchState),
        catchError((error) =>
          of({ loading: false, error: apiError(error), page: null } as SearchState),
        ),
        startWith({ loading: true, error: '', page: null } as SearchState),
      ),
    ),
    shareReplay({ bufferSize: 1, refCount: true }),
  );
  constructor(private products: ProductsService) {}
  get query(): ProductQuery {
    return this.querySubject.value;
  }
  setQuery(query: ProductQuery): void {
    this.querySubject.next({ ...query, q: query.q.trim() });
  }
  retry(): void {
    this.querySubject.next({ ...this.query });
  }
}
