package com.noteplace.readingsummary.auth.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.noteplace.readingsummary.auth.dto.LoginRequest;
import com.noteplace.readingsummary.auth.dto.RegisterRequest;
import com.noteplace.readingsummary.auth.dto.TokenResponse;
import com.noteplace.readingsummary.auth.security.AuthUser;
import com.noteplace.readingsummary.auth.security.JwtService;
import com.noteplace.readingsummary.user.domain.User;
import com.noteplace.readingsummary.user.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository userRepo;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;

  public AuthController(UserRepository userRepo, PasswordEncoder encoder, JwtService jwtService) {
    this.userRepo = userRepo;
    this.encoder = encoder;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  public TokenResponse register(@Valid @RequestBody RegisterRequest req) {
    if (userRepo.existsByEmail(req.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");
    }
    User u = new User();
    u.setEmail(req.email());
    u.setPasswordHash(encoder.encode(req.password()));
    u.setRole("USER");
    userRepo.save(u);
    return new TokenResponse(jwtService.generate(new AuthUser(u.getId(), u.getEmail(), u.getRole())));
  }

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest req) {
    User u = userRepo.findByEmail(req.email())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
    if (!encoder.matches(req.password(), u.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
    }
    return new TokenResponse(jwtService.generate(new AuthUser(u.getId(), u.getEmail(), u.getRole())));
  }
}
