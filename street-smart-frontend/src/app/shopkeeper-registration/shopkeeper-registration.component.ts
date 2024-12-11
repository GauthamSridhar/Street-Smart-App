import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ShopRegistrationService } from '../services/shop-registration.service';
import { toast } from 'ngx-sonner';
import { NavbarComponent } from "../navbar/navbar.component";

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
  imports: [FormsModule, CommonModule, NavbarComponent],
  templateUrl: './shopkeeper-registration.component.html',
  styleUrls: ['./shopkeeper-registration.component.css']
})
export class ShopkeeperRegistrationComponent {
  shopkeeper: Shopkeeper = {
    name: '',
    category: '',
    description: '',
    address: '',
    latitude: undefined,
    longitude: undefined
  };

  registrationSuccess = false;
  locationError = '';
  formError = '';

  constructor(private shopRegistrationService: ShopRegistrationService) {}

  onSubmit(form: any): void {
    this.formError = '';

    if (form.invalid) {
      // Mark all fields as touched to show validation errors
      Object.keys(form.controls).forEach(field => {
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
      category: this.shopkeeper.category
    };

    console.log('Submitting Registration Form...');
    console.log('Shopkeeper Data before submission:', requestPayload);

    this.shopRegistrationService.registerShop(requestPayload).subscribe({
      next: (response) => {
        console.log('Registration successful:', response);
        this.registrationSuccess = true;


        // Reset the form
        form.resetForm();
        this.shopkeeper.latitude = undefined;
        this.shopkeeper.longitude = undefined;
        console.log('Form has been reset.');
      },
      error: (error) => {
        console.error('Registration failed:', error);
        this.formError = `Registration failed: ${error.message}`;
      }
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
        console.log('Location obtained:', { latitude: this.shopkeeper.latitude, longitude: this.shopkeeper.longitude });
      },
      (error) => {
        switch(error.code) {
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
      }
    );
  }
}
