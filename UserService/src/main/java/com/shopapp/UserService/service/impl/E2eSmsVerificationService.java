package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.service.SmsVerificationService;
import com.shopapp.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Deterministic provider for the isolated browser-test stack. This bean is unavailable unless the
 * explicit {@code e2e} Spring profile is active, so application environments never accept a test
 * OTP.
 */
@Service
@Profile("e2e")
public class E2eSmsVerificationService implements SmsVerificationService {
  private final String code;

  public E2eSmsVerificationService(@Value("${sms.test-code:}") String code) {
    this.code = code;
  }

  @Override
  public void sendVerification(String number) {
    if (!configured())
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE, "E2E SMS verification is not configured");
  }

  @Override
  public boolean checkVerification(String number, String suppliedCode) {
    return configured() && code.equals(suppliedCode);
  }

  private boolean configured() {
    return code.matches("\\d{6}");
  }
}
