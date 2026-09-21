import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormsModule } from '@angular/forms';
import { forkJoin, Subscription } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { UserService } from '../services/user.service';
import { RegisterService } from '../services/register-service.service';
import { UserResponse, UserEdit } from '../model/user-response.model';
import { OtpDialogComponent, OtpProof } from '../otp-dialog/otp-dialog.component';
import { apiError } from '../services/api-error';
import { SessionService } from '../services/session.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, OtpDialogComponent],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit, OnDestroy {
  user: UserResponse | null = null;
  edit: UserEdit = { fullName: '', email: '', phoneNumber: '' };
  busy = false;
  loading = true;
  smsEnabled = true;
  showOtp = false;
  contactInfo = '';
  proof: OtpProof | null = null;
  message = '';
  errorMessage = '';
  sessions: Array<{ id: string; createdAt: string; expiresAt: string; current: boolean }> = [];
  private subscriptions = new Subscription();
  constructor(
    private users: UserService,
    private registration: RegisterService,
    private session: SessionService,
    private router: Router,
  ) {}
  ngOnInit(): void {
    this.load();
  }
  load(): void {
    this.loading = true;
    this.subscriptions.add(
      forkJoin({ user: this.users.me(), config: this.registration.config(), sessions: this.users.sessions() }).subscribe({
        next: (data) => {
          this.user = data.user;
          this.smsEnabled = data.config.enabled;
          this.loading = false;
          this.edit = {
            fullName: data.user.fullName,
            email: data.user.email,
            phoneNumber: data.user.phoneNumber,
          };
          this.sessions = data.sessions;
        },
        error: (error) => {
          this.errorMessage = apiError(error);
          this.loading = false;
        },
      }),
    );
  }
  revokeSession(id: string, current: boolean): void {
    if (this.busy) return;
    this.busy = true;
    this.users.revokeSession(id).subscribe({
      next: () => {
        this.busy = false;
        if (current) { this.session.clear(); void this.router.navigate(['/login']); return; }
        this.sessions = this.sessions.filter((session) => session.id !== id);
      },
      error: (error) => { this.busy = false; this.errorMessage = apiError(error); },
    });
  }
  logoutAll(): void {
    if (this.busy) return;
    this.busy = true;
    this.users.logoutAll().subscribe({
      next: () => { this.session.clear(); void this.router.navigate(['/login']); },
      error: (error) => { this.busy = false; this.errorMessage = apiError(error); },
    });
  }
  get verifiedPhone(): boolean {
    return (
      !!this.proof &&
      this.proof.phoneNumber === this.edit.phoneNumber &&
      this.proof.expiresAt > Date.now()
    );
  }
  verifyPhone(): void {
    if (!/^\+[1-9]\d{9,14}$/.test(this.edit.phoneNumber)) {
      this.errorMessage = 'Use an international phone number.';
      return;
    }
    this.contactInfo = this.edit.phoneNumber;
    this.showOtp = true;
  }
  verified(proof: OtpProof | null): void {
    this.showOtp = false;
    this.proof = proof;
  }
  save(): void {
    if (!this.user || this.busy) return;
    this.message = '';
    this.errorMessage = '';
    const changedPhone = this.user.phoneNumber !== this.edit.phoneNumber;
    const sensitive = changedPhone || this.user.email.toLowerCase() !== this.edit.email.trim().toLowerCase() || !!this.edit.password;
    if (changedPhone && this.smsEnabled && !this.verifiedPhone) {
      this.errorMessage = 'Verify the new phone number first.';
      return;
    }
    const payload: UserEdit = {
      fullName: this.edit.fullName.trim(),
      email: this.edit.email.trim(),
      phoneNumber: this.edit.phoneNumber,
      ...(this.edit.currentPassword ? { currentPassword: this.edit.currentPassword } : {}),
      ...(this.edit.password ? { password: this.edit.password } : {}),
      ...(changedPhone && this.verifiedPhone
        ? { phoneVerificationToken: this.proof!.verificationToken }
        : {}),
    };
    this.busy = true;
    this.subscriptions.add(
      this.users.update(this.user.id, payload).subscribe({
        next: (user) => {
          this.user = user;
          this.busy = false;
          this.proof = null;
          this.edit.currentPassword = '';
          this.edit.password = '';
          this.message = 'Profile saved.';
          if (sensitive) { this.session.clear(); void this.router.navigate(['/login']); }
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
