
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ShopRegistrationService } from '../services/shop-registration.service';
import { Router } from '@angular/router';
import { apiError } from '../services/api-error';
import { NavbarComponent } from '../navbar/navbar.component';

interface Shopkeeper {
  name: string;
  category: string;
  description: string;
  address: string;
  latitude?: number;
  longitude?: number;
}

@Component({
  selector: 'app-shopkeeper-registration',
  standalone: true,
  imports: [FormsModule, NavbarComponent],
  templateUrl: './shopkeeper-registration.component.html',
  styleUrls: ['./shopkeeper-registration.component.css'],
})
export class ShopkeeperRegistrationComponent {
  shopkeeper: Shopkeeper = {
    name: '',
    category: '',
    description: '',
    address: '',
    latitude: undefined,
    longitude: undefined,
  };

  registrationSuccess = false;
  busy = false;
  locationError = '';
  formError = '';

  constructor(
    private shopRegistrationService: ShopRegistrationService,
    private router: Router,
  ) {}

  onSubmit(form: any): void {
    if (this.busy) return;
    this.formError = '';

    if (
      form.invalid ||
      !Number.isFinite(this.shopkeeper.latitude) ||
      !Number.isFinite(this.shopkeeper.longitude)
    ) {
      // Mark all fields as touched to show validation errors
      Object.keys(form.controls).forEach((field) => {
        const control = form.controls[field];
        control.markAsTouched({ onlySelf: true });
      });
      this.formError = 'Form is invalid. Please fill out all required fields correctly.';
      return;
    }

    const requestPayload = {
      name: this.shopkeeper.name,
      description: this.shopkeeper.description,
      address: this.shopkeeper.address,
      latitude: this.shopkeeper.latitude!,
      longitude: this.shopkeeper.longitude!,
      category: this.shopkeeper.category,
    };

    this.busy = true;
    this.shopRegistrationService.registerShop(requestPayload).subscribe({
      next: (response) => {
        this.busy = false;
        this.registrationSuccess = true;
        void this.router.navigate(['/shop-dashboard']);

        // Reset the form
        form.resetForm();
        this.shopkeeper.latitude = undefined;
        this.shopkeeper.longitude = undefined;
      },
      error: (error) => {
        this.busy = false;
        this.formError = apiError(error);
      },
    });
  }

  getLocation(): void {
    if (!navigator.geolocation) {
      this.locationError = 'Geolocation is not supported by your browser.';
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.shopkeeper.latitude = position.coords.latitude;
        this.shopkeeper.longitude = position.coords.longitude;
        this.locationError = '';
      },
      (error) => {
        switch (error.code) {
          case error.PERMISSION_DENIED:
            this.locationError = 'User denied the request for Geolocation.';
            break;
          case error.POSITION_UNAVAILABLE:
            this.locationError = 'Location information is unavailable.';
            break;
          case error.TIMEOUT:
            this.locationError = 'The request to get user location timed out.';
            break;
          default:
            this.locationError = 'An unknown error occurred.';
            break;
        }
      },
    );
  }
}
