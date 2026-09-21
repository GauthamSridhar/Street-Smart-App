package com.shopapp.UserService.service;

/**
 * Provider boundary for phone verification. Real SMS and browser E2E use separate implementations.
 */
public interface SmsVerificationService {
  void sendVerification(String number);

  boolean checkVerification(String number, String code);
}
