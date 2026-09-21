import { Component, OnInit } from '@angular/core';

import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { RegisterService } from '../../services/register-service.service';
import { NavbarComponent } from '../../navbar/navbar.component';
import { OtpDialogComponent, OtpProof } from '../../otp-dialog/otp-dialog.component';
import { apiError } from '../../services/api-error';
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, NavbarComponent, OtpDialogComponent],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  showOtpDialog = false;
  contactInfo = '';
  formError = '';
  isLoading = false;
  smsEnabled = true;
  configLoaded = false;
  proof: OtpProof | null = null;
  countryCodes = [
    { code: '+91', country: 'India' },
    { code: '+1', country: 'USA' },
    { code: '+44', country: 'UK' },
    { code: '+61', country: 'Australia' },
  ];
  constructor(
    private fb: FormBuilder,
    private service: RegisterService,
    private router: Router,
  ) {
    this.registerForm = fb.group(
      {
        phoneCountryCode: ['+91', Validators.required],
        phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{4,14}$/)]],
        email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
        fullName: ['', [Validators.required, Validators.maxLength(100)]],
        role: ['USER', [Validators.required, Validators.pattern(/^(USER|SHOPKEEPER)$/)]],
        password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
        confirmPassword: ['', Validators.required],
      },
      {
        validators: (control: AbstractControl) =>
          control.get('password')?.value === control.get('confirmPassword')?.value
            ? null
            : { passwordMismatch: true },
      },
    );
  }
  ngOnInit(): void {
    this.loadConfig();
  }
  loadConfig(): void {
    this.formError = '';
    this.service.config().subscribe({
      next: (config) => {
        this.smsEnabled = config.enabled;
        this.configLoaded = true;
      },
      error: (error) => {
        this.formError = apiError(error);
        this.configLoaded = false;
      },
    });
  }
  get phone(): string {
    const v = this.registerForm.getRawValue();
    return v.phoneCountryCode + v.phoneNumber.trim();
  }
  get isPhoneVerified(): boolean {
    return (
      !!this.proof && this.proof.phoneNumber === this.phone && this.proof.expiresAt > Date.now()
    );
  }
  openOtpDialog(): void {
    if (!/^\+[1-9]\d{9,14}$/.test(this.phone)) {
      this.formError = 'Enter a valid international phone number.';
      return;
    }
    this.contactInfo = this.phone;
    this.showOtpDialog = true;
  }
  onOtpDialogClose(event: OtpProof | null): void {
    this.showOtpDialog = false;
    this.proof = event;
    if (event) this.registerUser();
  }
  registerUser(): void {
    if (this.isLoading) return;
    this.formError = '';
    if (!this.configLoaded) {
      this.formError = 'Load the registration settings before continuing.';
      return;
    }
    if (this.registerForm.invalid || !/^\+[1-9]\d{9,14}$/.test(this.phone)) {
      this.registerForm.markAllAsTouched();
      this.formError = 'Please correct the form fields.';
      return;
    }
    if (this.smsEnabled && !this.isPhoneVerified) {
      this.openOtpDialog();
      return;
    }
    const v = this.registerForm.getRawValue();
    this.isLoading = true;
    this.service
      .registerUser({
        email: v.email.trim(),
        phoneNumber: this.phone,
        fullName: v.fullName.trim(),
        password: v.password,
        role: v.role,
        ...(this.isPhoneVerified ? { phoneVerificationToken: this.proof!.verificationToken } : {}),
      })
      .subscribe({
        next: () => {
          this.isLoading = false;
          void this.router.navigate(['/login']);
        },
        error: (error) => {
          this.isLoading = false;
          this.formError = apiError(error);
        },
      });
  }
}
