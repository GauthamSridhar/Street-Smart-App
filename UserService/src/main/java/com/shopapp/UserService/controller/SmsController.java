package com.shopapp.UserService.controller;

import com.shopapp.UserService.dto.user.SmsRequest;
import com.shopapp.UserService.service.impl.SmsSenderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final SmsSenderServiceImpl smsSender;

    @Autowired
    public SmsController(SmsSenderServiceImpl smsSender) {
        this.smsSender = smsSender;
    }

    @PostMapping("/send")
    public ResponseEntity<Object> sendSms(@RequestBody SmsRequest smsRequest) {
        try {
            // Use the OTP received in the request
            String otpCode = smsRequest.getOtpCode();
            String phoneNumber = smsRequest.getPhoneNumber();
            String message = smsRequest.getMessage();

            if (otpCode != null && !otpCode.isEmpty()) {
                // If message is empty, use OTP as the message content
                if (message.isEmpty()) {
                    message = otpCode;
                }

                smsSender.sendSms(phoneNumber, message);  // Send OTP via Twilio
                // Return a structured response in JSON format
                return ResponseEntity.ok(new ApiResponse(true, "OTP sent successfully."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(false, "OTP code is required."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse(false, "Failed to send OTP: " + e.getMessage()));
        }
    }

    // ApiResponse class to structure the response as JSON
    public static class ApiResponse {
        private boolean success;
        private String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
