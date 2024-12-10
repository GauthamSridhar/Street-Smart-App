import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-otp-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './otp-dialog.component.html',
  styleUrls: ['./otp-dialog.component.css']
})
export class OtpDialogComponent implements OnInit {
  @Input() otpType: 'phone' | 'email' = 'phone'; // 'phone' or 'email'
  @Input() contactInfo: string = ''; // phone number or email
  @Output() close = new EventEmitter<{ success: boolean }>();

  otpCode: string = '';
  isResendDisabled: boolean = true;
  resendCountdown: number = 60;
  resendInterval: any;
  errorMessage: string = '';

  ngOnInit() {
    this.startResendCountdown();
    this.sendOtp();
  }

  sendOtp() {
    // TODO: Implement actual OTP sending logic via a backend service
    console.log(`Sending OTP to ${this.contactInfo}`);
  }

  verifyOtp() {
    // TODO: Implement actual OTP verification logic
    // For demonstration, assume '123456' is correct
    if (this.otpCode === '123456') {
      this.close.emit({ success: true });
    } else {
      this.errorMessage = 'Invalid OTP. Please try again.';
    }
  }

  closeDialog() {
    this.close.emit({ success: false });
  }

  resendOtp() {
    this.isResendDisabled = true;
    this.resendCountdown = 60;
    this.startResendCountdown();
    this.sendOtp();
  }

  startResendCountdown() {
    this.resendInterval = setInterval(() => {
      this.resendCountdown--;
      if (this.resendCountdown <= 0) {
        this.isResendDisabled = false;
        clearInterval(this.resendInterval);
      }
    }, 1000);
  }
}
