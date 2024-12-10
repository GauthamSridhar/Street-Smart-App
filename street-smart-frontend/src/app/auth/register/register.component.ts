// src/app/register/register.component.ts

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { RegisterService } from '../../services/register-service.service';
import { RegisterPayload } from '../../model/registerPayload';
import { RegisterResponse } from '../../model/RegisterResponse';
import { NavbarComponent } from '../../navbar/navbar.component'; // Ensure correct import path
import { OtpDialogComponent } from '../../otp-dialog/otp-dialog.component'; // Ensure correct import path
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    NavbarComponent,
    OtpDialogComponent,
    FormsModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent {
  // Registration Form
  registerForm: FormGroup;

  // Verification status
  isPhoneVerified: boolean = false;
  isEmailVerified: boolean = false;

  // Control for OTP Dialog
  showOtpDialog: boolean = false;
  otpType: 'phone' | 'email' = 'phone'; // 'phone' or 'email'
  contactInfo: string = '';

  // Error message
  formError: string = '';

  // Loading state
  isLoading: boolean = false;

  // Country codes
  countryCodes = [
    { code: '+1', country: 'USA' },
    { code: '+91', country: 'India' },
    { code: '+44', country: 'UK' },
    { code: '+61', country: 'Australia' },
    { code: '+81', country: 'Japan' },
    { code: '+49', country: 'Germany' },
    // Add more as needed
  ];

  constructor(
    private formBuilder: FormBuilder,
    private registerService: RegisterService,
    private router: Router
  ) {
    this.registerForm = this.formBuilder.group({
      // Phone Number
      phoneCountryCode: ['', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10,15}$')]],

      // Email
      email: ['', [Validators.required, Validators.email]],

      // Full Name
      fullName: ['', [Validators.required, Validators.minLength(2)]],

      // Role
      role: ['', Validators.required],

      // Password
      password: ['', [Validators.required, Validators.minLength(8), this.passwordStrengthValidator]],

      // Confirm Password
      confirmPassword: ['', Validators.required],
    }, {
      validators: this.passwordMatchValidator
    });
  }

  /**
   * Custom validator to check password strength.
   */
  passwordStrengthValidator(control: any) {
    const value = control.value;
    if (!value) return null;

    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumeric = /[0-9]/.test(value);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(value);

    const valid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecialChar;
    if (!valid) {
      return { weakPassword: true };
    }
    return null;
  }

  /**
   * Custom validator to check if password and confirmPassword match.
   */
  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    if (password !== confirmPassword) {
      form.get('confirmPassword')?.setErrors({ passwordMismatch: true });
    } else {
      form.get('confirmPassword')?.setErrors(null);
    }
  }

  /**
   * Open the OTP dialog for phone or email verification.
   * @param type - 'phone' or 'email'
   */
  openOtpDialog(type: 'phone' | 'email') {
    console.log(`Requesting OTP for: ${type}`);
    if (type === 'phone') {
      const phoneInput = this.registerForm.get('phoneNumber')?.value.trim();
      const countryCode = this.registerForm.get('phoneCountryCode')?.value;
      if (phoneInput && countryCode) {
        this.contactInfo = `${countryCode}${phoneInput}`;
        this.otpType = 'phone';
        this.showOtpDialog = true;
        console.log(`Opening OTP dialog for phone: ${this.contactInfo}`);
      } else {
        alert('Please enter your phone number and select a country code before requesting an OTP.');
      }
    } else if (type === 'email') {
      const emailInput = this.registerForm.get('email')?.value.trim();
      if (emailInput) {
        this.contactInfo = emailInput;
        this.otpType = 'email';
        this.showOtpDialog = true;
        console.log(`Opening OTP dialog for email: ${this.contactInfo}`);
      } else {
        alert('Please enter your email before requesting an OTP.');
      }
    }
  }

  /**
   * Handle the closure of the OTP dialog.
   * @param event - Contains the success status of OTP verification.
   */
  onOtpDialogClose(event: { success: boolean }) {
    this.showOtpDialog = false;
    if (event.success) {
      if (this.otpType === 'phone') {
        this.isPhoneVerified = true;
        alert('Phone number verified successfully.');
        console.log('Phone number verified.');
      } else if (this.otpType === 'email') {
        this.isEmailVerified = true;
        alert('Email verified successfully.');
        console.log('Email verified.');
      }
    } else {
      alert('OTP verification failed. Please try again.');
      console.warn('OTP verification failed.');
    }
  }

  /**
   * Handle user registration.
   */
  registerUser() {
    console.log('Registration attempt initiated.');
    this.formError = '';

    // Check if at least one contact method is provided
    const hasPhone = this.registerForm.get('phoneNumber')?.value.trim() !== '' && this.registerForm.get('phoneCountryCode')?.value.trim() !== '';
    const hasEmail = this.registerForm.get('email')?.value.trim() !== '';

    // Validate that at least one is provided
    if (!hasPhone && !hasEmail) {
      this.formError = 'Please provide at least an Email or a Phone Number.';
      console.warn('Registration failed: No contact method provided.');
      return;
    }

    // If phone is provided, it must be verified
    if (hasPhone && !this.isPhoneVerified) {
      this.formError = 'Please verify your phone number.';
      console.warn('Registration failed: Phone number not verified.');
      return;
    }

    // If email is provided, it must be verified
    if (hasEmail && !this.isEmailVerified) {
      this.formError = 'Please verify your email.';
      console.warn('Registration failed: Email not verified.');
      return;
    }

    // Check if form is valid
    if (this.registerForm.invalid) {
      this.formError = 'Please correct the errors in the form.';
      console.warn('Registration failed: Form is invalid.');
      return;
    }

    // Check password match and strength are already handled by validators

    // Prepare registration payload
    const registrationPayload = this.prepareRegistrationData();
    console.log('Prepared registration payload:', registrationPayload);

    // Initiate registration
    this.isLoading = true;
    this.registerService.registerUser(registrationPayload).subscribe({
      next: (response: RegisterResponse) => {
        this.isLoading = false;
        console.log('Received response from backend:', response);

        // Assuming response contains a message and user data
        if (response && response.id) { // Adjust based on actual backend response
          alert('Registration successful! Please login.');
          console.log('Registration successful. Navigating to /login.');
          this.router.navigate(['/login']);
        } else {
          this.formError = response.message || 'Registration failed. Please try again.';
          console.error('Registration failed:', response.message);
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        // Handle specific error scenarios based on backend response
        if (error instanceof HttpErrorResponse) {
          if (error.status === 409) { // Conflict, e.g., email or phone already exists
            this.formError = error.error.message || 'Email or phone number already exists.';
          } else if (error.status === 400) { // Bad Request, e.g., validation errors
            this.formError = error.error.message || 'Invalid registration data.';
          } else {
            this.formError = 'An error occurred during registration. Please try again later.';
          }
          console.error('Registration error:', error);
        } else {
          this.formError = 'An unexpected error occurred. Please try again.';
          console.error('Unknown registration error:', error);
        }
      }
    });
  }

  /**
   * Prepare the registration data payload for the backend.
   * @returns RegistrationPayload object
   */
  private prepareRegistrationData(): RegisterPayload {
    const payload: RegisterPayload = {
      phoneNumber: this.isPhoneVerified
        ? `${this.registerForm.get('phoneCountryCode')?.value}${this.registerForm.get('phoneNumber')?.value}`
        : '',
      email: this.isEmailVerified ? this.registerForm.get('email')?.value : '',
      fullName: this.registerForm.get('fullName')?.value.trim(),
      role: this.registerForm.get('role')?.value,
      password: this.registerForm.get('password')?.value
    };

    console.log('Final registration payload:', payload);
    return payload;
  }
}
