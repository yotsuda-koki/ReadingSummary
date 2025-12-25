package com.noteplace.readingsummary.auth.security;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
  private final SecretKey key;
  private final String issuer;
  private final int minutes;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.access-token-minutes}") int minutes) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.minutes = minutes;
  }

  public String generate(AuthUser user) {
    Instant now = Instant.now();
    return Jwts.builder()
      .issuer(issuer)
      .subject(user.email())
      .claim("uid", user.id())
      .claim("role", user.role())
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(Duration.ofMinutes(minutes))))
      .signWith(key)
      .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
  }
}
