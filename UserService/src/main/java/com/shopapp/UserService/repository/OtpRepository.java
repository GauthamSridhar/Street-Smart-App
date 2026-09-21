package com.shopapp.UserService.repository;

import com.shopapp.UserService.model.OtpChallenge;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface OtpRepository extends JpaRepository<OtpChallenge, String> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from OtpChallenge o where o.phone = :phone")
  Optional<OtpChallenge> locked(String phone);
}
