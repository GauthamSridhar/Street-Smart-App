import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { RegisterService } from '../services/register-service.service';
import { apiError } from '../services/api-error';
export interface OtpProof {
  phoneNumber: string;
  verificationToken: string;
  expiresAt: number;
}
@Component({
  selector: 'app-otp-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './otp-dialog.component.html',
  styleUrls: ['./otp-dialog.component.css'],
})
export class OtpDialogComponent implements OnInit, OnDestroy {
  @Input() contactInfo = '';
  @Output() close = new EventEmitter<OtpProof | null>();
  otpCode = '';
  errorMessage = '';
  busy = false;
  resendCountdown = 0;
  private timer?: ReturnType<typeof setInterval>;
  private subscriptions = new Subscription();
  constructor(private service: RegisterService) {}
  get isResendDisabled(): boolean {
    return this.busy || this.resendCountdown > 0;
  }
  ngOnInit(): void {
    this.sendOtp();
  }
  sendOtp(): void {
    if (this.busy) return;
    this.busy = true;
    this.errorMessage = '';
    this.subscriptions.add(
      this.service.send(this.contactInfo).subscribe({
        next: () => {
          this.busy = false;
          this.resendCountdown = 60;
          clearInterval(this.timer);
          this.timer = setInterval(() => {
            if (--this.resendCountdown <= 0) clearInterval(this.timer);
          }, 1000);
        },
        error: (error) => {
          this.busy = false;
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  verifyOtp(): void {
    if (this.busy || !/^\d{6}$/.test(this.otpCode)) {
      this.errorMessage = 'Enter the six-digit code.';
      return;
    }
    this.busy = true;
    this.subscriptions.add(
      this.service.verify(this.contactInfo, this.otpCode).subscribe({
        next: (result) => {
          this.busy = false;
          this.close.emit({
            phoneNumber: this.contactInfo,
            verificationToken: result.verificationToken,
            expiresAt: Date.now() + 9 * 60 * 1000,
          });
        },
        error: (error) => {
          this.busy = false;
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  closeDialog(): void {
    this.close.emit(null);
  }
  resendOtp(): void {
    if (!this.isResendDisabled) this.sendOtp();
  }
  ngOnDestroy(): void {
    clearInterval(this.timer);
    this.subscriptions.unsubscribe();
  }
}
