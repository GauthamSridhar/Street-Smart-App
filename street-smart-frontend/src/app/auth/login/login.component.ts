// src/app/login/login.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { LoginService } from '../../services/login.service';
import { LoginForm } from '../../model/loginForm';
import { NavbarComponent } from "../../navbar/navbar.component"; // Ensure correct import path
import { LoginResponse } from '../../model/loginResponse';
import { ShopDetails } from '../../model/shopDetails';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  currentInput: 'phone' | 'email' = 'email'; // Default input type is email
  isLoading: boolean = false; // Indicates loading state

  // List of country codes
  countryCodes = [
    { code: '+1', country: 'USA' },
    { code: '+91', country: 'India' },
    { code: '+44', country: 'UK' },
    { code: '+61', country: 'Australia' },
    { code: '+81', country: 'Japan' },
    { code: '+49', country: 'Germany' },
  ];

  constructor(
    private formBuilder: FormBuilder,
    private loginService: LoginService,
    private router: Router
  ) { }

  ngOnInit(): void {
    console.log('Initializing LoginComponent');
    this.initializeForm();
  }

  /**
   * Initialize the reactive form with dynamic validators based on input type.
   */
  private initializeForm(): void {
    console.log('Initializing login form');
    this.loginForm = this.formBuilder.group({
      // Email Controls
      email: [{ value: '', disabled: false }, [Validators.required, Validators.email]],
      // Phone Controls
      countryCode: [{ value: '', disabled: true }, Validators.required],
      phone: [{ value: '', disabled: true }, [Validators.required, Validators.pattern('^[0-9]{10,15}$')]],
      // Common Controls
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  /**
   * Toggle between email and phone input fields.
   * @param type - 'email' or 'phone'
   */
  setInputType(type: 'phone' | 'email'): void {
    console.log(`Setting input type to: ${type}`);
    this.currentInput = type;
    if (type === 'email') {
      this.loginForm.get('email')?.enable();
      this.loginForm.get('countryCode')?.disable();
      this.loginForm.get('phone')?.disable();
      console.log('Enabled email fields and disabled phone fields');
    } else {
      this.loginForm.get('countryCode')?.enable();
      this.loginForm.get('phone')?.enable();
      this.loginForm.get('email')?.disable();
      console.log('Enabled phone fields and disabled email fields');
    }
  }

  /**
   * Helper method to check if a form control is invalid and touched.
   * @param controlName - Name of the form control
   * @returns boolean
   */
  isControlInvalid(controlName: string): boolean {
    const control = this.loginForm.get(controlName);
    const invalid = !!(control && control.invalid && (control.dirty || control.touched));
    if (invalid) {
      console.warn(`Validation failed for control: ${controlName}`);
    }
    return invalid;
  }

  /**
   * Handle form submission.
   */
  loginUser(): void {
    console.log('Login attempt initiated');
    if (this.loginForm.valid) {
      this.isLoading = true;
      console.log('Form is valid. Preparing credentials.');
      const credentials = this.prepareCredentials();
      console.log('Credentials prepared:', credentials);
      this.loginService.validateUser(credentials).subscribe({
        next: (response: LoginResponse) => {
          this.isLoading = false;
          console.log('Received response from backend:', response);
          const { jwt, username, role, id } = response;

          if (jwt && username && role && id) {
            console.log('JWT, username, role, and id received. Storing in sessionStorage.');
            sessionStorage.setItem('tokenId', jwt);
            sessionStorage.setItem('username', username);
            sessionStorage.setItem('role', role);
            sessionStorage.setItem('id', id); // Store user ID
            console.log('Token, username, role, and id stored successfully');

            // Handle role-based redirection
            this.handleRoleRedirect(role, id);
          } else {
            console.error('Required data not found in response');
            this.handleLoginError('Login failed. Incomplete response from server.');
          }
        },
        error: (err: any) => {
          this.isLoading = false;
          console.error('Login failed with error:', err);
          // Handle specific error scenarios
          if (err instanceof HttpErrorResponse && err.status === 500) {
            // Assuming a 500 error during login indicates a shop registration issue
            this.router.navigate(['/shop-registration']).then(() => {
              console.log('Redirected to /shop-registration due to server error.');
              alert('Please register your shop.');
            }).catch(navErr => {
              console.error('Navigation to /shop-registration failed:', navErr);
              this.handleLoginError('Login failed. Please try again.');
            });
          } else {
            this.handleLoginError('Login failed. Please check your credentials.');
          }
        }
      });
    } else {
      console.warn('Form is invalid. Marking all controls as touched.');
      this.markAllAsTouched();
      alert('Please fill out all fields correctly.');
    }
  }

  /**
   * Handle redirection based on user role.
   * @param role - Role of the user (USER, SHOPKEEPER, ADMIN)
   * @param id - ID of the user (used to fetch shop details for SHOPKEEPER)
   */
  private handleRoleRedirect(role: 'USER' | 'SHOPKEEPER' | 'ADMIN', id: string): void {
    switch (role) {
      case 'USER':
        console.log('Redirecting USER to /dashboard');
        this.router.navigate(['/dashboard']).then(() => {
          console.log('Navigation to /dashboard successful');
        }).catch(err => {
          console.error('Navigation to /dashboard failed:', err);
          this.handleLoginError('Navigation failed. Please try again.');
        });
        break;

      case 'SHOPKEEPER':
        console.log('SHOPKEEPER detected. Fetching shop details.');
        this.fetchShopDetails(id);
        break;

      case 'ADMIN':
        console.log('Redirecting ADMIN to /admin-dashboard');
        this.router.navigate(['/admin-dashboard']).then(() => {
          console.log('Navigation to /admin-dashboard successful');
        }).catch(err => {
          console.error('Navigation to /admin-dashboard failed:', err);
          this.handleLoginError('Navigation failed. Please try again.');
        });
        break;

      default:
        console.error('Unknown role:', role);
        this.handleLoginError('Login failed. Unknown user role.');
    }
  }

  /**
   * Fetch shop details and handle redirection based on shop status.
   * @param ownerId - ID of the shopkeeper.
   */
  private fetchShopDetails(ownerId: string): void {
    this.loginService.getShopDetails(ownerId).subscribe({
      next: (shopDetails: ShopDetails) => {
        console.log('Received shop details:', shopDetails);

        if (!shopDetails) {
          console.warn('No shop found for SHOPKEEPER. Redirecting to /shop-registration.');
          this.router.navigate(['/shop-registration']).then(() => {
            console.log('Navigation to /shop-registration successful');
            alert('Please register your shop.');
          }).catch(err => {
            console.error('Navigation to /shop-registration failed:', err);
            this.handleLoginError('Redirection failed. Please try again.');
          });
          return;
        }

        if (shopDetails.status==="PENDING") {
          console.log("shopDetails", shopDetails);
          
          console.warn('Shop exists but not verified. Redirecting to /error.');
          this.router.navigate(['/error'], { queryParams: { message: 'Shop is not verified.' } }).then(() => {
            console.log('Navigation to /error successful');
          }).catch(err => {
            console.error('Navigation to /error failed:', err);
            this.handleLoginError('Redirection failed. Please try again.');
          });
          return;
        }
        else if(shopDetails.status==="REJECTED"){
          console.warn('Shop exists but not verified. Redirecting to /error.');
          this.router.navigate(['/shop-rejected'], { queryParams: { message: 'Shop is rejected.' } }).then(() => {
            console.log('Navigation to /error successful');
          }).catch(err => {
            console.error('Navigation to /error failed:', err);
            this.handleLoginError('Redirection failed. Please try again.');
          });
          return
        }

        // Shop exists and is verified
        console.log('Shop is verified. Redirecting to /shop-dashboard.');
        this.router.navigate(['/shop-dashboard']).then(() => {
          console.log('Navigation to /shop-dashboard successful');
          sessionStorage.setItem('shopId', shopDetails.id); // Store shop ID
        }).catch(err => {
          console.error('Navigation to /shop-dashboard failed:', err);
          this.handleLoginError('Navigation failed. Please try again.');
        });
      },
      error: (err: any) => {
        console.error('Failed to fetch shop details:', err);
        if (err instanceof HttpErrorResponse && err.status === 500) {
          console.warn('Shop details not found (500). Redirecting to /shop-registration.');
          this.router.navigate(['/shop-registration']).then(() => {
            console.log('Navigation to /shop-registration successful');
            alert('Please register your shop.');
          }).catch(navErr => {
            console.error('Navigation to /shop-registration failed:', navErr);
            this.handleLoginError('Redirection failed. Please try again.');
          });
        } else {
          this.handleLoginError('Login failed. Unable to retrieve shop details.');
        }
      }
    });
  }

  /**
   * Prepare the credentials object based on the current input type.
   * @returns { identifier: string, password: string }
   */
  private prepareCredentials(): LoginForm {
    if (this.currentInput === 'email') {
      const email = this.loginForm.get('email')?.value;
      const password = this.loginForm.get('password')?.value;
      console.log('Using email for login:', email);
      return {
        identifier: email,
        password: password
      };
    } else {
      const countryCode = this.loginForm.get('countryCode')?.value;
      const phone = this.loginForm.get('phone')?.value;
      const combinedPhone = `${countryCode}${phone}`;
      console.log('Using phone for login:', combinedPhone);
      return {
        identifier: combinedPhone,
        password: this.loginForm.get('password')?.value
      };
    }
  }

  /**
   * Mark all form controls as touched to trigger validation messages.
   */
  private markAllAsTouched(): void {
    console.log('Marking all form controls as touched');
    Object.values(this.loginForm.controls).forEach(control => {
      control.markAsTouched();
    });
  }

  /**
   * Handle login errors by displaying appropriate messages.
   * @param message - Error message to display
   */
  private handleLoginError(message: string): void {
    console.error(message);
    alert(message);
  }
}
