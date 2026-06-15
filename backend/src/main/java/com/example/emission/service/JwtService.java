package com.example.emission.service;

import com.example.emission.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey secretKey;
  private final long expireHours;

  public JwtService(
      @Value("${platform.jwt-secret}") String secret,
      @Value("${platform.jwt-expire-hours}") long expireHours
  ) {
    this.secretKey = Keys.hmacShaKeyFor(secret.repeat(4).getBytes(StandardCharsets.UTF_8));
    this.expireHours = expireHours;
  }

  public String createToken(UserAccount user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.username())
        .claim("role", user.role())
        .claim("displayName", user.displayName())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expireHours * 3600)))
        .signWith(secretKey)
        .compact();
  }

  public Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean validate(String token) {
    try {
      parseToken(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public UserAccount extractUser(String token) {
    Claims claims = parseToken(token);
    return new UserAccount(
        claims.getSubject(),
        claims.get("role", String.class),
        claims.get("displayName", String.class)
    );
  }
}
