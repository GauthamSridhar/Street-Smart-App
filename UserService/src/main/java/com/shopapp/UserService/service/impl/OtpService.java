package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.model.OtpChallenge;
import com.shopapp.UserService.repository.OtpRepository;
import com.shopapp.UserService.service.SmsVerificationService;
import com.shopapp.common.ApiException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {
  private final OtpRepository repo;
  private final SmsVerificationService sms;
  private final BCryptPasswordEncoder encoder;
  private final boolean enabled;
  private final SecureRandom random = new SecureRandom();

  public OtpService(
      OtpRepository repo,
      SmsVerificationService sms,
      BCryptPasswordEncoder encoder,
      @Value("${sms.enabled:false}") boolean enabled) {
    this.repo = repo;
    this.sms = sms;
    this.encoder = encoder;
    this.enabled = enabled;
  }

  @Transactional
  public void send(String phone) {
    if (!enabled)
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE, "Phone verification is disabled in this environment");
    var old = repo.locked(phone);
    Instant now = Instant.now();
    if (old.isPresent() && old.get().getSentAt().plusSeconds(60).isAfter(now))
      throw new ApiException(
          HttpStatus.TOO_MANY_REQUESTS, "Wait 60 seconds before requesting another code");
    var challenge = old.orElseGet(OtpChallenge::new);
    challenge.setPhone(phone);
    challenge.setCodeHash(
        encoder.encode(Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes())));
    challenge.setExpiresAt(now.plusSeconds(300));
    challenge.setSentAt(now);
    challenge.setAttempts(0);
    challenge.setProofHash(null);
    challenge.setProofExpiresAt(null);
    repo.saveAndFlush(challenge);
    sms.sendVerification(phone);
  }

  // Failed attempts must commit so they cannot be bypassed by transaction rollback.
  @Transactional(noRollbackFor = ApiException.class)
  public String verify(String phone, String code) {
    var challenge =
        repo.locked(phone)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired code"));
    if (challenge.getExpiresAt().isBefore(Instant.now())
        || challenge.getAttempts() >= 5
        || challenge.getProofHash() != null)
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired code");
    challenge.setAttempts(challenge.getAttempts() + 1);
    if (!sms.checkVerification(phone, code))
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired code");
    String proof = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes());
    challenge.setProofHash(encoder.encode(proof));
    challenge.setProofExpiresAt(Instant.now().plusSeconds(600));
    return proof;
  }

  @Transactional
  public boolean consumeVerification(String phone, String proof) {
    if (!enabled) return false;
    var challenge =
        repo.locked(phone)
            .orElseThrow(
                () -> new ApiException(HttpStatus.BAD_REQUEST, "Verify your phone number first"));
    if (proof == null
        || challenge.getProofHash() == null
        || challenge.getProofExpiresAt().isBefore(Instant.now())
        || !encoder.matches(proof, challenge.getProofHash()))
      throw new ApiException(HttpStatus.BAD_REQUEST, "Phone verification is missing or expired");
    repo.delete(challenge);
    return true;
  }

  private byte[] randomBytes() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return bytes;
  }
}
