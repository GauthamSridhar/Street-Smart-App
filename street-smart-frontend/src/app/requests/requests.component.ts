// src/app/requests/requests.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RequestsService, ShopApprovalResponseDTO } from '../services/requests.service';
import { Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from "../navbar/navbar.component";
import { Router } from '@angular/router';


@Component({
  selector: 'app-requests',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './requests.component.html',
  styleUrls: ['./requests.component.css']
})
export class RequestsComponent implements OnInit, OnDestroy {
  approvals: ShopApprovalResponseDTO[] = [];
  private subscription!: Subscription;

  // Variables for the rejection dialog
  showRejectionDialog: boolean = false;
  selectedShopId: string | null = null;
  rejectionReason: string = '';

  constructor(private requestsService: RequestsService, private router: Router) {}

  ngOnInit(): void {
    console.log('RequestsComponent initialized. Fetching approvals...');
    this.fetchApprovals();
  }

  /**
   * Fetch all pending shop approval requests.
   */
  fetchApprovals(): void {
    this.subscription = this.requestsService.getPendingRequests().subscribe(
      (approvals: ShopApprovalResponseDTO[]) => {
        console.log('Approvals retrieved successfully:', approvals);
        this.approvals = approvals;
      },
      (error) => {
        console.error('Error fetching approvals:', error);
        console.error('Check the backend logs or token validity.');
        alert('Failed to fetch shop approvals. Please try again later.');
      }
    );
  }

  /**
   * Approve a specific shop request.
   * @param shopId - The ID of the shop to approve.
   */
  approveRequest(shopId: string): void {
    console.log(`Approving request for shop ID: ${shopId}`);
    this.requestsService.approveRequest(shopId).subscribe(
      (response) => {
        console.log('Shop approved:', response);
        // Remove the approved shop from the list
        this.approvals = this.approvals.filter(a => a.shopId !== shopId);
        alert(`Shop ID: ${shopId} approved successfully.`);
      },
      (error) => {
        console.error('Error approving request:', error);
        alert(`Failed to approve Shop ID: ${shopId}. Please try again.`);
      }
    );
  }

  /**
   * Open the rejection dialog for a specific shop request.
   * @param shopId - The ID of the shop to reject.
   */
  openRejectionDialog(shopId: string): void {
    console.log(`Opening rejection dialog for shop ID: ${shopId}`);
    this.selectedShopId = shopId;
    this.rejectionReason = '';
    this.showRejectionDialog = true;
  }

  /**
   * Close the rejection dialog without taking action.
   */
  closeRejectionDialog(): void {
    console.log('Closing rejection dialog');
    this.showRejectionDialog = false;
    this.selectedShopId = null;
    this.rejectionReason = '';
  }

  /**
   * Submit the rejection of a specific shop request with a reason.
   */
  submitRejection(): void {
    if (this.selectedShopId && this.rejectionReason.trim() !== '') {
      console.log(`Rejecting shop ID: ${this.selectedShopId} with reason: "${this.rejectionReason}"`);
      this.requestsService.rejectRequest(this.selectedShopId, this.rejectionReason).subscribe(
        (response) => {
          console.log('Shop rejected:', response);
          // Remove the rejected shop from the list
          this.approvals = this.approvals.filter(a => a.shopId !== this.selectedShopId);
          alert(`Shop ID: ${this.selectedShopId} rejected successfully.`);
          this.closeRejectionDialog();
        },
        (error) => {
          console.error('Error rejecting request:', error);
          alert(`Failed to reject Shop ID: ${this.selectedShopId}. Please try again.`);
        }
      );
    } else {
      console.warn('No reason provided for rejection.');
      alert('Please provide a reason for rejection.');
    }
  }

  /**
   * Optionally, navigate to the shop details page if such a route exists.
   * @param shopId - The ID of the shop to navigate to.
   */
  navigateToShop(shopId: string): void {
    console.log(`Navigating to shop details for shop ID: ${shopId}`);
    // Example: Navigate to '/shop-details/:shopId'
    this.router.navigate(['/shop-details', shopId]).then(success => {
      if (success) {
        console.log(`Navigation to shop-details/${shopId} successful.`);
      } else {
        console.error(`Navigation to shop-details/${shopId} failed.`);
        alert('Failed to navigate to shop details.');
      }
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      console.log('Unsubscribed from getPendingRequests');
    }
  }
}
