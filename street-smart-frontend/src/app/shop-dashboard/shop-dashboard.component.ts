// src/app/shop-dashboard/shop-dashboard.component.ts

import { Component, OnInit } from '@angular/core';
import { ShopService } from '../services/shop-service.service';
import { ShopResponse } from '../model/shop-response.model';
import { NavbarComponent } from '../navbar/navbar.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-shop-dashboard',
  templateUrl: './shop-dashboard.component.html',
  styleUrls: ['./shop-dashboard.component.css'],
  imports:[NavbarComponent,CommonModule]
})
export class ShopDashboardComponent implements OnInit {
  shopDetails: ShopResponse | null = null;
  errorMessage: string | null = null;
  isLoading: boolean = true;

  constructor(private shopService: ShopService) {}

  ngOnInit(): void {
    // Fetch shop details for the logged-in shopkeeper
    this.shopService.getAllShops().subscribe({
      next: (shops) => {
        // Assuming the shopkeeper owns only one shop; otherwise, adjust accordingly
        this.shopDetails = shops.length ? shops[0] : null;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load shop details:', error);
        this.errorMessage = error.message;
        this.isLoading = false;
      }
    });
  }
}
