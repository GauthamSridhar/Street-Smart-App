package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.dto.user.request.*;
import com.shopapp.UserService.dto.user.response.UserResponse;
import com.shopapp.UserService.mapper.UserMapper;
import com.shopapp.UserService.model.UserRole;
import com.shopapp.UserService.repository.UserRepository;
import com.shopapp.UserService.service.UserService;
import com.shopapp.common.*;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository users;
  private final UserMapper mapper;
  private final BCryptPasswordEncoder passwords;
  private final OtpService otp;
  private final com.shopapp.UserService.repository.SessionRepository sessions;

  @Override
  @Transactional
  public UserResponse register(RegisterUserRequest request) {
    if (request.getRole() == UserRole.ADMIN)
      throw new AccessDeniedException("Administrator registration is not public");
    String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
    if (users.existsByEmail(email) || users.existsByPhoneNumber(request.getPhoneNumber()))
      throw ApiException.conflict("An account with these details already exists");
    var user = mapper.toEntity(request);
    user.setPassword(passwords.encode(request.getPassword()));
    user.setRole(request.getRole() == null ? UserRole.USER : request.getRole());
    user.setVerified(
        otp.consumeVerification(request.getPhoneNumber(), request.getPhoneVerificationToken()));
    return mapper.toResponse(users.saveAndFlush(user));
  }

  @Override
  @Transactional
  public UserResponse updateProfile(UUID id, UpdateUserRequest request) {
    Caller.owner(id);
    var user = users.locked(id).orElseThrow(() -> ApiException.notFound("User"));
    boolean sensitive =
        !user.getEmail().equalsIgnoreCase(request.getEmail().trim())
            || !user.getPhoneNumber().equals(request.getPhoneNumber())
            || request.getPassword() != null;
    if (sensitive
        && (request.getCurrentPassword() == null
            || !passwords.matches(request.getCurrentPassword(), user.getPassword())))
      throw new AccessDeniedException("Current password is required for this change");
    if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())
        && users.existsByEmail(request.getEmail().trim().toLowerCase(Locale.ROOT)))
      throw ApiException.conflict("Email is already registered");
    if (!user.getPhoneNumber().equals(request.getPhoneNumber())) {
      if (users.existsByPhoneNumber(request.getPhoneNumber()))
        throw ApiException.conflict("Phone number is already registered");
      user.setVerified(
          otp.consumeVerification(request.getPhoneNumber(), request.getPhoneVerificationToken()));
    }
    mapper.updateEntity(user, request);
    if (request.getPassword() != null) user.setPassword(passwords.encode(request.getPassword()));
    if (sensitive) sessions.deleteByUserId(id);
    return mapper.toResponse(users.saveAndFlush(user));
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse findUserById(UUID id) {
    if (!Caller.hasRole("ADMIN")) Caller.owner(id);
    return mapper.toResponse(users.findById(id).orElseThrow(() -> ApiException.notFound("User")));
  }

  @Override
  public boolean doesUserExist(UUID id) {
    return users.existsById(id);
  }
}
