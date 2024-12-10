// src/app/products/products.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from "../navbar/navbar.component";
import { ProductsService } from '../services/products.service';
import { ProductResponseDTO } from '../model/product-response-dto.model'; // Ensure correct path
import { AddProductRequest } from '../model/add-product-request.model'; // Ensure correct path
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class ProductsComponent implements OnInit, OnDestroy {
  newProduct: AddProductRequest = { name: '', available: true };
  products: ProductResponseDTO[] = [];
  private subscriptions: Subscription = new Subscription();
  shopId: string = '';

  constructor(
    private productsService: ProductsService
  ) {}

  ngOnInit(): void {
    // Retrieve shopId from session storage
    const storedShopId = sessionStorage.getItem('shopId');
    console.log('Retrieved shopId from session storage:', storedShopId);
    
    if (storedShopId) {
      this.shopId = storedShopId;
      console.log(`Shop ID set to: ${this.shopId}`);
      this.fetchProducts();
    } else {
      console.error('No shopId found in session storage.');
      // alert('Invalid shop. Please select a valid shop.');
      // Optionally, redirect the user to the shop selection page
    }
  }

  /**
   * Fetch all products for the current shop.
   */
  fetchProducts(): void {
    const sub = this.productsService.getProductsByShop(this.shopId).subscribe({
      next: (products: ProductResponseDTO[]) => {
        this.products = products;
        console.log('Products loaded:', this.products);
      },
      error: (error: Error) => {
        console.error('Error fetching products:', error.message);
        // alert('Failed to load products. Please try again later.');
      }
    });
    this.subscriptions.add(sub);
  }

  /**
   * Adds a new product to the shop.
   */
  addProduct(): void {
    if (this.newProduct.name.trim()) {
      const productToAdd: AddProductRequest = { ...this.newProduct };
      console.log('Submitting new product:', productToAdd);

      const sub = this.productsService.addProduct(this.shopId, productToAdd).subscribe({
        next: (addedProduct: ProductResponseDTO) => {
          this.products.push(addedProduct);
          console.log('Product added successfully:', addedProduct);
          this.newProduct = { name: '', available: true }; // Reset form
          // alert(`Product "${addedProduct.name}" added successfully.`);
        },
        error: (error: Error) => {
          console.error('Error adding product:', error.message);
          // alert(`Failed to add product. ${error.message}`);
        }
      });

      this.subscriptions.add(sub);
    } else {
      // alert('Product name cannot be empty.');
    }
  }

  /**
   * Toggles the availability status of a product.
   * @param product - The product to toggle.
   */
  toggleAvailability(product: ProductResponseDTO): void {
    const updatedStatus = !product.available;
    console.log(`Toggling availability for Product ID: ${product.id} to ${updatedStatus}`);

    const sub = this.productsService.toggleAvailability(product.id, updatedStatus).subscribe({
      next: (updatedProduct: ProductResponseDTO) => {
        // Update the local product list
        const index = this.products.findIndex(p => p.id === updatedProduct.id);
        if (index !== -1) {
          this.products[index].available = updatedProduct.available;
          console.log('Product availability updated:', updatedProduct);
          // alert(`Product "${updatedProduct.name}" is now ${updatedProduct.available ? 'Available' : 'Unavailable'}.`);
        }
      },
      error: (error: Error) => {
        console.error('Error updating product availability:', error.message);
        // alert(`Failed to update product availability. ${error.message}`);
      }
    });

    this.subscriptions.add(sub);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    console.log('ProductsComponent destroyed and subscriptions unsubscribed.');
  }
}
