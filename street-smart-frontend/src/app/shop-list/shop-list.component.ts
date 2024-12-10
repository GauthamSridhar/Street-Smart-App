import { Component, Input, Output, EventEmitter, NgModule } from '@angular/core';
import { Shop } from '../model/shop.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-shop-list',
  templateUrl: './shop-list.component.html',
  imports:[CommonModule,FormsModule]
})
export class ShopListComponent {
  @Input() shops: Shop[] = [];
  @Output() shopSelected = new EventEmitter<Shop>();

  searchQuery = '';
  selectedCategory = '';
  filteredShops: Shop[] = [];
  categories: string[] = ['Food', 'Clothing', 'Electronics'];

  ngOnInit(): void {
    this.filteredShops = [...this.shops];
  }

  filterShops(): void {
    this.filteredShops = this.shops.filter(
      (shop) =>
        shop.name.toLowerCase().includes(this.searchQuery.toLowerCase()) &&
        (!this.selectedCategory || shop.category === this.selectedCategory)
    );
  }

  selectShop(shop: Shop): void {
    this.shopSelected.emit(shop);
  }
}
