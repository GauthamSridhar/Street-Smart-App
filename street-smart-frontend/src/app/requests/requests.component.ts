import { Component, OnInit, OnDestroy } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { RequestsService, ShopApprovalResponseDTO } from '../services/requests.service';
import { NavbarComponent } from '../navbar/navbar.component';
import { apiError } from '../services/api-error';
@Component({
  selector: 'app-requests',
  standalone: true,
  imports: [FormsModule, RouterModule, NavbarComponent],
  templateUrl: './requests.component.html',
  styleUrls: ['./requests.component.css'],
})
export class RequestsComponent implements OnInit, OnDestroy {
  approvals: ShopApprovalResponseDTO[] = [];
  page = 0;
  loading = false;
  busy = false;
  showRejectionDialog = false;
  selectedShopId: string | null = null;
  rejectionReason = '';
  message = '';
  errorMessage = '';
  private subscriptions = new Subscription();
  constructor(private requests: RequestsService) {}
  ngOnInit(): void {
    this.fetchApprovals();
  }
  fetchApprovals(): void {
    this.loading = true;
    this.errorMessage = '';
    this.subscriptions.add(
      this.requests.getPendingRequests(this.page).subscribe({
        next: (rows) => {
          this.approvals = rows;
          this.loading = false;
          if (!rows.length && this.page > 0) {
            this.page--;
            this.fetchApprovals();
          }
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  move(delta: number): void {
    if (!this.loading && !this.busy) {
      this.page += delta;
      this.fetchApprovals();
    }
  }
  approveRequest(id: string): void {
    this.decide(id);
  }
  openRejectionDialog(id: string): void {
    this.selectedShopId = id;
    this.rejectionReason = '';
    this.showRejectionDialog = true;
  }
  closeRejectionDialog(): void {
    if (this.busy) return;
    this.selectedShopId = null;
    this.showRejectionDialog = false;
  }
  submitRejection(): void {
    if (this.selectedShopId && this.rejectionReason.trim() && this.rejectionReason.length <= 1000)
      this.decide(this.selectedShopId, this.rejectionReason.trim());
  }
  private decide(id: string, reason?: string): void {
    if (this.busy) return;
    this.busy = true;
    this.errorMessage = '';
    this.subscriptions.add(
      (reason
        ? this.requests.rejectRequest(id, reason)
        : this.requests.approveRequest(id)
      ).subscribe({
        next: (result) => {
          this.busy = false;
          this.closeRejectionDialog();
          this.message = result.deliveryPending
            ? 'Decision saved. Delivery to the shop is pending and will retry automatically.'
            : 'Decision saved.';
          this.fetchApprovals();
        },
        error: (error) => {
          this.busy = false;
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
