import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { LoginService } from '../../services/login.service';
import { SessionService } from '../../services/session.service';
import { apiError } from '../../services/api-error';
import { NavbarComponent } from '../../navbar/navbar.component';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  currentInput: 'phone' | 'email' = 'email';
  isLoading = false;
  formError = '';
  countryCodes = [
    { code: '+91', country: 'India' },
    { code: '+1', country: 'USA' },
    { code: '+44', country: 'UK' },
    { code: '+61', country: 'Australia' },
  ];
  constructor(
    private fb: FormBuilder,
    private loginService: LoginService,
    private session: SessionService,
    private router: Router,
  ) {}
  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      countryCode: [{ value: '+91', disabled: true }, Validators.required],
      phone: [
        { value: '', disabled: true },
        [Validators.required, Validators.pattern(/^[0-9]{4,14}$/)],
      ],
      password: ['', Validators.required],
    });
  }
  setInputType(type: 'phone' | 'email'): void {
    this.currentInput = type;
    for (const name of ['email', 'countryCode', 'phone']) {
      const enabled = type === 'email' ? name === 'email' : name !== 'email';
      if (enabled) this.loginForm.get(name)?.enable();
      else this.loginForm.get(name)?.disable();
    }
  }
  isControlInvalid(name: string): boolean {
    const c = this.loginForm.get(name);
    return !!c && c.invalid && (c.dirty || c.touched);
  }
  loginUser(): void {
    if (this.isLoading) return;
    this.formError = '';
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    const value = this.loginForm.getRawValue();
    const identifier =
      this.currentInput === 'email' ? value.email.trim() : value.countryCode + value.phone.trim();
    this.isLoading = true;
    this.loginService.validateUser({ identifier, password: value.password }).subscribe({
      next: (response) => {
        this.isLoading = false;
        try {
          this.session.save(response);
          void this.router.navigateByUrl(this.session.home);
        } catch {
          this.formError = 'The server returned an invalid session. Please try again.';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.formError = apiError(error);
      },
    });
  }
}
