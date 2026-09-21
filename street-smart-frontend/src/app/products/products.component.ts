import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { ProductsService } from '../services/products.service';
import { ShopService } from '../services/shop.service';
import { SessionService } from '../services/session.service';
import { ProductResponseDTO } from '../model/product-response-dto.model';
import { AddProductRequest } from '../model/add-product-request.model';
import { apiError } from '../services/api-error';
@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css'],
})
export class ProductsComponent implements OnInit, OnDestroy {
  newProduct: AddProductRequest = { name: '', available: true };
  products: ProductResponseDTO[] = [];
  shopId = '';
  errorMessage = '';
  busy = false;
  private subscriptions = new Subscription();
  constructor(
    private productsService: ProductsService,
    private shops: ShopService,
    private session: SessionService,
  ) {}
  ngOnInit(): void {
    this.subscriptions.add(
      this.shops.getShopByShopkeeperId(this.session.id).subscribe({
        next: (shop) => {
          this.shopId = shop.id;
          this.fetchProducts();
        },
        error: (error) => {
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  fetchProducts(): void {
    this.subscriptions.add(
      this.productsService.getProductsByShop(this.shopId).subscribe({
        next: (rows) => {
          this.products = rows;
        },
        error: (error) => {
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  addProduct(): void {
    if (this.busy || !this.shopId || !this.newProduct.name.trim()) return;
    this.busy = true;
    this.errorMessage = '';
    this.subscriptions.add(
      this.productsService
        .addProduct(this.shopId, { ...this.newProduct, name: this.newProduct.name.trim(),
          currency: this.newProduct.price != null ? 'INR' : null })
        .subscribe({
          next: (product) => {
            this.products = [...this.products, product];
            this.newProduct = { name: '', available: true };
            this.busy = false;
          },
          error: (error) => {
            this.errorMessage = apiError(error);
            this.busy = false;
          },
        }),
    );
  }
  toggleAvailability(product: ProductResponseDTO): void {
    if (this.busy) return;
    this.busy = true;
    this.errorMessage = '';
    this.subscriptions.add(
      this.productsService
        .updateProduct(product.id, { name: product.name, description: product.description,
          price: product.price, currency: product.price != null ? 'INR' : null, available: !product.available })
        .subscribe({
          next: (updated) => {
            this.products = this.products.map((p) => (p.id === updated.id ? updated : p));
            this.busy = false;
          },
          error: (error) => {
            this.errorMessage = apiError(error);
            this.busy = false;
          },
        }),
    );
  }
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
