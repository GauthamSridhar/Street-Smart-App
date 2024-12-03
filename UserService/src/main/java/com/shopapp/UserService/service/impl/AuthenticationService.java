package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.dto.user.JwtToken;
import com.shopapp.UserService.dto.user.request.LoginRequest;
import com.shopapp.UserService.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Authenticates the user and generates a JWT token.
     *
     * @param userCredentials the login request containing credentials
     * @return JwtToken containing the generated token
     */
    public JwtToken authenticate(LoginRequest userCredentials) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userCredentials.getIdentifier(),
                        userCredentials.getPassword()));
        String username = authentication.getName();
        String token = jwtUtil.generateToken(username);
        return new JwtToken(token);

    }
}
