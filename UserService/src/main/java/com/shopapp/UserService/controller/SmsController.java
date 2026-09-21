package com.shopapp.UserService.controller;

import com.shopapp.UserService.service.impl.OtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {
  private final OtpService otp;

  @org.springframework.beans.factory.annotation.Value("${sms.enabled:false}")
  private boolean enabled;

  @GetMapping("/config")
  public Map<String, Boolean> config() {
    return Map.of("enabled", enabled);
  }

  public record SendRequest(
      @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{9,14}$") String phoneNumber) {}

  public record VerifyRequest(
      @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{9,14}$") String phoneNumber,
      @NotBlank @Pattern(regexp = "^\\d{6}$") String otpCode) {}

  @PostMapping("/send")
  public Map<String, Object> send(@Valid @RequestBody SendRequest request) {
    otp.send(request.phoneNumber());
    return Map.of("success", true, "message", "Verification code sent");
  }

  @PostMapping("/verify")
  public Map<String, Object> verify(@Valid @RequestBody VerifyRequest request) {
    return Map.of(
        "success", true, "verificationToken", otp.verify(request.phoneNumber(), request.otpCode()));
  }
}
