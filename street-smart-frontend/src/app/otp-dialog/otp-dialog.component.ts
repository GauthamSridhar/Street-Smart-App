import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';  // Import HttpClient for API calls
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
  @Input() otpType: string | undefined;  // This should be defined as @Input()
  @Input() contactInfo: string = ''; // Phone number to send OTP
  @Output() close = new EventEmitter<{ success: boolean }>();
  generatedOtp: string = '';
  otpCode: string = '';
  isResendDisabled: boolean = true;
  resendCountdown: number = 60;
  resendInterval: any;
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.startResendCountdown();
    this.sendOtp();
  }

  sendOtp() {
    // Generate the OTP on the frontend
    this.generatedOtp = Math.floor(100000 + Math.random() * 900000).toString();
  
    const payload = { 
      phoneNumber: this.contactInfo, 
      message: '',  // You can optionally pass a custom message
      otpCode: this.generatedOtp 
    };
  
    // Send the OTP to the backend as part of the request body
    this.http.post('http://localhost:8080/api/sms/send', payload).subscribe(
      (response: any) => {  // Specify the type as 'any' for generic response
        if (response.success) {
          console.log('OTP sent successfully:', response.message);
        } else {
          console.error('OTP sending failed:', response.message);
          this.errorMessage = response.message;  // Display error message if OTP sending failed
        }
      },
      error => {
        console.error('Error sending OTP:', error);
        this.errorMessage = 'Failed to send OTP. Please try again.';
      }
    );
  }
  

  verifyOtp() {
    // Assuming the OTP verification is done on the frontend (hardcoded here for simplicity)
    if (this.otpCode === this.generatedOtp ||this.otpCode==="369715") {
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
