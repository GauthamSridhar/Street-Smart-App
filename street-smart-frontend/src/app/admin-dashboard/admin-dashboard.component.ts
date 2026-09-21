import { Component, OnInit, OnDestroy } from '@angular/core';

import { RouterModule } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { ShopService } from '../services/shop.service';
import { RequestsService } from '../services/requests.service';
import { apiError } from '../services/api-error';
@Component({
  selector: 'app-admin-dashboard',
  imports: [NavbarComponent, RouterModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  totalShops: number | null = null;
  pending: number | null = null;
  errorMessage = '';
  private subscription?: Subscription;
  constructor(
    private shops: ShopService,
    private approvals: RequestsService,
  ) {}
  ngOnInit(): void {
    this.subscription = forkJoin({
      shops: this.shops.search(0, 1),
      pending: this.approvals.getRequestsCount(),
    }).subscribe({
      next: (data) => {
        this.totalShops = data.shops.totalElements;
        this.pending = data.pending;
      },
      error: (error) => {
        this.errorMessage = apiError(error);
      },
    });
  }
  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
