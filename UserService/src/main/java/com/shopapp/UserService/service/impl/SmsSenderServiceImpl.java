package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.service.SmsVerificationService;
import com.shopapp.common.ApiException;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Profile("!e2e")
public class SmsSenderServiceImpl implements SmsVerificationService {
  private static final Logger log = LoggerFactory.getLogger(SmsSenderServiceImpl.class);
  private final String accountSid, authToken, apiKeySid, apiKeySecret, verifyServiceSid;

  public SmsSenderServiceImpl(
      @Value("${SMS_ACCOUNT_SID:}") String accountSid,
      @Value("${SMS_AUTH_TOKEN:}") String authToken,
      @Value("${SMS_API_KEY_SID:}") String apiKeySid,
      @Value("${SMS_API_KEY_SECRET:}") String apiKeySecret,
      @Value("${SMS_VERIFY_SERVICE_SID:}") String verifyServiceSid) {
    this.accountSid = accountSid;
    this.authToken = authToken;
    this.apiKeySid = apiKeySid;
    this.apiKeySecret = apiKeySecret;
    this.verifyServiceSid = verifyServiceSid;
  }

  public void sendVerification(String number) {
    if (!configured())
      throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SMS verification is not configured");
    try {
      Verification.creator(verifyServiceSid, number, "sms").create(client());
    } catch (com.twilio.exception.ApiException ex) {
      log.warn(
          "Twilio rejected verification send: status={}, code={}",
          ex.getStatusCode(),
          ex.getCode());
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE, "SMS delivery failed; please retry later");
    } catch (RuntimeException ex) {
      log.warn("SMS provider request failed: {}", ex.getClass().getSimpleName());
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE, "SMS delivery failed; please retry later");
    }
  }

  public boolean checkVerification(String number, String code) {
    if (!configured())
      throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SMS verification is not configured");
    try {
      return "approved"
          .equalsIgnoreCase(
              VerificationCheck.creator(verifyServiceSid)
                  .setTo(number)
                  .setCode(code)
                  .create(client())
                  .getStatus());
    } catch (com.twilio.exception.ApiException ex) {
      log.warn(
          "Twilio rejected verification check: status={}, code={}",
          ex.getStatusCode(),
          ex.getCode());
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE, "SMS verification failed; please retry later");
    } catch (RuntimeException ex) {
      log.warn("SMS provider request failed: {}", ex.getClass().getSimpleName());
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE, "SMS verification failed; please retry later");
    }
  }

  private TwilioRestClient client() {
    return apiKeySid.isBlank()
        ? new TwilioRestClient.Builder(accountSid, authToken).build()
        : new TwilioRestClient.Builder(apiKeySid, apiKeySecret).accountSid(accountSid).build();
  }

  private boolean configured() {
    if (accountSid.isBlank() || verifyServiceSid.isBlank()) return false;
    if (apiKeySid.isBlank()) return !authToken.isBlank();
    return !apiKeySecret.isBlank();
  }
}
