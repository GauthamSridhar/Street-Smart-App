import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../environment';
import { AddProductRequest } from '../model/add-product-request.model';
import { ProductResponseDTO } from '../model/product-response-dto.model';
import { Page, ProductMatch, ProductQuery } from '../model/product-search';
@Injectable({ providedIn: 'root' })
export class ProductsService {
  private readonly url = environment.apiBaseUrl + '/products';
  constructor(private http: HttpClient) {}
  search(query: ProductQuery) {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(query))
      if (value !== undefined && value !== null) params = params.set(key, value);
    return this.http.get<Page<ProductMatch>>(this.url + '/search', { params });
  }
  getProductsByShop(shopId: string) {
    return this.http.get<ProductResponseDTO[]>(this.url, { params: { shopId } });
  }
  categories() { return this.http.get<string[]>(this.url + '/categories'); }
  addProduct(shopId: string, product: AddProductRequest) {
    return this.http.post<ProductResponseDTO>(this.url, product, { params: { shopId } });
  }
  updateProduct(id: string, product: AddProductRequest) {
    return this.http.put<ProductResponseDTO>(this.url + '/' + id, {
      name: product.name,
      available: product.available,
      ...(product.description !== undefined ? { description: product.description } : {}),
      ...(product.price !== undefined ? { price: product.price } : {}),
      ...(product.currency !== undefined ? { currency: product.currency } : {}),
    });
  }
  deleteProduct(id: string) {
    return this.http.delete<void>(this.url + '/' + id);
  }
  getProductsCount(shopId: string) {
    return this.http.get<number>(this.url + '/count/' + shopId);
  }
}
