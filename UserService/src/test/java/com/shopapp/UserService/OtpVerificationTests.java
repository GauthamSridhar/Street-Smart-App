package com.shopapp.UserService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.shopapp.UserService.repository.OtpRepository;
import com.shopapp.UserService.service.SmsVerificationService;
import com.shopapp.UserService.service.impl.*;
import com.shopapp.common.ApiException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "sms.enabled=true")
@ActiveProfiles("test")
class OtpVerificationTests {
  @Autowired OtpService otp;
  @Autowired OtpRepository repo;
  @MockitoBean SmsVerificationService sms;
  final String phone = "+919876543210";

  @BeforeEach
  void clean() {
    repo.deleteAll();
    reset(sms);
  }

  @Test
  void approvedProviderCheckCreatesProofThatCanOnlyBeUsedOnce() {
    when(sms.checkVerification(phone, "123456")).thenReturn(true);
    otp.send(phone);
    verify(sms).sendVerification(phone);
    String proof = otp.verify(phone, "123456");
    assertThat(otp.consumeVerification(phone, proof)).isTrue();
    assertThatThrownBy(() -> otp.consumeVerification(phone, proof))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void wrongAttemptsArePersistedAndLockTheChallenge() {
    otp.send(phone);
    for (int i = 0; i < 5; i++)
      assertThatThrownBy(() -> otp.verify(phone, "000000")).isInstanceOf(ApiException.class);
    assertThat(repo.findById(phone).orElseThrow().getAttempts()).isEqualTo(5);
    assertThatThrownBy(() -> otp.verify(phone, "123456")).isInstanceOf(ApiException.class);
  }

  @Test
  void expiredCodeIsRejected() {
    otp.send(phone);
    var challenge = repo.findById(phone).orElseThrow();
    challenge.setExpiresAt(java.time.Instant.now().minusSeconds(1));
    repo.saveAndFlush(challenge);
    assertThatThrownBy(() -> otp.verify(phone, "123456")).isInstanceOf(ApiException.class);
  }

  @Test
  void resendCooldownPreventsExtraSms() {
    otp.send(phone);
    assertThatThrownBy(() -> otp.send(phone)).isInstanceOf(ApiException.class);
    verify(sms, times(1)).sendVerification(phone);
  }
}
