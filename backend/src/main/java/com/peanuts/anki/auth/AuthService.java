package com.peanuts.anki.auth;

import com.peanuts.anki.auth.dto.AuthResponse;
import com.peanuts.anki.auth.dto.LoginRequest;
import com.peanuts.anki.auth.dto.RegisterRequest;
import com.peanuts.anki.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());

        log.info("User registered successfully: {}", user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", request.email());
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId());

        log.info("User logged in successfully: {}", user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}