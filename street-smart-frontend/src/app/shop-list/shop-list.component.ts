import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Shop } from '../model/shop.model';

// Presentation only: product queries are owned by the discovery screen.
@Component({
  selector: 'app-shop-list',
  imports: [],
  templateUrl: './shop-list.component.html',
})
export class ShopListComponent {
  @Input() shops: Shop[] = [];
  @Output() shopSelected = new EventEmitter<Shop>();
  selectShop(shop: Shop): void {
    this.shopSelected.emit(shop);
  }
}
