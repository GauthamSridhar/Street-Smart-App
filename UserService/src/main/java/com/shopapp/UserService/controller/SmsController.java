package com.shopapp.UserService.controller;

import com.shopapp.UserService.service.impl.SmsSenderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<String> sendSms(@RequestParam String phoneNumber, @RequestParam String message) {
        try {
            smsSender.sendSms(phoneNumber, message);
            return ResponseEntity.ok("OTP sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send OTP: " + e.getMessage());
        }
    }
}
